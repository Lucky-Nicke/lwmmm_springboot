package com.lanxige.model.system;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lanxige.model.base.BaseEntity;
import io.swagger.annotations.ApiModel;
import lombok.Data;

//影视
@Data
@ApiModel(description = "影视")
@TableName("sys_movie")
public class SysMovie extends BaseEntity {

//    @TableId(type = IdType.AUTO)
//    private Integer id;//主键

    private String name;//影视名

    private String cid;//所属栏目

    private String description;//描述

    private String director;//导演

    private String isApproval;//审核状态

    private String image;//缩略图

    private String playId;//播放id

    @TableField(exist = false)//不存储到数据库,仅仅用于前台数据展示
    private String playAuth;//播放秘钥
}
