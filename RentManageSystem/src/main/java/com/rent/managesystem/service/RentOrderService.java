package com.rent.managesystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rent.managesystem.entity.RentOrder;

/**
 * 租房订单 Service 接口
 *
 * @author 连梓祺 & 团队
 */
public interface RentOrderService extends IService<RentOrder> {

    /**
     * 分页查询某租客的订单列表
     *
     * @param page     分页参数
     * @param tenantId 租客ID
     * @return 分页结果
     */
    Page<RentOrder> listByTenantId(Page<RentOrder> page, String tenantId);

    /**
     * 分页查询某房东收到的订单列表
     *
     * @param page       分页参数
     * @param landlordId 房东ID
     * @return 分页结果
     */
    Page<RentOrder> listByLandlordId(Page<RentOrder> page, String landlordId);
}
