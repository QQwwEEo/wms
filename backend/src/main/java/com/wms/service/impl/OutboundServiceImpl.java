package com.wms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.entity.Outbound;
import com.wms.mapper.OutboundMapper;
import com.wms.service.OutboundService;
import org.springframework.stereotype.Service;

@Service
public class OutboundServiceImpl extends ServiceImpl<OutboundMapper, Outbound> implements OutboundService {}
