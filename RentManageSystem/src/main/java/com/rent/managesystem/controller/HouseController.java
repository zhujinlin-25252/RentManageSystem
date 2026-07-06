package com.rent.managesystem.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rent.managesystem.common.Result;
import com.rent.managesystem.common.exception.BizException;
import com.rent.managesystem.common.ErrorCode;
import com.rent.managesystem.dto.PageQuery;
import com.rent.managesystem.dto.PageResult;
import com.rent.managesystem.entity.HouseInfo;
import com.rent.managesystem.entity.UserInfo;
import com.rent.managesystem.service.HouseService;
import com.rent.managesystem.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 房源管理控制器
 *
 * <p>处理房源发布、搜索、上下架等核心业务接口。
 * 公开浏览接口无需登录，发布/管理接口需要房东身份。
 *
 * @author 连梓祺 & 团队
 */
@RestController
@RequestMapping("/house")
@RequiredArgsConstructor
public class HouseController {

    private final HouseService houseService;
    private final UserService userService;

    // ==================== 公开接口（无需登录）====================

    /**
     * 分页查询已发布的房源列表（公开）
     *
     * <p>支持多条件筛选：
     * <ul>
     *   <li>city — 城市筛选</li>
     *   <li>district — 区域筛选</li>
     *   <li>minPrice/maxPrice — 价格区间</li>
     *   <li>keyword — 关键字模糊搜索（标题/描述/地址/城市/区域）</li>
     *   <li>roomCount — 户型筛选（N室+）</li>
     *   <li>sortBy — 排序（price_asc / price_desc）</li>
     * </ul>
     *
     * <pre>{@code GET /api/house/public/list?keyword=天河&roomCount=2&sortBy=price_asc&page=1&size=10}</pre>
     */
    @GetMapping("/public/list")
    public Result<PageResult<HouseInfo>> listHouses(PageQuery query,
                                                     @RequestParam(required = false) String city,
                                                     @RequestParam(required = false) String district,
                                                     @RequestParam(required = false) BigDecimal minPrice,
                                                     @RequestParam(required = false) BigDecimal maxPrice,
                                                     @RequestParam(required = false) Integer roomCount,
                                                     @RequestParam(required = false) String sortBy) {

        Page<HouseInfo> page = new Page<>(query.getPageNum(), query.getPageSize());

        Page<HouseInfo> resultPage = houseService.listPublishedHouses(page, city, district, minPrice, maxPrice,
                query.getKeyword(), roomCount, sortBy);

        return Result.success(PageResult.of(resultPage));
    }

    /**
     * 查看房源详情（公开）
     *
     * <pre>{@code GET /api/house/public/{houseId}}</pre>
     */
    @GetMapping("/public/{houseId}")
    public Result<HouseInfo> getHouseDetail(@PathVariable String houseId) {
        HouseInfo house = houseService.getById(houseId);
        if (house == null) {
            return Result.error(ErrorCode.HOUSE_NOT_FOUND);
        }
        // 已下架
        if (house.getStatus() == 3) {
            return Result.error(ErrorCode.HOUSE_OFF_SHELF);
        }
        // 仅已发布(1)和已出租(2)可公开查看，管理员可查看所有状态
        if (house.getStatus() != 1 && house.getStatus() != 2 && !isCurrentUserAdmin()) {
            return Result.error(ErrorCode.HOUSE_NOT_FOUND);
        }
        // 浏览次数 +1（可选：可用Redis计数避免频繁更新数据库）
        house.setViewCount(house.getViewCount() == null ? 1 : house.getViewCount() + 1);
        houseService.updateById(house);
        return Result.success(house);
    }

    /**
     * 判断当前请求是否为管理员（不抛异常，用于公开接口的权限放宽）
     */
    private boolean isCurrentUserAdmin() {
        try {
            UserInfo user = userService.getById(getCurrentUserId());
            return user != null && user.getRole() != null && user.getRole() == 2;
        } catch (Exception e) {
            return false;
        }
    }

    // ==================== 需要登录的接口 ====================

    /**
     * 房东发布房源
     *
     * <pre>{@code POST /api/house
     * Request: { "title": "精装修一室一厅", "city": "广州", ... }}</pre>
     */
    @PostMapping
    public Result<HouseInfo> publishHouse(@RequestBody HouseInfo house) {
        String landlordId = getCurrentUserId();

        house.setLandlordId(landlordId);
        house.setStatus(0);  // 新房源默认待审核
        house.setViewCount(0);
        house.setFavoriteCount(0);

        boolean saved = houseService.save(house);
        return saved ? Result.success(house) : Result.error(ErrorCode.SYSTEM_ERROR);
    }

