package com.wms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.PageResult;
import com.wms.common.Result;
import com.wms.entity.Inbound;
import com.wms.entity.Material;
import com.wms.service.InboundService;
import com.wms.service.MaterialService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@RestController
@RequestMapping("/inbound")
public class InboundController {

    @Autowired
    private InboundService inboundService;

    @Autowired
    private MaterialService materialService;

    @GetMapping("/page")
    public Result<PageResult<Inbound>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String materialName) {
        LambdaQueryWrapper<Inbound> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(materialName), Inbound::getMaterialName, materialName);
        wrapper.orderByDesc(Inbound::getCreateTime);
        Page<Inbound> page = inboundService.page(new Page<>(pageNum, pageSize), wrapper);
        return Result.success(PageResult.of(page.getTotal(), page.getRecords()));
    }

    @PostMapping
    public Result<Void> add(@RequestBody Inbound inbound, HttpServletRequest request) {
        // 生成入库单号
        String no = "IN" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%03d", new Random().nextInt(999));
        inbound.setInboundNo(no);
        // 设置操作员
        Long userId = (Long) request.getAttribute("userId");
        String username = (String) request.getAttribute("username");
        inbound.setOperatorId(userId);
        inbound.setOperatorName(username);
        inbound.setInboundTime(LocalDateTime.now());
        // 计算总金额
        if (inbound.getPrice() != null && inbound.getQuantity() != null) {
            inbound.setTotalAmount(inbound.getPrice().multiply(inbound.getQuantity()));
        }
        inboundService.save(inbound);
        // 更新物资库存
        Material material = materialService.getById(inbound.getMaterialId());
        if (material != null) {
            material.setQuantity(material.getQuantity().add(inbound.getQuantity()));
            materialService.updateById(material);
        }
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        inboundService.removeById(id);
        return Result.success();
    }
}
