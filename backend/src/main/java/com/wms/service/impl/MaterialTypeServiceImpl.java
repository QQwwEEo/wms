package com.wms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.entity.MaterialType;
import com.wms.mapper.MaterialTypeMapper;
import com.wms.service.MaterialTypeService;
import org.springframework.stereotype.Service;

@Service
public class MaterialTypeServiceImpl extends ServiceImpl<MaterialTypeMapper, MaterialType> implements MaterialTypeService {}
