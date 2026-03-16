package com.lanxige.model.video;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Description: 视频点赞记录
 */
@Data
@TableName("video_like_log")
public class VideoLikeLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long videoId;

    private Long commentId;

    // 1点赞 0取消
    private Integer status;

    private String type;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer isDeleted;
}