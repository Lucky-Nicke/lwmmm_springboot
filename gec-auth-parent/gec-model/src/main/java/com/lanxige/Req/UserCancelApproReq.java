package com.lanxige.Req;

import lombok.Data;

/**
 * 用户取消上传视频
 */
@Data
public class UserCancelApproReq {
    private String userId;
    private String videoId;
}
