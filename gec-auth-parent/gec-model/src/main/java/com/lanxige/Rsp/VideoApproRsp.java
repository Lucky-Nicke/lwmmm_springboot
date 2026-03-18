package com.lanxige.Rsp;

import lombok.Data;

import java.util.Date;

/**
 * 视频审批记录
 */
@Data
public class VideoApproRsp {
    private Integer id;
    private Integer videoId;
    private String approStatus;
    private String approBy;
    private Date approTime;
    private String approDesc;
    private Date createTime;
    private Date updateTime;
    private String uploadBy;
}