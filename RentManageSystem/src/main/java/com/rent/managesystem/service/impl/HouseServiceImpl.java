package com.rent.managesystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rent.managesystem.entity.HouseInfo;
import com.rent.managesystem.mapper.HouseMapper;
import com.rent.managesystem.service.HouseService;
import org.springframework.stereotype.Service;

/**
 * 房源 Service 实现类
 *
 * <p>核心业务：房源发布、审核、搜索、上下架等。
 *
 * @author 连梓祺 & 团队
 */
@Service
public class HouseServiceImpl extends ServiceImpl<HouseMapper, HouseInfo> implements HouseService {

    /**
     * 分页查询已发布的房源列表
     * 只展示 status=1（已发布）且未逻辑删除的房源
     */
    @Override
    public Page<HouseInfo> listPublishedHouses(Page<HouseInfo> page,
                                               String city, String district,
                                               java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice,
                                               String keyword, Integer roomCount, String sortBy) {
        LambdaQueryWrapper<HouseInfo> wrapper = new LambdaQueryWrapper<>();

        // 只查已发布的房源
        wrapper.eq(HouseInfo::getStatus, 1);

        // 按条件筛选（非空才加条件，避免全表扫描）
        if (city != null && !city.isEmpty()) {
            wrapper.eq(HouseInfo::getCity, city);
        }
        if (district != null && !district.isEmpty()) {
            wrapper.eq(HouseInfo::getDistrict, district);
        }
        if (minPrice != null) {
            wrapper.ge(HouseInfo::getRentMonthly, minPrice);
        }
        if (maxPrice != null) {
            wrapper.le(HouseInfo::getRentMonthly, maxPrice);
        }

        // 关键字模糊搜索：标题、描述、地址、城市、区域
        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.trim();
            wrapper.and(w -> w
                    .like(HouseInfo::getTitle, kw)
                    .or().like(HouseInfo::getDescription, kw)
                    .or().like(HouseInfo::getAddress, kw)
                    .or().like(HouseInfo::getCity, kw)
                    .or().like(HouseInfo::getDistrict, kw));
        }

        // 户型筛选："N室+"语义，如选3室则匹配 rooms >= 3
        if (roomCount != null && roomCount > 0) {
            wrapper.ge(HouseInfo::getRooms, roomCount);
        }

        // 动态排序
        if ("price_asc".equals(sortBy)) {
            wrapper.orderByAsc(HouseInfo::getRentMonthly);
        } else if ("price_desc".equals(sortBy)) {
            wrapper.orderByDesc(HouseInfo::getRentMonthly);
        } else {
            wrapper.orderByDesc(HouseInfo::getCreateTime);
        }

        return page(page, wrapper);
    }
}
