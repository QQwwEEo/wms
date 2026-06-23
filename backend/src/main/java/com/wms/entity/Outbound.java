package com.wms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("sys_outbound")
public class Outbound {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String outboundNo;
    private Long materialId;
    private String materialName;
    private Long warehouseId;
    private String warehouseName;
    private BigDecimal quantity;
    private String receiver;
    private Long operatorId;
    private String operatorName;
    private LocalDateTime outboundTime;
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
