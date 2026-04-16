package com.lanxige.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.lanxige.Dto.VideoCommentDTO;
import com.lanxige.Dto.VideoDanmakuDTO;
import com.lanxige.Rsp.DataTrendRsp;
import com.lanxige.Rsp.HotMovieRsp;
import com.lanxige.Rsp.NewCommentRsp;
import com.lanxige.Rsp.VideoDetailRsp;
import com.lanxige.mapper.system.SysMovieMapper;
import com.lanxige.mapper.system.SysUserMapper;
import com.lanxige.mapper.video.VideoCommentMapper;
import com.lanxige.mapper.video.VideoDanmuMapper;
import com.lanxige.mapper.video.VideoStatMapper;
import com.lanxige.model.system.SysMovie;
import com.lanxige.model.system.SysUser;
import com.lanxige.model.video.VideoComment;
import com.lanxige.model.video.VideoDanmaku;
import com.lanxige.model.video.VideoStat;
import com.lanxige.model.vo.DayCountVo;
import com.lanxige.service.SysDateService;
import com.lanxige.utils.TimeAgoUtil;
import com.lanxige.utils.VodTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SysDateServiceImpl implements SysDateService {
    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysMovieMapper sysMovieMapper;

    @Autowired
    private VideoStatMapper videoStatMapper;

    @Autowired
    private VideoDanmuMapper videoDanmuMapper;

    @Autowired
    private VideoCommentMapper videoCommentMapper;

    @Autowired
    private VodTemplate vodTemplate;

    /**
     * 根据用户id查询角色id
     *
     * @return 用户数据
     */
    @Override
    public DataTrendRsp getUserTrend() {
        List<DayCountVo> list = sysUserMapper.selectUserTrend();

        // 转成 Map<日期, 数量>
        Map<LocalDate, Integer> map = getDateIntegerMap(list);

        List<Integer> actualData = new ArrayList<>();

        // 最近7天
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            actualData.add(map.getOrDefault(date, 0));
        }

        // 查询总用户数
        Integer totalUserCount = sysUserMapper.selectTotalUserCount();

        // 封装返回
        DataTrendRsp rsp = new DataTrendRsp();
        rsp.setActualData(actualData);
        rsp.setTotalUserCount(totalUserCount);

        return rsp;
    }

    /**
     * 获得日期map
     *
     * @param list 日期集合
     * @return 日期map
     */
    private static Map<LocalDate, Integer> getDateIntegerMap(List<DayCountVo> list) {
        Map<LocalDate, Integer> map = list.stream()
                .collect(Collectors.toMap(
                        DayCountVo::getDay,
                        DayCountVo::getCount
                ));
        return map;
    }

    /**
     * 获取电影库存
     *
     * @return 电影库存
     */
    @Override
    public DataTrendRsp getMovieStock() {

        List<DayCountVo> list = sysMovieMapper.selectMovieTrend();

        // 转Map
        Map<LocalDate, Integer> map = getDateIntegerMap(list);

        List<Integer> result = new ArrayList<>();

        // 最近7天
        for (int i = 6; i >= 0; i--) {
            LocalDate day = LocalDate.now().minusDays(i);
            result.add(map.getOrDefault(day, 0));
        }

        // 总数
        Integer total = sysMovieMapper.selectTotalMovieCount();

        DataTrendRsp rsp = new DataTrendRsp();
        rsp.setActualData(result);
        rsp.setTotalUserCount(total);

        return rsp;
    }

    /**
     * 获取电影播放量
     *
     * @return 电影播放量
     */
    @Override
    public DataTrendRsp getMoviePlays() {

        List<DayCountVo> list = videoStatMapper.selectPlayTrend();

        Map<LocalDate, Integer> map = getDateIntegerMap(list);

        List<Integer> result = new ArrayList<>();

        for (int i = 6; i >= 0; i--) {
            LocalDate day = LocalDate.now().minusDays(i);
            result.add(map.getOrDefault(day, 0));
        }

        Integer total = videoStatMapper.selectTotalPlayCount();

        DataTrendRsp rsp = new DataTrendRsp();
        rsp.setActualData(result);
        rsp.setTotalUserCount(total);

        return rsp;
    }

    /**
     * 获取弹幕量
     *
     * @return 弹幕量
     */
    @Override
    public DataTrendRsp getMovieDanmaku() {

        List<DayCountVo> list = videoDanmuMapper.selectDanmakuTrend();

        Map<LocalDate, Integer> map = getDateIntegerMap(list);

        List<Integer> result = new ArrayList<>();

        for (int i = 6; i >= 0; i--) {
            LocalDate day = LocalDate.now().minusDays(i);
            result.add(map.getOrDefault(day, 0));
        }

        Integer total = videoDanmuMapper.selectTotalDanmakuCount();

        DataTrendRsp rsp = new DataTrendRsp();
        rsp.setActualData(result);
        rsp.setTotalUserCount(total);

        return rsp;
    }

    /**
     * 查询视频详情
     *
     * @param videoId 视频id
     * @return 视频详情
     */
    @Override
    public VideoDetailRsp getSingelMovieDetail(Long videoId) {
        VideoDetailRsp rsp = new VideoDetailRsp();

        // 更新视频播放量
        videoStatMapper.update(
                null,
                new LambdaUpdateWrapper<VideoStat>()
                        .eq(VideoStat::getVideoId, videoId)
                        .setSql("visit_pv = visit_pv + 1")
        );

        // 1 查询视频基本信息（排除下架视频 is_approval=2 和已删除）
        SysMovie movie = sysMovieMapper.selectOne(
                new LambdaQueryWrapper<SysMovie>()
                        .eq(SysMovie::getId, videoId)
                        .ne(SysMovie::getIsApproval, "2")
                        .eq(SysMovie::getIsDeleted, 0)
        );
        if (movie == null) {
            return rsp;
        }

        String director = movie.getDirector();
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("username",director);
        SysUser sysCreateUser = sysUserMapper.selectOne(queryWrapper);

        rsp.setVideoId(movie.getId());
        rsp.setName(movie.getName());
        rsp.setImage(movie.getImage());
        rsp.setCategory(movie.getCid());
        rsp.setPublishTime(movie.getCreateTime());
        rsp.setDescription(movie.getDescription());

        // 调用阿里云服务获取播放秘钥
        String playAuth;
        try {
            playAuth = this.vodTemplate.getVideoPlayAuth(movie.getPlayId()).getPlayAuth();
        } catch (Exception e) {
            log.error("获取播放秘钥失败，异常信息：{}", e.getMessage());
            throw new RuntimeException(e);
        }

        // 封装map 集合
        HashMap<String, Object> map = new HashMap<>();
        // 分别封装三个参数  参数的key 要和前端对应
        map.put("image", movie.getImage());
        map.put("playId", movie.getPlayId());
        map.put("playAuth", playAuth);
        log.info("获取播放秘钥成功，结果：{}", map);

        rsp.setPlayId(map);

        // 2 查询统计数据
        QueryWrapper<VideoStat> statWrapper = new QueryWrapper<>();
        statWrapper.eq("video_id", videoId);

        VideoStat stat = videoStatMapper.selectOne(statWrapper);

        if (stat != null) {
            rsp.setPlayCount(stat.getPlayCount());
            rsp.setLikeCount(stat.getLikeCount());
        }

        // 3 查询弹幕
        QueryWrapper<VideoDanmaku> danmakuWrapper = new QueryWrapper<>();
        danmakuWrapper.eq("video_id", videoId);

        List<VideoDanmaku> danmakuList = videoDanmuMapper.selectList(danmakuWrapper);

        List<VideoDanmakuDTO> danmakuDTOList = new ArrayList<>();

        for (VideoDanmaku d : danmakuList) {

            VideoDanmakuDTO dto = new VideoDanmakuDTO();

            dto.setUpdateTime(d.getUpdateTime());
            dto.setIsDeleted(d.getIsDeleted());
            dto.setId(d.getId());
            dto.setContent(d.getContent());
            dto.setPlayTime(d.getPlayTime());
            dto.setColor(d.getColor());
            dto.setCreateTime(d.getCreateTime());
            dto.setUserId(d.getUserId());

            danmakuDTOList.add(dto);
        }

        rsp.setDanmakuList(danmakuDTOList);
        rsp.setDanmakuCount(danmakuDTOList.size());

        // 4 查询评论
        QueryWrapper<VideoComment> commentWrapper = new QueryWrapper<>();
        commentWrapper.eq("video_id", videoId)
                .eq("status", 1)
                .eq("is_deleted",0)
                .orderByDesc("create_time");

        List<VideoComment> commentList = videoCommentMapper.selectList(commentWrapper);

        List<VideoCommentDTO> allComments = new ArrayList<>();

        // 先转换 DTO
        for (VideoComment c : commentList) {

            VideoCommentDTO dto = new VideoCommentDTO();

            dto.setId(c.getId());
            dto.setContent(c.getContent());
            dto.setLikeCount(c.getLikeCount());
            dto.setCreateTime(c.getCreateTime());
            dto.setParentId(c.getParentId());
            dto.setRootId(c.getRootId());

            SysUser sysUser = sysUserMapper.selectById(c.getUserId());
            dto.setUserId(sysUser != null ? sysUser.getName() : "已注销账户");
            dto.setAvatar(sysUser != null ? sysUser.getHeadUrl() : null);

            dto.setChildren(new ArrayList<>());

            allComments.add(dto);
        }

        Map<Long, VideoCommentDTO> commentMap = new HashMap<>();

        for (VideoCommentDTO c : allComments) {
            commentMap.put(c.getId(), c);
        }

        List<VideoCommentDTO> rootComments = new ArrayList<>();

        for (VideoCommentDTO comment : allComments) {

            // 一级评论
            if (comment.getParentId() == null) {

                rootComments.add(comment);
                continue;
            }

            // 找父评论
            VideoCommentDTO parent = commentMap.get(comment.getParentId());

            if (parent != null) {

                comment.setReplyToUser(parent.getUserId());

                parent.getChildren().add(comment);
            }
        }

        rsp.setCommentList(rootComments);
        rsp.setCommentCount(allComments.size());
        rsp.setCreator(director);
        rsp.setCreatorAvatar(sysCreateUser.getHeadUrl());

        return rsp;
    }

    /**
     * 删除弹幕
     *
     * @param danmakuId 弹幕id
     * @return 删除结果
     */
    @Override
    public boolean removeDanmaku(String danmakuId) {
        Long id = Long.valueOf(danmakuId);

        VideoDanmaku danmaku = videoDanmuMapper.selectById(id);
        if (danmaku == null) {
            return false;
        }

        return videoDanmuMapper.deleteById(Long.valueOf(danmakuId)) > 0;
    }

    /**
     * 删除评论
     *
     * @param commentId 评论id
     * @return 删除结果
     */
    @Override
    @Transactional
    public boolean removeComment(String commentId) {

        VideoComment comment = videoCommentMapper.selectById(commentId);
        if (comment == null) {
            return false;
        }

        // 获取原始的父ID和根ID，用于后续更新计数
        Long originalParentId = comment.getParentId();
        Long originalRootId = comment.getRootId(); // 假设实体中有此字段，如果没有则需查询确认

        // 对于根评论：将自身、其所有子评论和楼中楼的 is_deleted 都置为 1
        if (originalParentId == null) {
            // 删除根评论及其所有后代评论
            // 通过 root_id 或 parent_id 找到并更新所有相关子评论
            UpdateWrapper<VideoComment> updateWrapper = new UpdateWrapper<>();
            updateWrapper
                    .eq("id", commentId)           // 删除自己
                    .or()
                    .eq("root_id", commentId)       // 删除所有以该评论为根的子评论
                    .or()
                    .eq("parent_id", commentId)     // 删除所有直接回复该评论的子评论 (作为备选，以防root_id没覆盖全)
                    .set("status",2)
                    .set("is_deleted", 1);

            int updatedRows = videoCommentMapper.update(null, updateWrapper);
            // 可以根据 updatedRows 判断是否成功，但通常返回true表示操作执行成功
            return updatedRows > 0;
        } else {
            // 对于非根评论（包括楼中楼），只删除自己
            UpdateWrapper<VideoComment> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", commentId)
                    .set("is_deleted", 1);

            int updatedRows = videoCommentMapper.update(null, updateWrapper);

            if (updatedRows > 0) {
                // 如果删除成功，再更新其父评论的回复数
                videoCommentMapper.decreaseReplyCount(originalParentId);
            }

            return updatedRows > 0;
        }
    }

    /**
     * 查询站内热门视频
     *
     * @return 热门视频
     */
    @Override
    public List<HotMovieRsp> getHotMovie() {
        return videoStatMapper.selectHotMovie();
    }

    /**
     * 查询站最新评论
     *
     * @return 最新评论
     */
    @Override
    public List<NewCommentRsp> getNewComment() {
        List<NewCommentRsp> list = videoCommentMapper.selectNewComment();

        for (NewCommentRsp rsp : list) {
            LocalDateTime createTime = rsp.getCreateTime();
            rsp.setTime(TimeAgoUtil.format(createTime));
        }

        return list;
    }
}
