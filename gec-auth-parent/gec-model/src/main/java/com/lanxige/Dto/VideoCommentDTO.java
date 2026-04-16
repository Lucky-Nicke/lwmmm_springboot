package com.lanxige.Dto;

import com.lanxige.model.base.BaseEntity;
import lombok.Data;

import java.util.List;

/**
 * 评论返回对象
 */
@Data
public class VideoCommentDTO extends BaseEntity {

    // 评论id
    private Long id;

    // 用户昵称
    private String userId;

    // 用户昵称
    private String avatar;

    // 评论父id
    private Long parentId;

    // 评论根id
    private Long rootId;

    // 评论内容
    private String content;

    // 点赞数
    private Integer likeCount;

    // 回复数
    private Integer replyCount;

    // 回复谁
    private String replyToUser;

    // 子评论
    private List<VideoCommentDTO> children;
}