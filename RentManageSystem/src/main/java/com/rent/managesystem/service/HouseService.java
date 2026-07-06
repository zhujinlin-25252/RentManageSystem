package com.rent.managesystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rent.managesystem.entity.HouseInfo;

/**
 * 房源 Service 接口
 *
 * @author 连梓祺 & 团队
 */
public interface HouseService extends IService<HouseInfo> {

    /**
     * 分页查询已发布房源（公开接口）
     * 支持按城市/区域/价格区间/关键字/户型筛选及排序
     *
     * @param page     分页参数
     * @param city     城市（可选）
     * @param district 区域（可选）
     * @param minPrice 最低租金（可选）
     * @param maxPrice 最高租金（可选）
     * @param keyword  关键字模糊搜索（可选，匹配标题/描述/地址/城市/区域）
     * @param roomCount 最小室数（可选，"N室+"语义）
     * @param sortBy   排序方式（price_asc/price_desc，默认最新发布）
     * @return 分页结果
     */
    Page<HouseInfo> listPublishedHouses(Page<HouseInfo> page,
                                        String city, String district,
                                        java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice,
                                        String keyword, Integer roomCount, String sortBy);
}
