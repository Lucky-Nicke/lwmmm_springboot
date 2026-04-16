package com.lanxige.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lanxige.Req.UserInfoRsp;
import com.lanxige.eurm.BusinessType;
import com.lanxige.model.system.SysUser;
import com.lanxige.model.vo.SysUserQueryVo;
import com.lanxige.service.SysUserService;
import com.lanxige.util.Result;
import com.lanxige.utils.aop.OpenLog;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@Api(tags = "用户管理控制器")
@RequestMapping("/admin/system/sysUser")
@CrossOrigin
public class SysUserController {
    @Autowired
    private SysUserService sysUserService;

    @PreAuthorize("hasAuthority('bnt.sysUser.list')")
    @ApiOperation("分页和条件查询")
    @GetMapping("/{page}/{limit}")
    public Result selectUserPageByVo(@PathVariable Long page,
                                     @PathVariable Long limit,
                                     SysUserQueryVo sysUserQueryVo) {
        IPage<SysUser> iPage = new Page<>(page, limit);
        iPage = this.sysUserService.selectPage(iPage, sysUserQueryVo);

        return Result.ok(iPage);
    }

    // 添加用户
    @OpenLog(title = "用户管理-添加",
            businessType = BusinessType.INSERT,
            requestMethod = "Post")
    @PreAuthorize("hasAuthority('bnt.sysUser.add')")
    @ApiOperation("添加用户")
    @PostMapping("/addUser")
    public Result addUser(@Validated @RequestBody SysUser sysUser) {
        boolean b = sysUserService.addUser(sysUser);

        if (b) {
            return Result.ok();
        } else {
            return Result.fail();
        }
    }

    // 根据id 去获取一个用户
    @PreAuthorize("hasAuthority('bnt.sysUser.list')")
    @ApiOperation("根据id查询用户")
    @GetMapping("/findUserById/{id}")
    public Result findUserById(@PathVariable Long id) {
        return Result.ok(sysUserService.getById(id));
    }

    // 修改用户
    @OpenLog(title = "用户管理-修改",
            businessType = BusinessType.UPDATE,
            requestMethod = "Post")
    @PreAuthorize("hasAuthority('bnt.sysUser.update')")
    @ApiOperation("修改用户信息")
    @PostMapping("/updateUser")
    public Result updateUser(@Validated @RequestBody SysUser sysUser) {
        boolean b = this.sysUserService.updateById(sysUser);

        if (b) {
            return Result.ok();
        } else {
            return Result.fail();
        }
    }

    // 删除用户
    @OpenLog(title = "用户管理-删除",
            businessType = BusinessType.DELETE,
            requestMethod = "Delete")
    @PreAuthorize("hasAuthority('bnt.sysUser.remove')")
    @ApiOperation("删除用户")
    @DeleteMapping("/deleteUser/{id}")
    public Result deleteUser(@PathVariable Long id) {
        boolean b = this.sysUserService.removeById(id);

        if (b) {
            return Result.ok();
        } else {
            return Result.fail();
        }
    }

    // 批量删除用户
    @OpenLog(title = "用户管理-批量删除",
            businessType = BusinessType.DELETE,
            requestMethod = "Delete")
    @PreAuthorize("hasAuthority('bnt.sysUser.remove')")
    @ApiOperation("批量删除用户")
    @DeleteMapping("/batchDeleteUser")
    public Result batchDeleteUser(@RequestBody List<Long> ids) {
        boolean b = this.sysUserService.removeByIds(ids);

        if (b) {
            return Result.ok();
        } else {
            return Result.fail();
        }
    }

    // 更改用户状态
    @OpenLog(title = "用户管理-更改用户状态",
            businessType = BusinessType.UPDATE,
            requestMethod = "Get")
    @PreAuthorize("hasAuthority('bnt.sysUser.update')")
    @ApiOperation("更改用户状态")
    @GetMapping("/updateStatus/{id}/{status}")
    public Result updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        boolean b = this.sysUserService.updateStatusById(id, status);

        if (b) {
            return Result.ok();
        }else {
            return Result.fail();
        }
    }

    @ApiOperation("查询用户信息")
    @GetMapping(value = "/info/{userId}")
    public Result info(@PathVariable Long userId) {
        Map<String, Object> map = sysUserService.getUserLessInfo(userId);
        return Result.ok(map);
    }

    // 添加用户
    @ApiOperation("注册用户")
    @PostMapping("/registerUser")
    public Result registerUser(@Validated @RequestBody SysUser sysUser) {
        boolean b = sysUserService.registerUser(sysUser);

        if (b) {
            return Result.ok();
        } else {
            return Result.fail();
        }
    }

    @ApiOperation("查询用户观看记录")
    @GetMapping(value = "/userWatchLog")
    public Result userWatchLog(String userId) {
        List<UserInfoRsp> list = sysUserService.userWatchLog(userId);
        return Result.ok(list);
    }

    @ApiOperation("查询用户点赞列表")
    @GetMapping(value = "/userLikeLog")
    public Result userLikeLog(String userId) {
        List<UserInfoRsp> list = sysUserService.userLikeLog(userId);
        return Result.ok(list);
    }
}

