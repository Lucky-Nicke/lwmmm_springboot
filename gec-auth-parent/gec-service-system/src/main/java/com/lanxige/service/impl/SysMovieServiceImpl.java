package com.lanxige.service.impl;

import com.aliyuncs.vod.model.v20170321.GetVideoPlayAuthRequest;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanxige.Req.*;
import com.lanxige.Rsp.AllVideoInfoRsp;
import com.lanxige.Rsp.VideoApproRsp;
import com.lanxige.mapper.system.SysMovieMapper;
import com.lanxige.mapper.system.SysUserMapper;
import com.lanxige.mapper.video.*;
import com.lanxige.model.system.SysMovie;
import com.lanxige.model.system.SysUser;
import com.lanxige.model.video.*;
import com.lanxige.model.vo.SysMovieQueryVo;
import com.lanxige.service.SysMovieService;
import com.lanxige.utils.VodTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SysMovieServiceImpl extends ServiceImpl<SysMovieMapper, SysMovie> implements SysMovieService {
    @Autowired
    private VideoStatMapper videoStatMapper;

    @Autowired
    private VodTemplate vodTemplate;

    @Autowired
    private VideoDanmuMapper videoDanmuMapper;

    @Autowired
    private VideoCommentMapper videoCommentMapper;

    @Autowired
    private SysMovieMapper sysMovieMapper;

    @Autowired
    private VideoLikeLogMapper videoLikeLogMapper;

    @Autowired
    private VideoWatchLogMapper videoWatchLogMapper;

    @Autowired
    private VideoApproMapper videoApproMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    public IPage<SysMovie> selectPage(IPage<SysMovie> p1, SysMovieQueryVo sysMovieQueryVo) {
        return this.baseMapper.selectPage(p1, sysMovieQueryVo);
    }

    /**
     * 保存电影信息
     */
    @Override
    public boolean saveMovieInfo(AddSysMovieReq sysMovie) {
        log.info("保存电影信息，参数：{}", sysMovie);

        // 1. 转换对象
        SysMovie movie = new SysMovie();
        BeanUtils.copyProperties(sysMovie, movie);
        movie.setIsApproval("1");
        movie.setDirector(sysMovie.getUsername());

        // 2. 插入 sys_movie
        int i = sysMovieMapper.insert(movie);
        log.info("保存电影信息成功，结果：{}", i);

        // 3. 获取自增ID（关键点！）
        String movieId = String.valueOf(movie.getId());
        log.info("生成的movieId：{}", movieId);

        // 4. 插入 video_stat
        VideoStat videoStat = new VideoStat();
        videoStat.setVideoId(movieId);
        videoStat.setVisitPv(0);
        videoStat.setLikeCount(0);
        videoStat.setPlayCount(0);

        int insert = videoStatMapper.insert(videoStat);
        log.info("保存至video_stat表，结果：{}", insert);

        return i > 0 && insert > 0;
    }

    /**
     * 根据id获取视频信息
     *
     * @param id 视频id
     * @return 视频信息
     */
    @Override
    public HashMap<String, Object> getMovieId(Long id) {
        //1.根据id 获取 到 SysMovie
        SysMovie sysMovie = this.getById(id);
        //2. 从  SysMovie 中获取到 image  playId  Auth
        String image = sysMovie.getImage();

        String playId = sysMovie.getPlayId();
        System.out.println("playId = " + playId);
        // 根据playId 去阿里云服务器获取播放秘钥
        GetVideoPlayAuthRequest request = new GetVideoPlayAuthRequest();
        request.setVideoId(playId);
        String playAuth = null;

        // 调用阿里云服务获取播放秘钥
        try {
            playAuth = this.vodTemplate.getVideoPlayAuth(playId).getPlayAuth();
        } catch (Exception e) {
            log.error("获取播放秘钥失败，异常信息：{}", e.getMessage());
            throw new RuntimeException(e);
        }

        // 封装map 集合
        HashMap<String, Object> map = new HashMap<>();
        // 分别封装三个参数  参数的key 要和前端对应
        map.put("image", image);
        map.put("playId", playId);
        map.put("playAuth", playAuth);
        log.info("获取播放秘钥成功，结果：{}", map);

        return map;
    }

    @Override
    public boolean deleteMovie(Long id) {
        boolean b = this.removeById(id);
        log.info("删除视频成功 {}", b);

        if (!b) {
            return false;
        }

        // 删除弹幕
        LambdaQueryWrapper<VideoDanmaku> danmakuWrapper = new LambdaQueryWrapper<>();
        danmakuWrapper.eq(VideoDanmaku::getVideoId, id);
        videoDanmuMapper.delete(danmakuWrapper);

        // 删除统计
        LambdaQueryWrapper<VideoStat> statWrapper = new LambdaQueryWrapper<>();
        statWrapper.eq(VideoStat::getVideoId, id);
        videoStatMapper.delete(statWrapper);

        // 删除评论
        LambdaQueryWrapper<VideoComment> commentWrapper = new LambdaQueryWrapper<>();
        commentWrapper.eq(VideoComment::getVideoId, id);
        videoCommentMapper.delete(commentWrapper);

        return b;
    }

    @Override
    public boolean deleteMovieBatch(List<Long> ids) {
        boolean b = this.removeByIds(ids);
        log.info("删除视频成功{}", b);

        // 删除弹幕
        LambdaQueryWrapper<VideoDanmaku> danmakuWrapper = new LambdaQueryWrapper<>();
        danmakuWrapper.in(VideoDanmaku::getVideoId, ids);
        videoDanmuMapper.delete(danmakuWrapper);

        // 删除统计
        LambdaQueryWrapper<VideoStat> statWrapper = new LambdaQueryWrapper<>();
        statWrapper.in(VideoStat::getVideoId, ids);
        videoStatMapper.delete(statWrapper);

        // 删除评论
        LambdaQueryWrapper<VideoComment> commentWrapper = new LambdaQueryWrapper<>();
        commentWrapper.in(VideoComment::getVideoId, ids);
        videoCommentMapper.delete(commentWrapper);

        return b;
    }

    /**
     * 修改电影信息
     *
     * @param sysMovie 电影信息
     * @return 修改结果
     */
    @Override
    public boolean updateMovieInfo(SysMovie sysMovie) {
        boolean b = this.updateById(sysMovie);
        log.info("修改视频成功{}", b);

        return b;
    }

    @Override
    public List<AllVideoInfoRsp> getAllVideoInfo() {

        QueryWrapper<SysMovie> wrapper = new QueryWrapper<>();
        wrapper.eq("is_deleted", 0)
                .eq("is_approval", "1")
                .orderByDesc("create_time");

        List<SysMovie> movieList = sysMovieMapper.selectList(wrapper);

        return movieList.stream().filter(movie -> {
            // 过滤掉作者已被删除的视频
            long count = sysUserMapper.selectCount(
                    new QueryWrapper<SysUser>()
                            .eq("username", movie.getDirector())
                            .eq("is_deleted", 0));
            return count > 0;
        }).map(movie -> {

            AllVideoInfoRsp rsp = new AllVideoInfoRsp();
            rsp.setId(movie.getId());
            rsp.setTitle(movie.getName());
            rsp.setAuthor(movie.getDirector());
            rsp.setCoverUrl(movie.getImage());
            rsp.setCategory(movie.getCid());

            VideoStat videoStat = videoStatMapper.selectOne(
                    new QueryWrapper<VideoStat>().eq("video_id", movie.getId()));
            rsp.setViews(videoStat.getPlayCount());

            return rsp;

        }).collect(Collectors.toList());
    }

    @Override
    public List<AllVideoInfoRsp> getHotVideoInfo() {
        return sysMovieMapper.selectHotVideos();
    }

    @Override
    public List<AllVideoInfoRsp> getHotWatchVideoInfo() {
        return sysMovieMapper.selectHotWatchVideos();
    }

    /**
     * 发送弹幕
     *
     * @param req 请求参数
     * @return 发送结果
     */
    @Override
    public int sendDanMu(SendDanMuReq req) {
        VideoDanmaku danmaku = new VideoDanmaku();

        // 视频ID
        danmaku.setVideoId(Long.valueOf(req.getVideoId()));

        // 用户ID（允许为空）
        if (req.getUserId() != null) {
            danmaku.setUserId(Long.valueOf(req.getUserId()));
        }

        // 内容
        danmaku.setContent(req.getContent());

        // 播放时间
        if (req.getTime() != null) {
            danmaku.setPlayTime(req.getTime());
        }

        // 默认颜色
        danmaku.setColor("#000000");

        return videoDanmuMapper.insert(danmaku);
    }

    /**
     * 发送点赞
     *
     * @param req 请求参数
     * @return 发送结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String sendLike(SendLikeReq req) {

        // 1. 查询点赞记录
        LambdaQueryWrapper<VideoLikeLog> wrapper = new LambdaQueryWrapper<>();
        if ("comment".equals(req.getLikeType())) {
            wrapper.eq(VideoLikeLog::getUserId, req.getUserId())
                    .eq(VideoLikeLog::getVideoId, req.getVideoId())
                    .eq(VideoLikeLog::getType, req.getLikeType())
                    .eq(VideoLikeLog::getCommentId, req.getCommentId())
                    .eq(VideoLikeLog::getIsDeleted, 0)
                    .last("limit 1");
        } else {
            wrapper.eq(VideoLikeLog::getUserId, req.getUserId())
                    .eq(VideoLikeLog::getVideoId, req.getVideoId())
                    .eq(VideoLikeLog::getType, req.getLikeType())
                    .eq(VideoLikeLog::getIsDeleted, 0)
                    .last("limit 1");
        }


        VideoLikeLog likeLog = videoLikeLogMapper.selectOne(wrapper);

        // 2. 已经点赞
        if (likeLog != null && likeLog.getStatus() == 1 && req.getLike()) {
            return "已经点过赞了";
        }

        // 3. 第一次点赞
        if (likeLog == null && req.getLike()) {
            VideoLikeLog log = new VideoLikeLog();
            log.setUserId(req.getUserId());
            log.setVideoId(req.getVideoId());
            log.setType(req.getLikeType());
            log.setCommentId(req.getCommentId());
            log.setStatus(1);
            videoLikeLogMapper.insert(log);

            // 更新点赞数
            if ("video".equalsIgnoreCase(req.getLikeType())) {
                videoStatMapper.update(null,
                        new LambdaUpdateWrapper<VideoStat>()
                                .eq(VideoStat::getVideoId, req.getVideoId())
                                .setSql("like_count = like_count + 1"));
            } else if ("comment".equalsIgnoreCase(req.getLikeType())) {
                videoCommentMapper.update(null,
                        new LambdaUpdateWrapper<VideoComment>()
                                .eq(VideoComment::getId, req.getCommentId())
                                .setSql("like_count = like_count + 1"));
            }

            return "点赞成功";
        }

        // 4. 取消点赞
        if (likeLog != null && !req.getLike()) {
            likeLog.setStatus(0);
            videoLikeLogMapper.updateById(likeLog);

            if ("video".equalsIgnoreCase(req.getLikeType())) {
                videoStatMapper.update(null,
                        new LambdaUpdateWrapper<VideoStat>()
                                .eq(VideoStat::getVideoId, req.getVideoId())
                                .setSql("like_count = GREATEST(like_count - 1,0)"));
            } else if ("comment".equalsIgnoreCase(req.getLikeType())) {
                videoCommentMapper.update(null,
                        new LambdaUpdateWrapper<VideoComment>()
                                .eq(VideoComment::getId, req.getCommentId())
                                .setSql("like_count = GREATEST(like_count - 1,0)"));
            }

            return "取消点赞成功";
        }

        // 5. 重新点赞（之前取消过的）
        if (likeLog != null && req.getLike()) {
            likeLog.setStatus(1);
            videoLikeLogMapper.updateById(likeLog);

            if ("video".equalsIgnoreCase(req.getLikeType())) {
                videoStatMapper.update(null,
                        new LambdaUpdateWrapper<VideoStat>()
                                .eq(VideoStat::getVideoId, req.getVideoId())
                                .setSql("like_count = like_count + 1"));
            } else if ("comment".equalsIgnoreCase(req.getLikeType())) {
                // 目标评论ID
                Long commentId = req.getCommentId();

                if (req.getLike()) {
                    // 点赞
                    videoCommentMapper.update(null,
                            new LambdaUpdateWrapper<VideoComment>()
                                    .eq(VideoComment::getId, commentId)
                                    .setSql("like_count = like_count + 1"));
                }
            }

            return "点赞成功";
        }

        return "操作失败";
    }

    /**
     * 发送评论
     *
     * @param req 请求参数
     * @return 发送结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int sendComment(SendCommentReq req) {

        VideoComment comment = new VideoComment();

        comment.setVideoId(req.getVideoId());
        comment.setUserId(req.getUserId());
        comment.setContent(req.getContent());

        // 一级评论
        if (req.getParentId() == null) {

            videoCommentMapper.insert(comment);

            // rootId = 自己
            comment.setRootId(comment.getId());
            videoCommentMapper.updateById(comment);

        } else {

            // 回复评论 / 楼中楼
            comment.setRootId(req.getCommentRootId());
            comment.setParentId(req.getParentId());

            videoCommentMapper.insert(comment);

            // 父评论回复数 +1
            videoCommentMapper.update(null,
                    new LambdaUpdateWrapper<VideoComment>()
                            .eq(VideoComment::getId, req.getParentId())
                            .setSql("reply_count = reply_count + 1"));
        }

        return 1;
    }

    @Override
    @Transactional
    public void recordVideoPV(Long videoId, Long userId) {

        // 1. 播放量 +1
        videoStatMapper.update(
                null,
                new LambdaUpdateWrapper<VideoStat>()
                        .eq(VideoStat::getVideoId, videoId)
                        .setSql("play_count = play_count + 1")
        );

        // 2. 如果用户登录，记录观看日志
        if (userId != null) {
            VideoWatchLog log = new VideoWatchLog();
            log.setUserId(userId);
            log.setVideoId(videoId);
            videoWatchLogMapper.insert(log);
        }
    }

    @Override
    public int userUploadVideo(UploadVideoReq req) {
        SysMovie movie = req.getMovie();
        movie.setIsApproval("0");
        movie.setDirector(req.getUsername());
        // 插入电影
        sysMovieMapper.insert(movie);

        // 插入审核记录
        VideoAppro videoAppro = new VideoAppro();
        videoAppro.setVideoId(movie.getId());
        videoAppro.setApproStatus("DOING");
        videoAppro.setUploadBy(req.getUserId());
        videoAppro.setUploadBy(req.getUserId());
        videoApproMapper.insert(videoAppro);

        // 插入视频统计表（初始化数据）
        VideoStat videoStat = new VideoStat();
        videoStat.setVideoId(String.valueOf(movie.getId()));
        videoStatMapper.insert(videoStat);
        return 1;
    }

    @Override
    public List<VideoApproRsp> showApproveRecord(String userId) {
        // 查询该用户上传的审核记录
        LambdaQueryWrapper<VideoAppro> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VideoAppro::getUploadBy, userId);
        wrapper.orderByDesc(VideoAppro::getCreateTime);
        List<VideoAppro> list = videoApproMapper.selectList(wrapper);

        // 转换为rsp并设置封面
        return list.stream().map(item -> {
            VideoApproRsp rsp = new VideoApproRsp();
            BeanUtils.copyProperties(item, rsp);

            // 查询封面
            SysMovie movie = sysMovieMapper.selectById(item.getVideoId());
            if (movie != null) {
                rsp.setDirector(movie.getDirector());
                rsp.setCid(movie.getCid());
                rsp.setDescription(movie.getDescription());
                rsp.setImage(movie.getImage());
                rsp.setVideoName(movie.getName());
            }

            return rsp;
        }).collect(Collectors.toList());
    }

    @Override
    public List<VideoApproRsp> showAdminApproveRecord() {
        LambdaQueryWrapper<VideoAppro> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(VideoAppro::getCreateTime);
        List<VideoAppro> list = videoApproMapper.selectList(wrapper);

        return list.stream().map(item -> {
            VideoApproRsp rsp = new VideoApproRsp();
            BeanUtils.copyProperties(item, rsp);

            SysMovie sysMovie = sysMovieMapper.selectById(item.getVideoId());
            rsp.setDirector(sysMovie.getDirector());
            rsp.setCid(sysMovie.getCid());
            rsp.setDescription(sysMovie.getDescription());
            rsp.setVideoName(sysMovie.getName());
            rsp.setImage(sysMovie.getImage());

            return rsp;
        }).collect(Collectors.toList());
    }

    @Override
    public int doApproval(ApprovalReq req, String userId) {
        LambdaQueryWrapper<VideoAppro> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VideoAppro::getVideoId, req.getVideoId());
        VideoAppro videoAppro = videoApproMapper.selectOne(wrapper);
        if (videoAppro == null) {
            throw new RuntimeException("审核记录不存在");
        }
        if ("SUCC".equals(req.getApproStatus())) {
            // 审核通过：更新 sys_movie 状态为 1
            SysMovie sysMovie = new SysMovie();
            sysMovie.setId(req.getVideoId());
            sysMovie.setIsApproval("1");
            sysMovieMapper.updateById(sysMovie);

            // 更新审核记录
            videoAppro.setApproStatus("PASS");
            videoAppro.setApproBy(userId);
            videoAppro.setApproTime(new Date());
            videoApproMapper.updateById(videoAppro);
        } else if ("FAIL".equals(req.getApproStatus())) {
            // 审核拒绝：更新 sys_movie 状态为 0
            SysMovie sysMovie = new SysMovie();
            sysMovie.setId(req.getVideoId());
            sysMovie.setIsApproval("0");
            sysMovieMapper.updateById(sysMovie);

            // 更新审核记录
            videoAppro.setApproStatus("REJECT");
            videoAppro.setApproBy(userId);
            videoAppro.setApproDesc(req.getApproDesc());
            videoApproMapper.updateById(videoAppro);

            // 删除 video_stat 统计数据
            LambdaQueryWrapper<VideoStat> statWrapper = new LambdaQueryWrapper<>();
            statWrapper.eq(VideoStat::getVideoId, String.valueOf(req.getVideoId()));
            videoStatMapper.delete(statWrapper);
        }
        return 1;
    }

    @Override
    public int doUserCancelApproval(UserCancelApproReq req) {
        String userId = req.getUserId();
        LambdaQueryWrapper<VideoAppro> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VideoAppro::getVideoId, req.getVideoId())
                .eq(VideoAppro::getUploadBy, userId);
        VideoAppro videoAppro = videoApproMapper.selectOne(wrapper);

        if (videoAppro == null) {
            throw new RuntimeException("审核记录不存在或无权操作");
        }
        if (!"DOING".equals(videoAppro.getApproStatus())) {
            throw new RuntimeException("当前状态不可取消");
        }

        // 更新审核状态
        videoAppro.setApproStatus("CANCEL");
        videoApproMapper.updateById(videoAppro);

        // 同步 sys_movie 审核状态为 2
        LambdaUpdateWrapper<SysMovie> movieWrapper = new LambdaUpdateWrapper<>();
        movieWrapper.eq(SysMovie::getId, req.getVideoId())
                .set(SysMovie::getIsApproval, "2");
        sysMovieMapper.update(null, movieWrapper);

        // 删除 video_stat 统计数据
        LambdaQueryWrapper<VideoStat> statWrapper = new LambdaQueryWrapper<>();
        statWrapper.eq(VideoStat::getVideoId, String.valueOf(req.getVideoId()));
        videoStatMapper.delete(statWrapper);
        return 1;
    }

    @Override
    public List<SysMovie> showMyUploadVideo(String username) {
        LambdaQueryWrapper<SysMovie> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(SysMovie::getCreateTime);
        wrapper.eq(SysMovie::getDirector, username).eq(SysMovie::getIsDeleted, 0);
        return sysMovieMapper.selectList(wrapper);
    }

    @Override
    public boolean offShelfMovie(Long id) {
        LambdaUpdateWrapper<SysMovie> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SysMovie::getId, id).set(SysMovie::getIsApproval, "2").set(SysMovie::getUpdateTime, new Date());
        return this.update(wrapper);
    }
}

