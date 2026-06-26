package com.wms.controller;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.PageResult;
import com.wms.common.Result;
import com.wms.dto.MaterialExcelVO;
import com.wms.entity.Material;
import com.wms.service.MaterialService;
import com.wms.service.MaterialTypeService;
import com.wms.service.WarehouseService;
import com.wms.utils.ExcelImportListener;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/material")
public class MaterialController {

    @Autowired
    private MaterialService materialService;

    @Autowired
    private MaterialTypeService materialTypeService;

    @Autowired
    private WarehouseService warehouseService;

    @GetMapping("/page")
    public Result<PageResult<Material>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long typeId,
            @RequestParam(required = false) Long warehouseId) {
        LambdaQueryWrapper<Material> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(name), Material::getName, name);
        wrapper.eq(typeId != null, Material::getTypeId, typeId);
        wrapper.eq(warehouseId != null, Material::getWarehouseId, warehouseId);
        wrapper.orderByDesc(Material::getCreateTime);
        Page<Material> page = materialService.page(new Page<>(pageNum, pageSize), wrapper);
        return Result.success(PageResult.of(page.getTotal(), page.getRecords()));
    }

    @GetMapping("/list")
    public Result<List<Material>> list() {
        return Result.success(materialService.list(new LambdaQueryWrapper<Material>().eq(Material::getStatus, 1)));
    }

    @PostMapping
    public Result<Void> add(@RequestBody Material material) {
        materialService.save(material);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody Material material) {
        materialService.updateById(material);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        materialService.removeById(id);
        return Result.success();
    }

    /**
     * 下载物资导入模板
     */
    @GetMapping("/import/template")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("物资导入模板", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        List<MaterialExcelVO> list = new ArrayList<>();
        MaterialExcelVO demo = new MaterialExcelVO();
        demo.setName("电脑（示例数据）");
        demo.setCode("MAT-0001");
        demo.setTypeName("电子设备");
        demo.setWarehouseName("A区主仓库");
        demo.setUnit("台");
        demo.setPrice(new BigDecimal("5999.00"));
        demo.setMinStock(new BigDecimal("5"));
        demo.setRemark("仅供模板参考，导入前请删除或修改");
        list.add(demo);

        EasyExcel.write(response.getOutputStream(), MaterialExcelVO.class).sheet("物资列表").doWrite(list);
    }

    /**
     * 导入物资数据，支持自定义列顺序和表头别名
     */
    @PostMapping("/import")
    public Result<Map<String, Object>> importExcel(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }
        try {
            ExcelImportListener listener = new ExcelImportListener(materialService, materialTypeService, warehouseService);
            EasyExcel.read(file.getInputStream(), listener).sheet().doRead();

            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("successCount", listener.getSuccessCount());
            resultMap.put("errors", listener.getErrors());

            if (!listener.getErrors().isEmpty()) {
                return Result.success("部分数据解析校验失败，已跳过并输出错误列表", resultMap);
            }
            return Result.success("物资数据全部导入成功", resultMap);
        } catch (Exception e) {
            return Result.error("解析 Excel 失败：" + e.getMessage());
        }
    }

    /**
     * 根据查询条件导出物资数据
     */
    @GetMapping("/export")
    public void exportExcel(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long typeId,
            @RequestParam(required = false) Long warehouseId,
            HttpServletResponse response) throws IOException {

        LambdaQueryWrapper<Material> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(name), Material::getName, name);
        wrapper.eq(typeId != null, Material::getTypeId, typeId);
        wrapper.eq(warehouseId != null, Material::getWarehouseId, warehouseId);
        wrapper.orderByDesc(Material::getCreateTime);

        List<Material> list = materialService.list(wrapper);
        List<MaterialExcelVO> voList = new ArrayList<>();
        for (Material m : list) {
            MaterialExcelVO vo = new MaterialExcelVO();
            vo.setName(m.getName());
            vo.setCode(m.getCode());
            vo.setTypeName(m.getTypeName());
            vo.setWarehouseName(m.getWarehouseName());
            vo.setUnit(m.getUnit());
            vo.setPrice(m.getPrice());
            vo.setMinStock(m.getMinStock());
            vo.setRemark(m.getRemark());
            voList.add(vo);
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("物资导出列表", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        EasyExcel.write(response.getOutputStream(), MaterialExcelVO.class).sheet("物资列表").doWrite(voList);
    }
}

