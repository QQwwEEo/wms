package com.wms.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import java.math.BigDecimal;

/**
 * 物资信息 Excel 导入导出实体
 */
@Data
public class MaterialExcelVO {

    @ExcelProperty("物资名称")
    private String name;

    @ExcelProperty("物资编码")
    private String code;

    @ExcelProperty("物资类型")
    private String typeName;

    @ExcelProperty("所属仓库")
    private String warehouseName;

    @ExcelProperty("单位")
    private String unit;

    @ExcelProperty("单价(元)")
    private BigDecimal price;

    @ExcelProperty("最低预警值")
    private BigDecimal minStock;

    @ExcelProperty("备注")
    private String remark;
}
