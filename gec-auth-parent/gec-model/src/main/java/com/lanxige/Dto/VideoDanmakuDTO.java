package com.lanxige.Dto;

import com.lanxige.model.base.BaseEntity;
import lombok.Data;


/**
 * 弹幕DTO对象
 */
@Data
public class VideoDanmakuDTO extends BaseEntity {
    /**
     * 弹幕ID
     */
    private Long id;

    /**
     * 弹幕内容
     */
    private String content;

    /**
     * 出现时间(秒)
     */
    private Double playTime;

    /**
     * 颜色
     */
    private String color;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户昵称
     */
    private String nickname;

}