package com.lanxige.controller;

import com.lanxige.Req.ChangePwdReq;
import com.lanxige.model.vo.LoginVo;
import com.lanxige.service.SysUserService;
import com.lanxige.util.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@Api(tags = "登录管理控制器")
@RequestMapping("/admin/system/index")
@CrossOrigin
public class LoginController {

    @Autowired
    private SysUserService sysUserService;

    @ApiOperation("登录")
    @PostMapping("/login")
    public void login(@RequestBody LoginVo loginVo) {
        //使用springSecurity进行认证
    }

    @ApiOperation("查询用户菜单信息")
    @GetMapping(value = "/info")
    public Result info(String username) {
        Map<String, Object> map = sysUserService.getUserInfo(username);
        return Result.ok(map);
    }

    @ApiOperation("退出登录")
    @PostMapping("/logout")
    public Result logout() {
        return Result.ok();
    }

    @ApiOperation("修改密码")
    @PostMapping("/changePwd")
    public Result changePwd(@Validated @RequestBody ChangePwdReq changePwdReq) {
        int updated = sysUserService.updatePassword(changePwdReq);
        return Result.ok(updated);
    }
}
