package com.wms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.PageResult;
import com.wms.common.Result;
import com.wms.entity.Material;
import com.wms.entity.Outbound;
import com.wms.service.MaterialService;
import com.wms.service.OutboundService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@RestController
@RequestMapping("/outbound")
public class OutboundController {

    @Autowired
    private OutboundService outboundService;

    @Autowired
    private MaterialService materialService;

    @GetMapping("/page")
    public Result<PageResult<Outbound>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String materialName) {
        LambdaQueryWrapper<Outbound> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(materialName), Outbound::getMaterialName, materialName);
        wrapper.orderByDesc(Outbound::getCreateTime);
        Page<Outbound> page = outboundService.page(new Page<>(pageNum, pageSize), wrapper);
        return Result.success(PageResult.of(page.getTotal(), page.getRecords()));
    }

    @PostMapping
    public Result<Void> add(@RequestBody Outbound outbound, HttpServletRequest request) {
        // 检查库存
        Material material = materialService.getById(outbound.getMaterialId());
        if (material == null || material.getQuantity().compareTo(outbound.getQuantity()) < 0) {
            return Result.error("库存不足");
        }
        String no = "OUT" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%03d", new Random().nextInt(999));
        outbound.setOutboundNo(no);
        Long userId = (Long) request.getAttribute("userId");
        String username = (String) request.getAttribute("username");
        outbound.setOperatorId(userId);
        outbound.setOperatorName(username);
        outbound.setOutboundTime(LocalDateTime.now());
        outboundService.save(outbound);
        // 减少库存
        material.setQuantity(material.getQuantity().subtract(outbound.getQuantity()));
        materialService.updateById(material);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        outboundService.removeById(id);
        return Result.success();
    }
}
