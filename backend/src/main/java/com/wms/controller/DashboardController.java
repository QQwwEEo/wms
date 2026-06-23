package com.wms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wms.common.Result;
import com.wms.entity.*;
import com.wms.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired private UserService userService;
    @Autowired private MaterialService materialService;
    @Autowired private WarehouseService warehouseService;
    @Autowired private InboundService inboundService;
    @Autowired private OutboundService outboundService;
    @Autowired private ApplyService applyService;
    @Autowired private SupplierService supplierService;

    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        Map<String, Object> data = new HashMap<>();
        data.put("userCount", userService.count(new LambdaQueryWrapper<User>().eq(User::getDeleted, 0)));
        data.put("materialCount", materialService.count(new LambdaQueryWrapper<Material>().eq(Material::getDeleted, 0)));
        data.put("warehouseCount", warehouseService.count(new LambdaQueryWrapper<Warehouse>().eq(Warehouse::getDeleted, 0)));
        data.put("inboundCount", inboundService.count(new LambdaQueryWrapper<Inbound>().eq(Inbound::getDeleted, 0)));
        data.put("outboundCount", outboundService.count(new LambdaQueryWrapper<Outbound>().eq(Outbound::getDeleted, 0)));
        data.put("pendingApplyCount", applyService.count(new LambdaQueryWrapper<Apply>().eq(Apply::getStatus, "pending").eq(Apply::getDeleted, 0)));
        data.put("supplierCount", supplierService.count(new LambdaQueryWrapper<Supplier>().eq(Supplier::getDeleted, 0)));
        return Result.success(data);
    }
}
