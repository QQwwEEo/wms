package com.wms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.PageResult;
import com.wms.common.Result;
import com.wms.entity.Apply;
import com.wms.entity.Material;
import com.wms.entity.Outbound;
import com.wms.service.ApplyService;
import com.wms.service.MaterialService;
import com.wms.service.OutboundService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/apply")
public class ApplyController {

    @Autowired
    private ApplyService applyService;

    @Autowired
    private MaterialService materialService;

    @Autowired
    private OutboundService outboundService;

    @GetMapping("/page")
    public Result<PageResult<Apply>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String materialName,
            @RequestParam(required = false) String status,
            HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        Long userId = (Long) request.getAttribute("userId");
        LambdaQueryWrapper<Apply> wrapper = new LambdaQueryWrapper<>();
        // 普通用户只能看自己的申请
        if ("user".equals(role)) {
            wrapper.eq(Apply::getUserId, userId);
        }
        wrapper.like(StringUtils.hasText(materialName), Apply::getMaterialName, materialName);
        wrapper.eq(StringUtils.hasText(status), Apply::getStatus, status);
        wrapper.orderByDesc(Apply::getCreateTime);
        Page<Apply> page = applyService.page(new Page<>(pageNum, pageSize), wrapper);
        return Result.success(PageResult.of(page.getTotal(), page.getRecords()));
    }

    @PostMapping
    public Result<Void> add(@RequestBody Apply apply, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String username = (String) request.getAttribute("username");
        String no = "AP" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%03d", new Random().nextInt(999));
        apply.setApplyNo(no);
        apply.setUserId(userId);
        apply.setUserName(username);
        apply.setStatus("pending");
        applyService.save(apply);
        return Result.success();
    }

    @PutMapping("/approve/{id}")
    public Result<Void> approve(@PathVariable Long id, @RequestBody Map<String, String> body, HttpServletRequest request) {
        Apply apply = applyService.getById(id);
        if (apply == null) {
            return Result.error("申请不存在");
        }
        String newStatus = body.get("status");
        if ("approved".equals(newStatus)) {
            // 验证库存并联动扣减
            Material material = materialService.getById(apply.getMaterialId());
            if (material == null) {
                return Result.error("物资不存在");
            }
            if (material.getQuantity().compareTo(apply.getQuantity()) < 0) {
                return Result.error("库存不足，无法审批通过");
            }
            
            // 扣除库存
            material.setQuantity(material.getQuantity().subtract(apply.getQuantity()));
            materialService.updateById(material);
            
            // 自动生成出库记录
            Outbound outbound = new Outbound();
            String no = "OUT" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                    + String.format("%03d", new Random().nextInt(999));
            outbound.setOutboundNo(no);
            outbound.setMaterialId(apply.getMaterialId());
            outbound.setMaterialName(apply.getMaterialName());
            outbound.setWarehouseId(material.getWarehouseId());
            outbound.setWarehouseName(material.getWarehouseName());
            outbound.setQuantity(apply.getQuantity());
            outbound.setReceiver(apply.getUserName());
            
            Long operatorId = (Long) request.getAttribute("userId");
            String operatorName = (String) request.getAttribute("username");
            outbound.setOperatorId(operatorId);
            outbound.setOperatorName(operatorName);
            outbound.setOutboundTime(LocalDateTime.now());
            outbound.setRemark("物资申请审批自动出库，申请单号：" + apply.getApplyNo());
            outboundService.save(outbound);
        }
        apply.setStatus(newStatus);
        apply.setApproveRemark(body.get("approveRemark"));
        apply.setApproveTime(LocalDateTime.now());
        applyService.updateById(apply);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        applyService.removeById(id);
        return Result.success();
    }
}
