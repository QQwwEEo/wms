package com.wms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.PageResult;
import com.wms.common.Result;
import com.wms.entity.MaterialType;
import com.wms.service.MaterialTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/materialType")
public class MaterialTypeController {

    @Autowired
    private MaterialTypeService materialTypeService;

    @GetMapping("/page")
    public Result<PageResult<MaterialType>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name) {
        LambdaQueryWrapper<MaterialType> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(name), MaterialType::getName, name);
        Page<MaterialType> page = materialTypeService.page(new Page<>(pageNum, pageSize), wrapper);
        return Result.success(PageResult.of(page.getTotal(), page.getRecords()));
    }

    @GetMapping("/list")
    public Result<List<MaterialType>> list() {
        return Result.success(materialTypeService.list(new LambdaQueryWrapper<MaterialType>().eq(MaterialType::getStatus, 1)));
    }

    @PostMapping
    public Result<Void> add(@RequestBody MaterialType type) {
        materialTypeService.save(type);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody MaterialType type) {
        materialTypeService.updateById(type);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        materialTypeService.removeById(id);
        return Result.success();
    }
}
