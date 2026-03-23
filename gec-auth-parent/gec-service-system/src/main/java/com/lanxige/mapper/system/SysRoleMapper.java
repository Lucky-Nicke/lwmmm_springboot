package com.lanxige.mapper.system;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lanxige.model.system.SysRole;
import com.lanxige.model.vo.SysRoleQueryVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {
    IPage<SysRole> selectPage(IPage<SysRole> page1, @Param("vo") SysRoleQueryVo roleQueryVo);

    /**
     * 查询角色通过用户id
     *
     * @param userId 用户id
     * @return 角色列表
     */
    List<SysRole> selectRoleListByUserId(Long userId);
}
