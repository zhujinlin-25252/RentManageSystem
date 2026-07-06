package com.rent.managesystem.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rent.managesystem.entity.OperationLog;
import com.rent.managesystem.mapper.OperationLogMapper;
import com.rent.managesystem.service.OperationLogService;
import org.springframework.stereotype.Service;

/**
 * 操作日志 Service 实现类
 *
 * @author 连梓祺 & 团队
 */
@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog> implements OperationLogService {
}
