package com.lanxige.controller;

import com.lanxige.Req.ApprovalReq;
import com.lanxige.Req.UploadVideoReq;
import com.lanxige.Req.UserCancelApproReq;
import com.lanxige.Rsp.VideoApproRsp;
import com.lanxige.model.system.SysMovie;
import com.lanxige.service.SysMovieService;
import com.lanxige.util.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Api(tags = "影视投稿管理控制器")
@RequestMapping("/admin/system/sysMovie")
@CrossOrigin
public class VideoApproController {
    @Autowired
    private SysMovieService sysMovieService;

    @ApiOperation("投稿视频")
    @PostMapping("/userUploadVideo")
    public Result userUploadVideo(@RequestBody UploadVideoReq req) {
        int i = sysMovieService.userUploadVideo(req);
        return Result.ok(i);
    }

    @ApiOperation("显示审批记录")
    @PostMapping("/showApproveRecord")
    public Result showApproveRecord(String userId) {
        List<VideoApproRsp> videoApproRsps = sysMovieService.showApproveRecord(userId);

        return Result.ok(videoApproRsps);
    }

    @ApiOperation("管理员需要查看的审批记录")
    @PostMapping("/showAdminApproveRecord")
    public Result showAdminApproveRecord() {
        List<VideoApproRsp> records = sysMovieService.showAdminApproveRecord();
        return Result.ok(records);
    }

    @ApiOperation("管理员审核操作")
    @PostMapping("/doApproval")
    public Result doApproval(@RequestBody ApprovalReq approvalReq, String userId) {
        sysMovieService.doApproval(approvalReq, userId);
        return Result.ok();
    }

    @ApiOperation("用户取消审核")
    @PostMapping("/doUserCancelApproval")
    public Result doUserCancelApproval(@RequestBody UserCancelApproReq req) {
        sysMovieService.doUserCancelApproval(req);
        return Result.ok();
    }

    @ApiOperation("查询我上传的视频")
    @PostMapping("/showMyUploadVideo")
    public Result showMyUploadVideo(String username) {
        List<SysMovie> list = sysMovieService.showMyUploadVideo(username);
        return Result.ok(list);
    }
}
