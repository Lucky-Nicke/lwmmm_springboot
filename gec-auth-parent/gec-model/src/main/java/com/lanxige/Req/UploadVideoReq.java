package com.lanxige.Req;

import com.lanxige.model.system.SysMovie;
import lombok.Data;

/**
 * 用户上传视频请求
 */
@Data
public class UploadVideoReq {
    private SysMovie movie;
    private String userId;
    private String username;
}