package com.lanxige.controller;

import com.lanxige.Req.ApprovalReq;
import com.lanxige.Rsp.VideoApproRsp;
import com.lanxige.model.system.SysMovie;
import com.lanxige.service.SysMovieService;
import com.lanxige.util.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@Api(tags = "影视管理控制器")
@RequestMapping("/admin/system/sysMovie")
@CrossOrigin
public class VideoApproController {
    @Autowired
    private SysMovieService sysMovieService;

    @ApiOperation("投稿视频")
    @PostMapping("/userUploadVideo")
    public Result userUploadVideo(@RequestBody SysMovie req, HttpServletRequest httpServletRequest) {
        sysMovieService.userUploadVideo(req,httpServletRequest);

        return Result.ok();
    }

    @ApiOperation("显示审批记录")
    @PostMapping("/showApproveRecord")
    public Result showApproveRecord(HttpServletRequest httpServletRequest) {
        List<VideoApproRsp> videoApproRsps = sysMovieService.showApproveRecord(httpServletRequest);

        return Result.ok(videoApproRsps);
    }

    @ApiOperation("管理员需要查看的审批记录")
    @PostMapping("/showAdminApproveRecord")
    public Result showAdminApproveRecord(HttpServletRequest httpServletRequest) {
        List<VideoApproRsp> records = sysMovieService.showAdminApproveRecord(httpServletRequest);
        return Result.ok(records);
    }

    @ApiOperation("管理员审核操作")
    @PostMapping("/doApproval")
    public Result doApproval(@RequestBody ApprovalReq approvalReq, HttpServletRequest httpServletRequest) {
        sysMovieService.doApproval(approvalReq, httpServletRequest);
        return Result.ok();
    }

    @ApiOperation("用户取消审核")
    @PostMapping("/doUserCancelApproval")
    public Result doUserCancelApproval(@RequestBody ApprovalReq approvalReq, HttpServletRequest httpServletRequest) {
        sysMovieService.doUserCancelApproval(approvalReq, httpServletRequest);
        return Result.ok();
    }

    @ApiOperation("查询我上传的视频")
    @PostMapping("/showMyUploadVideo")
    public Result showMyUploadVideo(HttpServletRequest httpServletRequest) {
        List<SysMovie> list = sysMovieService.showMyUploadVideo(httpServletRequest);
        return Result.ok(list);
    }
}
