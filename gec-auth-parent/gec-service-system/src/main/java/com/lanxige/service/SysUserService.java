package com.lanxige.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lanxige.Req.ChangePwdReq;
import com.lanxige.Req.UserInfoRsp;
import com.lanxige.model.system.SysUser;
import com.lanxige.model.vo.SysUserQueryVo;

import java.util.List;
import java.util.Map;

public interface SysUserService extends IService<SysUser> {
    /**
     * 分页查询用户信息
     *
     * @param iPage          分页数据
     * @param sysUserQueryVo 查询条件
     * @return 分页数据
     */
    IPage<SysUser> selectPage(IPage<SysUser> iPage, SysUserQueryVo sysUserQueryVo);

    /**
     * 修改用户状态
     *
     * @param id     用户id
     * @param status 用户状态
     * @return 修改结果
     */
    boolean updateStatusById(Long id, Integer status);

    /**
     * 根据用户名查询用户信息
     *
     * @param username 用户名
     * @return 用户信息
     */
    SysUser getUserInfoUserName(String username);

    /**
     * 获取用户信息
     *
     * @param username 用户名
     * @return 用户信息
     */
    Map<String, Object> getUserInfo(String username);

    /**
     * 修改密码
     *
     * @param changePwdReq 修改密码请求
     * @return 修改结果
     */
    int updatePassword(ChangePwdReq changePwdReq);

    /**
     * 添加用户
     *
     * @param sysUser 用户信息
     * @return 添加结果
     */
    boolean addUser(SysUser sysUser);

    /**
     * 获取用户信息
     *
     * @param userId 用户id
     * @return 用户信息
     */
    Map<String, Object> getUserLessInfo(Long userId);

    boolean registerUser(SysUser sysUser);

    List<UserInfoRsp> userWatchLog(String id);

    List<UserInfoRsp> userLikeLog(String id);
}
