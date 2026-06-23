package com.wms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("sys_material")
public class Material {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String code;
    private Long typeId;
    private String typeName;
    private Long warehouseId;
    private String warehouseName;
    private String unit;
    private BigDecimal quantity;
    private BigDecimal price;
    @TableField("min_stock")
    private BigDecimal minStock;
    private String image;
    private String remark;
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