    /**
     * 房东修改自己的房源信息
     *
     * <p>前端通过 {@code needReaudit} 标志告知后端内容是否发生了变更：
     * <ul>
     *   <li>{@code needReaudit=true} → 状态重置为 0(待审核)</li>
     *   <li>{@code needReaudit=false 或不传 → 保持原状态不变</li>
     * </ul>
     *
     * <pre>{@code PUT /api/house/{houseId}
     * Request: { "title": "...", "needReaudit": true }}</pre>
     */
    @PutMapping("/{houseId}")
    public Result<HouseInfo> updateHouse(@PathVariable String houseId,
                                         @RequestBody HouseInfo houseInfo) {
        HouseInfo existing = getMyHouse(houseId);

        // 只允许修改特定字段（防止恶意篡改状态等敏感数据）
        existing.setTitle(houseInfo.getTitle());
        existing.setDescription(houseInfo.getDescription());
        existing.setRentMonthly(houseInfo.getRentMonthly());
        existing.setDeposit(houseInfo.getDeposit());
        existing.setArea(houseInfo.getArea());
        existing.setRooms(houseInfo.getRooms());
        existing.setHalls(houseInfo.getHalls());
        existing.setBathrooms(houseInfo.getBathrooms());
        existing.setFloor(houseInfo.getFloor());
        existing.setOrientation(houseInfo.getOrientation());
        existing.setPaymentType(houseInfo.getPaymentType());
        existing.setFacilities(houseInfo.getFacilities());
        existing.setCoverImage(houseInfo.getCoverImage());

        // 前端检测到有修改时，重置为待审核状态
        if (Boolean.TRUE.equals(houseInfo.getNeedReaudit())) {
            existing.setStatus(0);
        }

        houseService.updateById(existing);
        return Result.success(existing);
    }

    /**
     * 房东下架房源
     *
     * <pre>{@code PUT /api/house/{houseId}/offshelf}</pre>
     */
    @PutMapping("/{houseId}/offshelf")
    public Result<Void> offShelfHouse(@PathVariable String houseId) {
        HouseInfo house = getMyHouse(houseId);
        // 只有已发布(1)或已出租(2)的房源才能下架
        if (house.getStatus() == 3) {
            throw new BizException(ErrorCode.HOUSE_ALREADY_OFFLINE);
        }
        house.setStatus(3);  // 下架状态
        houseService.updateById(house);
        return Result.success();
    }

    /**
     * 房东重新上架（重新提交审核）
     *
     * <p>只有已下架(status=3)的房源才能上架，上架后状态变为待审核(0)。
     *
     * <pre>{@code PUT /api/house/{houseId}/onshelf}</pre>
     */
    @PutMapping("/{houseId}/onshelf")
    public Result<Void> onShelfHouse(@PathVariable String houseId) {
        HouseInfo house = getMyHouse(houseId);
        if (house.getStatus() != 3) {
            throw new BizException(ErrorCode.HOUSE_NOT_OFFLINE);
        }
        house.setStatus(0);  // 重新进入待审核状态
        houseService.updateById(house);
        return Result.success();
    }

    /**
     * 房东删除自己的房源（物理删除）
     *
     * <p>只能删除已下架或待审核状态的房源，
     * 已发布/已出租的房源必须先下架才能删除。
     *
     * <pre>{@code DELETE /api/house/{houseId}}</pre>
     */
    @DeleteMapping("/{houseId}")
    public Result<Void> deleteHouse(@PathVariable String houseId) {
        HouseInfo house = getMyHouse(houseId);
        // 已发布(1)和已出租(2)的房源不能直接删除
        if (house.getStatus() == 1 || house.getStatus() == 2) {
            throw new BizException(ErrorCode.HOUSE_CANNOT_DELETE);
        }
        houseService.removeById(houseId);
        return Result.success();
    }

    /**
     * 获取当前房东的所有房源
     *
     * <pre>{@code GET /api/house/my}</pre>
     */
    @GetMapping("/my")
    public Result<PageResult<HouseInfo>> myHouses(PageQuery query) {
        String landlordId = getCurrentUserId();
        Page<HouseInfo> page = new Page<>(query.getPageNum(), query.getPageSize());

        Page<HouseInfo> result = houseService.lambdaQuery()
                .eq(HouseInfo::getLandlordId, landlordId)
                .orderByDesc(HouseInfo::getCreateTime)
                .page(page);

        return Result.success(PageResult.of(result));
    }

