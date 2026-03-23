package com.lanxige.Rsp;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 视频审批记录
 */
@Data
public class VideoApproRsp {
    private Long id;
    private Long videoId;
    private String videoName;
    private String approStatus;
    private String approBy;
    private Date approTime;
    private String approDesc;
    private String uploadBy;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String image; // 封面
    private String cid;//所属栏目
    private String description;//描述
    private String director;//导演
}