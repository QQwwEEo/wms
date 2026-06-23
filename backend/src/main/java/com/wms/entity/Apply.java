package com.wms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("sys_apply")
public class Apply {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String applyNo;
    private Long materialId;
    private String materialName;
    private BigDecimal quantity;
    private String applyReason;
    private Long userId;
    private String userName;
    private String status;  // pending / approved / rejected
    private String approveRemark;
    private LocalDateTime approveTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
