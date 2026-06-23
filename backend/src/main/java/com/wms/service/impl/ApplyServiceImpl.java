package com.wms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.entity.Apply;
import com.wms.mapper.ApplyMapper;
import com.wms.service.ApplyService;
import org.springframework.stereotype.Service;

@Service
public class ApplyServiceImpl extends ServiceImpl<ApplyMapper, Apply> implements ApplyService {}
