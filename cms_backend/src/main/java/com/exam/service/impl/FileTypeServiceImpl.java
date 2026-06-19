package com.exam.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.exam.mapper.FileTypeMapper;
import com.exam.model.entity.FileType;
import com.exam.service.FileTypeService;
import org.springframework.stereotype.Service;

@Service
public class FileTypeServiceImpl extends ServiceImpl<FileTypeMapper, FileType> implements FileTypeService {
}
