package com.wms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("sys_inbound")
public class Inbound {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String inboundNo;
    private Long materialId;
    private String materialName;
    private Long warehouseId;
    private String warehouseName;
    private Long supplierId;
    private String supplierName;
    private BigDecimal quantity;
    private BigDecimal price;
    private BigDecimal totalAmount;
    private Long operatorId;
    private String operatorName;
    private LocalDateTime inboundTime;
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
