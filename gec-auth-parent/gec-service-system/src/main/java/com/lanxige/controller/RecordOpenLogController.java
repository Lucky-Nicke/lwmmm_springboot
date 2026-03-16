package com.lanxige.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lanxige.eurm.BusinessType;
import com.lanxige.model.system.SysOpenLog;
import com.lanxige.model.vo.SysOperLogQueryVo;
import com.lanxige.service.SysOpenLogService;
import com.lanxige.util.Result;
import com.lanxige.utils.aop.OpenLog;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Api(tags = "系统管理-打开菜单日志")
@RequestMapping("/admin/system/openrecord")
@CrossOrigin
public class RecordOpenLogController {
    @Autowired
    private SysOpenLogService sysOpenLogService;

    @PreAuthorize("hasAuthority('bnt.sysOperLog.list')")
    @ApiOperation("查询打开菜单记录log")
    @GetMapping("/{page}/{limit}")
    public Result getRecordLog(@PathVariable Long page, @PathVariable Long limit, SysOperLogQueryVo sysOperLogQueryVo){
        IPage<SysOpenLog> iPage = new Page<>(page,limit);
        iPage = this.sysOpenLogService.getLoginLog(iPage, sysOperLogQueryVo);
        return Result.ok(iPage);
    }

    @PreAuthorize("hasAuthority(bnt.sysOperLog.delete)")
    @OpenLog(title = "操作日志管理-删除",
            businessType = BusinessType.DELETE,
            requestMethod = "Delete")
    @ApiOperation("删除打开菜单记录")
    @PostMapping("/del/{id}")
    public Result getRecordLog(@PathVariable Long id){
        boolean b = this.sysOpenLogService.removeById(id);
        if (b) {
            return Result.ok();
        } else {
            return Result.fail("删除失败");

        }
    }

    @PreAuthorize("hasAuthority(bnt.sysOperLog.delete)")
    @OpenLog(title = "操作日志管理-批量删除",
            businessType = BusinessType.DELETE,
            requestMethod = "Delete")
    @ApiOperation("批量删除打开菜单记录")
    @PostMapping("/batchdel/{ids}")
    public Result getRecordLog(@PathVariable List<Long> ids){
        boolean b = this.sysOpenLogService.removeByIds(ids);
        if (b) {
            return Result.ok();
        } else {
            return Result.fail();

        }
    }
}
