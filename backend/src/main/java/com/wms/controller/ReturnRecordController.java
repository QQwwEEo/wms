package com.wms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.PageResult;
import com.wms.common.Result;
import com.wms.entity.ReturnRecord;
import com.wms.entity.Material;
import com.wms.entity.Inbound;
import com.wms.service.ReturnRecordService;
import com.wms.service.MaterialService;
import com.wms.service.InboundService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/returnRecord")
public class ReturnRecordController {

    @Autowired
    private ReturnRecordService returnRecordService;

    @Autowired
    private MaterialService materialService;

    @Autowired
    private InboundService inboundService;

    @GetMapping("/page")
    public Result<PageResult<ReturnRecord>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String materialName,
            @RequestParam(required = false) String status,
            HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        Long userId = (Long) request.getAttribute("userId");
        LambdaQueryWrapper<ReturnRecord> wrapper = new LambdaQueryWrapper<>();
        if ("user".equals(role)) {
            wrapper.eq(ReturnRecord::getUserId, userId);
        }
        wrapper.like(StringUtils.hasText(materialName), ReturnRecord::getMaterialName, materialName);
        wrapper.eq(StringUtils.hasText(status), ReturnRecord::getStatus, status);
        wrapper.orderByDesc(ReturnRecord::getCreateTime);
        Page<ReturnRecord> page = returnRecordService.page(new Page<>(pageNum, pageSize), wrapper);
        return Result.success(PageResult.of(page.getTotal(), page.getRecords()));
    }

    @PostMapping
    public Result<Void> add(@RequestBody ReturnRecord record, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String username = (String) request.getAttribute("username");
        String no = "RT" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%03d", new Random().nextInt(999));
        record.setReturnNo(no);
        record.setUserId(userId);
        record.setUserName(username);
        record.setStatus("pending");
        returnRecordService.save(record);
        return Result.success();
    }

    @PutMapping("/approve/{id}")
    public Result<Void> approve(@PathVariable Long id, @RequestBody Map<String, String> body, HttpServletRequest request) {
        ReturnRecord record = returnRecordService.getById(id);
        if (record == null) {
            return Result.error("归还记录不存在");
        }
        String newStatus = body.get("status");
        if ("approved".equals(newStatus)) {
            // 自动回加库存并联动入库
            Material material = materialService.getById(record.getMaterialId());
            if (material == null) {
                return Result.error("物资不存在");
            }
            
            // 回加库存
            material.setQuantity(material.getQuantity().add(record.getQuantity()));
            materialService.updateById(material);
            
            // 自动生成入库记录
            Inbound inbound = new Inbound();
            String no = "IN" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                    + String.format("%03d", new Random().nextInt(999));
            inbound.setInboundNo(no);
            inbound.setMaterialId(record.getMaterialId());
            inbound.setMaterialName(record.getMaterialName());
            inbound.setWarehouseId(material.getWarehouseId());
            inbound.setWarehouseName(material.getWarehouseName());
            inbound.setQuantity(record.getQuantity());
            
            if (material.getPrice() != null) {
                inbound.setPrice(material.getPrice());
                inbound.setTotalAmount(material.getPrice().multiply(record.getQuantity()));
            }
            
            Long operatorId = (Long) request.getAttribute("userId");
            String operatorName = (String) request.getAttribute("username");
            inbound.setOperatorId(operatorId);
            inbound.setOperatorName(operatorName);
            inbound.setInboundTime(LocalDateTime.now());
            inbound.setRemark("物资归还审批自动入库，归还单号：" + record.getReturnNo());
            inboundService.save(inbound);
        }
        record.setStatus(newStatus);
        record.setApproveRemark(body.get("approveRemark"));
        record.setApproveTime(LocalDateTime.now());
        returnRecordService.updateById(record);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        returnRecordService.removeById(id);
        return Result.success();
    }
}
