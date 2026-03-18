package com.lanxige.Req;

import lombok.Data;

/**
 * 审批请求
 */
@Data
public class ApprovalReq {
    private Long videoId;
    private String approStatus;
    private String approDesc;
    private String approBy;
}
