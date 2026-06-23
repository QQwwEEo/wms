package com.wms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.PageResult;
import com.wms.common.Result;
import com.wms.entity.Supplier;
import com.wms.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/supplier")
public class SupplierController {

    @Autowired
    private SupplierService supplierService;

    @GetMapping("/page")
    public Result<PageResult<Supplier>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name) {
        LambdaQueryWrapper<Supplier> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(name), Supplier::getName, name);
        wrapper.orderByDesc(Supplier::getCreateTime);
        Page<Supplier> page = supplierService.page(new Page<>(pageNum, pageSize), wrapper);
        return Result.success(PageResult.of(page.getTotal(), page.getRecords()));
    }

    @GetMapping("/list")
    public Result<List<Supplier>> list() {
        List<Supplier> list = supplierService.list(new LambdaQueryWrapper<Supplier>().eq(Supplier::getStatus, 1));
        return Result.success(list);
    }

    @PostMapping
    public Result<Void> add(@RequestBody Supplier supplier) {
        supplierService.save(supplier);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody Supplier supplier) {
        supplierService.updateById(supplier);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        supplierService.removeById(id);
        return Result.success();
    }
}
