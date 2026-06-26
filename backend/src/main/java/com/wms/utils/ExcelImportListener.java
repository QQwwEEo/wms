package com.wms.utils;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wms.entity.Material;
import com.wms.entity.MaterialType;
import com.wms.entity.Warehouse;
import com.wms.service.MaterialService;
import com.wms.service.MaterialTypeService;
import com.wms.service.WarehouseService;
import lombok.Getter;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;

/**
 * Excel 导入解析监听器，支持自定义列顺序和表头别名自适应匹配
 */
public class ExcelImportListener extends AnalysisEventListener<Map<Integer, String>> {

    private final MaterialService materialService;
    private final MaterialTypeService materialTypeService;
    private final WarehouseService warehouseService;

    @Getter
    private final List<String> errors = new ArrayList<>();
    @Getter
    private int successCount = 0;

    // 映射：Excel 列索引 -> 属性字段名称
    private final Map<Integer, String> headMap = new HashMap<>();

    // 基础数据缓存，避免每行都查数据库
    private final Map<String, Long> warehouseMap = new HashMap<>();
    private final Map<String, Long> typeMap = new HashMap<>();

    // 同义词匹配词典
    private static final Map<String, List<String>> SYNONYMS = new HashMap<>();

    static {
        SYNONYMS.put("name", Arrays.asList("物资名称", "名称", "物料名称", "物料名", "name", "material name", "materialname"));
        SYNONYMS.put("code", Arrays.asList("物资编码", "编码", "物资条码", "条码", "code", "material code", "materialcode"));
        SYNONYMS.put("typeName", Arrays.asList("物资类型", "类型", "物料类型", "类别", "type", "material type", "materialtype", "typename"));
        SYNONYMS.put("warehouseName", Arrays.asList("所属仓库", "仓库", "仓库名称", "warehouse", "warehouse name", "warehousename"));
        SYNONYMS.put("unit", Arrays.asList("单位", "计量单位", "unit"));
        SYNONYMS.put("price", Arrays.asList("单价(元)", "单价", "单价元", "价格", "price", "unit price", "unitprice"));
        SYNONYMS.put("minStock", Arrays.asList("最低预警值", "最低库存", "预警库存", "预警值", "min stock", "minstock", "min_stock"));
        SYNONYMS.put("remark", Arrays.asList("备注", "说明", "描述", "remark", "description", "comment"));
    }

    public ExcelImportListener(MaterialService materialService,
                               MaterialTypeService materialTypeService,
                               WarehouseService warehouseService) {
        this.materialService = materialService;
        this.materialTypeService = materialTypeService;
        this.warehouseService = warehouseService;

        // 初始化缓存
        try {
            List<Warehouse> warehouses = warehouseService.list(new LambdaQueryWrapper<Warehouse>().eq(Warehouse::getStatus, 1));
            for (Warehouse w : warehouses) {
                if (w.getName() != null) {
                    warehouseMap.put(w.getName().trim(), w.getId());
                }
            }

            List<MaterialType> types = materialTypeService.list(new LambdaQueryWrapper<MaterialType>().eq(MaterialType::getStatus, 1));
            for (MaterialType t : types) {
                if (t.getName() != null) {
                    typeMap.put(t.getName().trim(), t.getId());
                }
            }
        } catch (Exception e) {
            errors.add("初始化基础数据缓存失败：" + e.getMessage());
        }
    }

    @Override
    public void invokeHeadMap(Map<Integer, String> headMapData, AnalysisContext context) {
        // 解析第一行表头，根据同义词匹配列索引与字段键
        for (Map.Entry<Integer, String> entry : headMapData.entrySet()) {
            String val = entry.getValue();
            if (val == null) continue;
            String normalizedVal = val.trim().toLowerCase();

            boolean matched = false;
            for (Map.Entry<String, List<String>> synEntry : SYNONYMS.entrySet()) {
                for (String syn : synEntry.getValue()) {
                    if (syn.equalsIgnoreCase(normalizedVal)) {
                        headMap.put(entry.getKey(), synEntry.getKey());
                        matched = true;
                        break;
                    }
                }
                if (matched) break;
            }
        }
    }

