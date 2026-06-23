package com.wms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.PageResult;
import com.wms.common.Result;
import com.wms.entity.Material;
import com.wms.service.MaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/material")
public class MaterialController {

    @Autowired
    private MaterialService materialService;

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
}
