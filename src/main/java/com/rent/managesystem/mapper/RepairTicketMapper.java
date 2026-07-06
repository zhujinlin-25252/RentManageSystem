package com.rent.managesystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rent.managesystem.entity.RepairTicket;
import org.apache.ibatis.annotations.Mapper;

/**
 * 报修工单 Mapper
 *
 * @author 连梓祺 & 团队
 */
@Mapper
public interface RepairTicketMapper extends BaseMapper<RepairTicket> {
}
