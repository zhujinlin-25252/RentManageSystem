package com.rent.managesystem.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rent.managesystem.entity.ContractInfo;
import com.rent.managesystem.mapper.ContractMapper;
import com.rent.managesystem.service.ContractService;
import org.springframework.stereotype.Service;

/**
 * 合同 Service 实现类
 *
 * @author 连梓祺 & 团队
 */
@Service
public class ContractServiceImpl extends ServiceImpl<ContractMapper, ContractInfo> implements ContractService {
}
