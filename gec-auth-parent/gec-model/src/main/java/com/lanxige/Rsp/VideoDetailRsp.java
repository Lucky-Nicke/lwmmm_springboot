package com.lanxige.Rsp;

import com.lanxige.Dto.VideoCommentDTO;
import com.lanxige.Dto.VideoDanmakuDTO;
import lombok.Data;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * 视频详情返回对象
 */
@Data
public class VideoDetailRsp {

    /**
     * 视频ID
     */
    private Long videoId;

    /**
     * 视频名称
     */
    private String name;

    /**
     * 视频封面
     */
    private String image;

    /**
     * 视频分类
     */
    private String category;

    /**
     * 发布时间
     */
    private Date publishTime;

    /**
     * 播放量
     */
    private Integer playCount;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 总弹幕数
     */
    private Integer danmakuCount;

    /**
     * 总评论数
     */
    private Integer commentCount;

    /**
     * 创建人名称
     */
    private String creator;

    /**
     * 创建人头像
     */
    private String creatorAvatar;

    /**
     * 简介
     */
    private String description;

    /**
     * 播放id
     */
    private HashMap<String, Object> playId;

    /**
     * 弹幕列表
     */
    private List<VideoDanmakuDTO> danmakuList;

    /**
     * 评论列表
     */
    private List<VideoCommentDTO> commentList;

}