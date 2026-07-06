package com.rent.managesystem.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rent.managesystem.entity.ExpenseRecord;
import com.rent.managesystem.mapper.ExpenseRecordMapper;
import com.rent.managesystem.service.ExpenseRecordService;
import org.springframework.stereotype.Service;

/**
 * 费用记录 Service 实现类
 *
 * @author 连梓祺 & 团队
 */
@Service
public class ExpenseRecordServiceImpl extends ServiceImpl<ExpenseRecordMapper, ExpenseRecord> implements ExpenseRecordService {
}
