package com.rent.managesystem.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rent.managesystem.entity.RepairTicket;
import com.rent.managesystem.mapper.RepairTicketMapper;
import com.rent.managesystem.service.RepairTicketService;
import org.springframework.stereotype.Service;

/**
 * 报修工单 Service 实现类
 *
 * @author 连梓祺 & 团队
 */
@Service
public class RepairTicketServiceImpl extends ServiceImpl<RepairTicketMapper, RepairTicket> implements RepairTicketService {
}
