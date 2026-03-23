package com.lanxige.Req;

import io.swagger.annotations.ApiModel;
import lombok.Data;

//影视
@Data
@ApiModel(description = "影视")
public class AddSysMovieReq{

    private String name;//影视名

    private String cid;//所属栏目

    private String description;//描述

    private String username;//导演

    private String image;//缩略图

    private String playId;//播放id
}
