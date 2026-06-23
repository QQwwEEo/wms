package com.wms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.entity.ReturnRecord;
import com.wms.mapper.ReturnRecordMapper;
import com.wms.service.ReturnRecordService;
import org.springframework.stereotype.Service;

@Service
public class ReturnRecordServiceImpl extends ServiceImpl<ReturnRecordMapper, ReturnRecord> implements ReturnRecordService {}