    /**
     * 房东获取自己的房源详情（用于编辑回填，不受公开状态限制）
     *
     * <pre>{@code GET /api/house/my/{houseId}}</pre>
     */
    @GetMapping("/my/{houseId}")
    public Result<HouseInfo> myHouseDetail(@PathVariable String houseId) {
        HouseInfo house = getMyHouse(houseId);
        return Result.success(house);
    }

    /**
     * 验证房源是否属于当前登录的房东
     */
    private HouseInfo getMyHouse(String houseId) {
        HouseInfo house = houseService.getById(houseId);
        if (house == null) {
            throw new BizException(ErrorCode.HOUSE_NOT_FOUND);
        }
        if (!house.getLandlordId().equals(getCurrentUserId())) {
            throw new BizException(ErrorCode.HOUSE_NO_PERMISSION);
        }
        return house;
    }

    // ==================== 管理员审核接口（需管理员权限）====================

    /**
     * 校验当前用户是否为管理员（role=2）
     */
    private void requireAdmin() {
        UserInfo user = userService.getById(getCurrentUserId());
        if (user == null || user.getRole() == null || user.getRole() != 2) {
            throw new BizException(ErrorCode.FORBIDDEN_NOT_ADMIN);
        }
    }

    /**
     * 获取待审核房源列表（管理员）
     *
     * <p>查询所有 status=0（待审核）的房源，支持分页。
     * 可选按城市筛选。
     *
     * <pre>{@code GET /api/house/admin/audit/list?page=1&size=10}</pre>
     */
    @GetMapping("/admin/audit/list")
    public Result<PageResult<HouseInfo>> listPendingAudit(PageQuery query,
                                                          @RequestParam(required = false) String city) {
        requireAdmin();

        Page<HouseInfo> page = new Page<>(query.getPageNum(), query.getPageSize());

        Page<HouseInfo> result = houseService.lambdaQuery()
                .eq(HouseInfo::getStatus, 0)
                .eq(city != null && !city.isEmpty(), HouseInfo::getCity, city)
                .orderByDesc(HouseInfo::getCreateTime)
                .page(page);

        return Result.success(PageResult.of(result));
    }

    /**
     * 审核通过 — 将待审核房源设为已发布（管理员）
     *
     * <p>状态流转：0(待审核) → 1(已发布)
     * <br>同时记录审核人和审核时间。
     *
     * <pre>{@code PUT /api/house/admin/{houseId}/approve}</pre>
     */
    @PutMapping("/admin/{houseId}/approve")
    public Result<Void> approveHouse(@PathVariable String houseId) {
        requireAdmin();

        HouseInfo house = houseService.getById(houseId);
        if (house == null) {
            throw new BizException(ErrorCode.HOUSE_NOT_FOUND);
        }
        if (house.getStatus() != 0) {
            throw new BizException(ErrorCode.HOUSE_NOT_PENDING_AUDIT);
        }

        // 更新状态为已发布，记录审核信息
        house.setStatus(1);
        house.setAuditUserId(getCurrentUserId());
        house.setAuditTime(LocalDateTime.now());
        house.setAuditRemark(null); // 通过时清空拒绝原因
        houseService.updateById(house);

        return Result.success();
    }

    /**
     * 审核拒绝 — 将待审核房源标记为审核拒绝（管理员）
     *
     * <p>状态流转：0(待审核) → 4(审核拒绝)
     * <br>必须填写拒绝原因，方便房东修改后重新提交。
     *
     * <pre>{@code PUT /api/house/admin/{houseId}/reject
     * Request: { "reason": "图片模糊，地址描述不完整" }}</pre>
     */
    @PutMapping("/admin/{houseId}/reject")
    public Result<Void> rejectHouse(@PathVariable String houseId,
                                    @RequestBody java.util.Map<String, String> body) {
        requireAdmin();

        HouseInfo house = houseService.getById(houseId);
        if (house == null) {
            throw new BizException(ErrorCode.HOUSE_NOT_FOUND);
        }
        if (house.getStatus() != 0) {
            throw new BizException(ErrorCode.HOUSE_NOT_PENDING_AUDIT);
        }

        String remark = body.get("remark");
        if (remark == null || remark.trim().isEmpty()) {
            throw new BizException(ErrorCode.PARAM_MISSING.getCode(), "请填写拒绝原因");
        }

        // 更新状态为审核拒绝，记录原因
        house.setStatus(4);
        house.setAuditUserId(getCurrentUserId());
        house.setAuditTime(LocalDateTime.now());
        house.setAuditRemark(remark.trim());
        houseService.updateById(house);

        return Result.success();
    }

    private String getCurrentUserId() {
        return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
