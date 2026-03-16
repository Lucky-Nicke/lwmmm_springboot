package com.lanxige.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanxige.Req.ChangePwdReq;
import com.lanxige.Req.UserInfoRsp;
import com.lanxige.mapper.system.SysRoleMapper;
import com.lanxige.mapper.system.SysUserMapper;
import com.lanxige.mapper.system.SysUserRoleMapper;
import com.lanxige.mapper.video.VideoLikeLogMapper;
import com.lanxige.mapper.video.VideoWatchLogMapper;
import com.lanxige.model.system.SysRole;
import com.lanxige.model.system.SysUser;
import com.lanxige.model.system.SysUserRole;
import com.lanxige.model.vo.RouterVo;
import com.lanxige.model.vo.SysUserQueryVo;
import com.lanxige.service.SysMenuService;
import com.lanxige.service.SysUserService;
import com.lanxige.util.MD5Helper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {
    @Autowired
    private SysUserMapper sysUserMapper;

    private final SysMenuService iSysMenuService;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private VideoWatchLogMapper videoWatchLogMapper;

    @Autowired
    private VideoLikeLogMapper videoLikeLogMapper;

    public SysUserServiceImpl(SysMenuService SysMenuService) {
        this.iSysMenuService = SysMenuService;
    }

    /**
     * 分页查询用户信息
     *
     * @param iPage          分页数据
     * @param sysUserQueryVo 查询条件
     * @return 分页数据
     */
    @Override
    public IPage<SysUser> selectPage(IPage<SysUser> iPage, SysUserQueryVo sysUserQueryVo) {
        log.info("分页查询用户信息");
        return this.baseMapper.selectPage(iPage, sysUserQueryVo);
    }

    /**
     * 修改用户状态
     *
     * @param id     用户id
     * @param status 用户状态
     * @return 修改结果
     */
    @Override
    public boolean updateStatusById(Long id, Integer status) {
        SysUser sysUser = this.getById(id);
        sysUser.setStatus(status);
        int i = this.baseMapper.updateById(sysUser);

        return i > 0;
    }

    /**
     * 根据用户名查询用户信息
     *
     * @param username 用户名
     * @return 用户信息
     */
    @Override
    public SysUser getUserInfoUserName(String username) {
        QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        return this.baseMapper.selectOne(queryWrapper);
    }

    /**
     * 获取用户信息
     *
     * @param username 用户名
     * @return 用户信息
     */
    @Override
    public Map<String, Object> getUserInfo(String username) {
        Map<String, Object> map = new HashMap<>();
        SysUser sysUser = this.getUserInfoUserName(username);

        List<RouterVo> routerVoList = iSysMenuService.findUserMenuList(sysUser.getId());
        List<String> permsList = iSysMenuService.findUserPermsList(sysUser.getId());

        //当前权限控制使用不到，我们暂时忽略
        map.put("name", sysUser.getName());
        map.put("avatar", sysUser.getHeadUrl());

        map.put("buttons", permsList);
        map.put("routers", routerVoList);
        return map;
    }

    /**
     * 修改密码
     *
     * @param req 修改密码请求
     * @return 修改结果
     */
    @Override
    public int updatePassword(ChangePwdReq req) {
        String username = req.getUsername();
        String oldPwd = req.getOldPwd();
        String newPwd = req.getNewPwd();

        if (StringUtils.isBlank(username) || StringUtils.isBlank(oldPwd)) {
            throw new RuntimeException("用户名或密码不能为空");
        }

        String passwordWithMD5 = MD5Helper.md5(oldPwd);
        QueryWrapper<SysUser> qw = new QueryWrapper<>();
        qw.eq("username", username);
        qw.eq("password", passwordWithMD5);
        SysUser sysUser = sysUserMapper.selectOne(qw);

        // 账号密码错误
        if (sysUser == null) {
            throw new RuntimeException("用户名或密码错误，请重新输入！");
        }

        log.info("用户名：{}，密码：{}", username, passwordWithMD5);

        // 开始修改密码
        QueryWrapper<SysUser> qw2 = new QueryWrapper<>();
        qw2.eq("username", username);
        String newPasswordWithMD5 = MD5Helper.md5(newPwd);
        sysUser.setPassword(newPasswordWithMD5);

        return sysUserMapper.update(sysUser, qw);
    }

    /**
     * 添加用户
     *
     * @param sysUser 用户信息
     * @return 添加结果
     */
    @Override
    public boolean addUser(SysUser sysUser) {
        // 非空判断
        if (StringUtils.isBlank(sysUser.getUsername()) ||
                StringUtils.isBlank(sysUser.getPassword()) ||
                StringUtils.isBlank(sysUser.getName()) ||
                StringUtils.isBlank(sysUser.getPhone())
        ) {
            throw new RuntimeException("用户名、密码、姓名、手机不能为空");
        }

        // 检测用户名是否重复
        SysUser infoUserName = getUserInfoUserName(sysUser.getUsername());
        if (infoUserName != null) {
            if (infoUserName.getUsername().equals(sysUser.getUsername())) {
                throw new RuntimeException("用户名已存在");
            }

            // 检测手机号是否重复
            String phone = infoUserName.getPhone();
            if (phone != null) {
                if (phone.equals(sysUser.getPhone())) {
                    throw new RuntimeException("手机号已存在");
                }
            }
        }

        // 检测手机号是否等于11位
        if (sysUser.getPhone().length() != 11) {
            throw new RuntimeException("手机号必须为11位");
        }

        // 检测密码是否大于六位
        if (sysUser.getPassword().length() < 6) {
            throw new RuntimeException("密码不能小于六位");
        }

        //MD5加密密码
        String passwordWithMD5 = MD5Helper.md5(sysUser.getPassword());
        sysUser.setPassword(passwordWithMD5);
        if (sysUser.getHeadUrl() == null) {
            sysUser.setHeadUrl("http://file.lanxige.club/img/yq-third-lwmmm/Default/identicon.png");
        }

        return save(sysUser);
    }

    /**
     * 获取用户信息
     *
     * @param username 用户名
     * @return 用户信息
     */
    @Override
    public Map<String, Object> getUserLessInfo(String username) {

        Map<String, Object> map = new HashMap<>();
        SysUser sysUser = this.getUserInfoUserName(username);

        //当前权限控制使用不到，我们暂时忽略
        map.put("name", sysUser.getName());
        map.put("avatar", sysUser.getHeadUrl());
        map.put("description", sysUser.getDescription());
        map.put("status", sysUser.getStatus());
        map.put("roleList", sysUser.getRoleList());
        map.put("phone", sysUser.getPhone());
        map.put("username", sysUser.getUsername());
        map.put("creaT", sysUser.getCreateTime());
        map.put("updT", sysUser.getUpdateTime());
        map.put("id", sysUser.getId());

        return map;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean registerUser(SysUser sysUser) {

        // 1 校验用户名是否存在
        QueryWrapper<SysUser> wrapper = new QueryWrapper<>();
        wrapper.eq("username", sysUser.getUsername())
                .eq("is_deleted", 0);

        Integer count = sysUserMapper.selectCount(wrapper);

        if (count > 0) {
            throw new RuntimeException("用户名已存在");
        }

        // 2 设置用户默认信息
        sysUser.setStatus(1);
        sysUser.setName("用户"+UUID.randomUUID().toString().substring(0, 6));

        // 3 密码加密
        sysUser.setPassword(passwordEncoder.encode(sysUser.getPassword()));
        sysUser.setHeadUrl("http://file.lanxige.club/img/yq-third-lwmmm/Default/identicon.png");

        // 4 保存用户
        int insert;
        try {
            insert = sysUserMapper.insert(sysUser);
        } catch (DuplicateKeyException e) {
            throw new RuntimeException("用户名已存在");
        }

        if (insert <= 0) {
            throw new RuntimeException("注册失败");
        }

        // 5 查询 USER 角色
        QueryWrapper<SysRole> roleWrapper = new QueryWrapper<>();
        roleWrapper.eq("role_code", "USER")
                .eq("is_deleted", 0);

        SysRole role = sysRoleMapper.selectOne(roleWrapper);

        if (role == null) {
            throw new RuntimeException("默认角色USER不存在");
        }

        // 6 建立用户角色关系
        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(sysUser.getId());
        userRole.setRoleId(role.getId());

        sysUserRoleMapper.insert(userRole);

        return true;
    }

    @Override
    public List<UserInfoRsp> userWatchLog(String id) {
        return videoWatchLogMapper.getUserWatchLog(Long.valueOf(id));
    }

    @Override
    public List<UserInfoRsp> userLikeLog(String id) {
        return videoLikeLogMapper.getUserLikeLog(Long.valueOf(id));
    }
}
