package com.rent.managesystem.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rent.managesystem.entity.RentOrder;
import com.rent.managesystem.mapper.RentOrderMapper;
import com.rent.managesystem.service.RentOrderService;
import org.springframework.stereotype.Service;

/**
 * 租房订单 Service 实现类
 *
 * @author 连梓祺 & 团队
 */
@Service
public class RentOrderServiceImpl extends ServiceImpl<RentOrderMapper, RentOrder> implements RentOrderService {

    @Override
    public Page<RentOrder> listByTenantId(Page<RentOrder> page, String tenantId) {
        return lambdaQuery()
                .eq(RentOrder::getTenantId, tenantId)
                .orderByDesc(RentOrder::getCreateTime)
                .page(page);
    }

    @Override
    public Page<RentOrder> listByLandlordId(Page<RentOrder> page, String landlordId) {
        return lambdaQuery()
                .eq(RentOrder::getLandlordId, landlordId)
                .orderByDesc(RentOrder::getCreateTime)
                .page(page);
    }
}
