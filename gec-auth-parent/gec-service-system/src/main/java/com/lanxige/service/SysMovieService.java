package com.lanxige.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lanxige.Req.ApprovalReq;
import com.lanxige.Req.SendCommentReq;
import com.lanxige.Req.SendDanMuReq;
import com.lanxige.Req.SendLikeReq;
import com.lanxige.Rsp.AllVideoInfoRsp;
import com.lanxige.Rsp.VideoApproRsp;
import com.lanxige.model.system.SysMovie;
import com.lanxige.model.vo.SysMovieQueryVo;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;

public interface SysMovieService extends IService<SysMovie> {
    IPage<SysMovie> selectPage(IPage<SysMovie> p1, SysMovieQueryVo sysMovieQueryVo);

    /**
     * 保存电影信息
     */
    boolean saveMovieInfo(SysMovie sysMovie);

    /**
     * 根据id查询电影信息
     *
     * @param id 电影id
     * @return 电影信息
     */
    HashMap<String, Object> getMovieId(Long id);

    /**
     * 根据id删除电影信息
     *
     * @param id 电影id
     * @return 删除结果
     */
    boolean deleteMovie(Long id);

    /**
     * 批量删除电影信息
     *
     * @param id 电影id列表
     * @return 删除结果
     */
    boolean deleteMovieBatch(List<Long> id);

    /**
     * 修改电影信息
     *
     * @param sysMovie 电影信息
     * @return 修改结果
     */
    boolean updateMovieInfo(SysMovie sysMovie);

    /*
     * 获取所有电影信息
     */
    List<AllVideoInfoRsp> getAllVideoInfo();

    /*
     * 获取热门电影信息
     */
    List<AllVideoInfoRsp> getHotVideoInfo();

    /*
     * 获取热门观看电影信息
     */
    List<AllVideoInfoRsp> getHotWatchVideoInfo();

    /*
     *发送弹幕
     */
    int sendDanMu(SendDanMuReq req);

    /*
     *发送点赞
     */
    String sendLike(SendLikeReq req);

    /*
     *发送评论
     */
    int sendComment(SendCommentReq req);

    /**
     * 记录视频播放量
     *
     * @param id     视频id
     * @param userId 用户id
     */
    void recordVideoPV(Long id, Long userId);

    // 用户上传视频
    int userUploadVideo(SysMovie req, HttpServletRequest httpServletRequest);

    // 用户取消上传视频
    List<VideoApproRsp> showApproveRecord(HttpServletRequest httpServletRequest);

    // 管理员审核视频
    List<VideoApproRsp> showAdminApproveRecord(HttpServletRequest httpServletRequest);

    // 管理员审核视频
    int doApproval(ApprovalReq approvalReq, HttpServletRequest httpServletRequest);

    // 用户取消上传视频
    int doUserCancelApproval(ApprovalReq approvalReq, HttpServletRequest httpServletRequest);

    // 用户查看自己上传的视频
    List<SysMovie> showMyUploadVideo(HttpServletRequest httpServletRequest);
}