    @Override
    public void invoke(Map<Integer, String> data, AnalysisContext context) {
        Integer rowNum = context.readRowHolder().getRowIndex() + 1;

        // 判断该行是否全为空，全为空则跳过
        boolean allEmpty = true;
        for (String val : data.values()) {
            if (StringUtils.hasText(val)) {
                allEmpty = false;
                break;
            }
        }
        if (allEmpty) return;

        // 匹配映射数据
        Map<String, String> fieldValues = new HashMap<>();
        for (Map.Entry<Integer, String> entry : data.entrySet()) {
            String fieldKey = headMap.get(entry.getKey());
            if (fieldKey != null) {
                fieldValues.put(fieldKey, entry.getValue());
            }
        }

        String name = fieldValues.get("name");
        String code = fieldValues.get("code");
        String typeName = fieldValues.get("typeName");
        String warehouseName = fieldValues.get("warehouseName");
        String unit = fieldValues.get("unit");
        String priceStr = fieldValues.get("price");
        String minStockStr = fieldValues.get("minStock");
        String remark = fieldValues.get("remark");

        // 1. 必填字段校验
        List<String> missingFields = new ArrayList<>();
        if (!StringUtils.hasText(name)) missingFields.add("物资名称");
        if (!StringUtils.hasText(code)) missingFields.add("物资编码");
        if (!StringUtils.hasText(typeName)) missingFields.add("物资类型");
        if (!StringUtils.hasText(warehouseName)) missingFields.add("所属仓库");

        if (!missingFields.isEmpty()) {
            errors.add(String.format("第 %d 行：必填项 [%s] 不能为空", rowNum, String.join(", ", missingFields)));
            return;
        }

        name = name.trim();
        code = code.trim();
        typeName = typeName.trim();
        warehouseName = warehouseName.trim();

        // 2. 校验仓库名称
        Long warehouseId = warehouseMap.get(warehouseName);
        if (warehouseId == null) {
            errors.add(String.format("第 %d 行：所属仓库 [%s] 在系统中不存在，请先创建该仓库", rowNum, warehouseName));
            return;
        }

        // 3. 解析或自动创建物资类型
        Long typeId = typeMap.get(typeName);
        if (typeId == null) {
            try {
                MaterialType newType = new MaterialType();
                newType.setName(typeName);
                newType.setCode("TYPE_" + (System.currentTimeMillis() + new Random().nextInt(1000)));
                newType.setStatus(1);
                newType.setRemark("Excel导入时自动创建");
                materialTypeService.save(newType);
                
                typeId = newType.getId();
                typeMap.put(typeName, typeId);
            } catch (Exception e) {
                errors.add(String.format("第 %d 行：自动创建物资类型 [%s] 失败：%s", rowNum, typeName, e.getMessage()));
                return;
            }
        }

        // 4. 安全解析数值
        BigDecimal price = BigDecimal.ZERO;
        if (StringUtils.hasText(priceStr)) {
            try {
                price = new BigDecimal(priceStr.trim());
                if (price.compareTo(BigDecimal.ZERO) < 0) {
                    errors.add(String.format("第 %d 行：单价 [%s] 不能为负数", rowNum, priceStr));
                    return;
                }
            } catch (NumberFormatException e) {
                errors.add(String.format("第 %d 行：单价 [%s] 格式不正确，必须为数字", rowNum, priceStr));
                return;
            }
        }

        BigDecimal minStock = BigDecimal.ZERO;
        if (StringUtils.hasText(minStockStr)) {
            try {
                minStock = new BigDecimal(minStockStr.trim());
                if (minStock.compareTo(BigDecimal.ZERO) < 0) {
                    errors.add(String.format("第 %d 行：最低预警值 [%s] 不能为负数", rowNum, minStockStr));
                    return;
                }
            } catch (NumberFormatException e) {
                errors.add(String.format("第 %d 行：最低预警值 [%s] 格式不正确，必须为数字", rowNum, minStockStr));
                return;
            }
        }

        // 5. 保存或更新物资数据（编码唯一）
        try {
            Material existing = materialService.getOne(new LambdaQueryWrapper<Material>()
                    .eq(Material::getCode, code));

            Material material = existing != null ? existing : new Material();
            material.setName(name);
            material.setCode(code);
            material.setTypeId(typeId);
            material.setTypeName(typeName);
            material.setWarehouseId(warehouseId);
            material.setWarehouseName(warehouseName);
            material.setUnit(unit != null ? unit.trim() : null);
            material.setPrice(price);
            material.setMinStock(minStock);
            material.setRemark(remark != null ? remark.trim() : null);
            material.setStatus(1);
            
            if (existing == null) {
                material.setQuantity(BigDecimal.ZERO);
            } else {
                material.setDeleted(0); // 确保如果之前被软删除，则在此处恢复
            }

            materialService.saveOrUpdate(material);
            successCount++;
        } catch (Exception e) {
            errors.add(String.format("第 %d 行：保存或更新物资数据失败：%s", rowNum, e.getMessage()));
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        // 完成解析后的收尾工作
    }
}
