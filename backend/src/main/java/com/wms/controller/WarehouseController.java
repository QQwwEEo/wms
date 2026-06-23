package com.wms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.PageResult;
import com.wms.common.Result;
import com.wms.entity.Warehouse;
import com.wms.service.WarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/warehouse")
public class WarehouseController {

    @Autowired
    private WarehouseService warehouseService;

    @GetMapping("/page")
    public Result<PageResult<Warehouse>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name) {
        LambdaQueryWrapper<Warehouse> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(name), Warehouse::getName, name);
        wrapper.orderByDesc(Warehouse::getCreateTime);
        Page<Warehouse> page = warehouseService.page(new Page<>(pageNum, pageSize), wrapper);
        return Result.success(PageResult.of(page.getTotal(), page.getRecords()));
    }

    @GetMapping("/list")
    public Result<List<Warehouse>> list() {
        List<Warehouse> list = warehouseService.list(new LambdaQueryWrapper<Warehouse>().eq(Warehouse::getStatus, 1));
        return Result.success(list);
    }

    @PostMapping
    public Result<Void> add(@RequestBody Warehouse warehouse) {
        warehouseService.save(warehouse);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody Warehouse warehouse) {
        warehouseService.updateById(warehouse);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        warehouseService.removeById(id);
        return Result.success();
    }
}
