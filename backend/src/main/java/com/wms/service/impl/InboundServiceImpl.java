package com.wms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.entity.Inbound;
import com.wms.mapper.InboundMapper;
import com.wms.service.InboundService;
import org.springframework.stereotype.Service;

@Service
public class InboundServiceImpl extends ServiceImpl<InboundMapper, Inbound> implements InboundService {}
