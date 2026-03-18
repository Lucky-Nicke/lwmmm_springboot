package com.lanxige.model.video;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 视频审核记录表
 */
@Data
@TableName("video_appro")
public class VideoAppro implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id; // 审核序号

    private Long videoId; // 影视id

    private String approStatus; // 审核状态（DOING/FAIL/SUCC）

    private String approBy; // 审核id

    private Date approTime; // 审核时间

    private String approDesc; // 审核意见

    private String uploadBy; // 上传人

    private LocalDateTime createTime; // 创建时间

    private LocalDateTime updateTime; // 更新时间
}