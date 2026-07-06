package com.rent.managesystem.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rent.managesystem.common.Result;
import com.rent.managesystem.common.exception.BizException;
import com.rent.managesystem.common.ErrorCode;
import com.rent.managesystem.dto.PageQuery;
import com.rent.managesystem.dto.PageResult;
import com.rent.managesystem.entity.HouseInfo;
import com.rent.managesystem.entity.PaymentOrder;
import com.rent.managesystem.entity.RentOrder;
import com.rent.managesystem.entity.UserInfo;
import com.rent.managesystem.service.HouseService;
import com.rent.managesystem.service.OrderEmailService;
import com.rent.managesystem.service.PaymentOrderService;
import com.rent.managesystem.service.RentOrderService;
import com.rent.managesystem.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 租房订单控制器
 *
 * <p>处理租客下单、订单列表、房东确认/取消等核心业务。
 *
 * @author 连梓祺 & 团队
 */
@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final RentOrderService rentOrderService;
    private final HouseService houseService;
    private final UserService userService;
    private final PaymentOrderService paymentOrderService;
    
    /** 邮件服务（可选注入 — 未配置SMTP时为null，不影响核心功能） */
    @Autowired(required = false)
    private OrderEmailService orderEmailService;

    /** 订单编号生成器计数器（简单实现，生产环境建议用Redis序列） */
    private static int orderSequence = 0;
    private static final DateTimeFormatter ORDER_NO_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    // ==================== 租客接口 ====================

    /**
     * 租客发起租房订单
     *
     * <p>租客在房源详情页点击"立即租房"后调用此接口：
     * <ul>
     *   <li>校验房源是否存在且为已发布状态</li>
     *   <li>自动计算总金额 = 押金 + 月租金 × 月数</li>
     *   <li>生成唯一订单编号</li>
     *   <li>预留发送邮件通知房东（可后续配置SMTP后启用）</li>
     * </ul>
     *
     * <pre>{@code POST /api/order
     * Request: {
     *   "houseId": "xxx",
     *   "startDate": "2026-06-01",
     *   "endDate": "2026-12-01",
     *   "contactName": "张三",
     *   "contactPhone": "13800138000",
     *   "remark": "希望尽快入住"
     * }}</pre>
     */
    @PostMapping
    public Result<RentOrder> createOrder(@RequestBody RentOrder order) {
        String tenantId = getCurrentUserId();

        // 1. 校验房源
        HouseInfo house = houseService.getById(order.getHouseId());
        if (house == null || house.getStatus() != 1) {
            throw new BizException(ErrorCode.HOUSE_NOT_FOUND);
        }

        // 2. 日期校验
        if (order.getStartDate() == null || order.getEndDate() == null) {
            throw new BizException(ErrorCode.PARAM_MISSING.getCode(), "请选择租期起止日期");
        }
        if (!order.getEndDate().isAfter(order.getStartDate())) {
            throw new BizException(ErrorCode.CONTRACT_DATE_INVALID);
        }

        // 3. 计算月数和总金额
        long months = java.time.temporal.ChronoUnit.MONTHS.between(
                order.getStartDate().withDayOfMonth(1),
                order.getEndDate().withDayOfMonth(1));
        // 至少按1个月算
        int monthCount = Math.max(1, (int) months);

        BigDecimal rentMonthly = house.getRentMonthly() != null ? house.getRentMonthly() : BigDecimal.ZERO;
        BigDecimal deposit = house.getDeposit() != null ? house.getDeposit() : BigDecimal.ZERO;
        BigDecimal totalAmount = deposit.add(rentMonthly.multiply(BigDecimal.valueOf(monthCount)));

        // 4. 生成订单编号
        synchronized (OrderController.class) {
            orderSequence++;
        }
        String orderNo = "RO" + LocalDateTime.now().format(ORDER_NO_FMT) + String.format("%04d", orderSequence % 10000);

        // 5. 构建订单对象
        RentOrder rentOrder = new RentOrder();
        rentOrder.setOrderNo(orderNo);
        rentOrder.setHouseId(order.getHouseId());
        rentOrder.setTenantId(tenantId);
        rentOrder.setLandlordId(house.getLandlordId());
        rentOrder.setTitle(house.getTitle());
        rentOrder.setRentMonthly(rentMonthly);
        rentOrder.setDeposit(deposit);
        rentOrder.setStartDate(order.getStartDate());
        rentOrder.setEndDate(order.getEndDate());
        rentOrder.setTotalAmount(totalAmount);
        rentOrder.setContactName(order.getContactName());
        rentOrder.setContactPhone(order.getContactPhone());
        rentOrder.setRemark(order.getRemark());
        rentOrder.setStatus(0); // 待确认

        // 6. 保存订单
        boolean saved = rentOrderService.save(rentOrder);
        if (!saved) {
            throw new BizException(ErrorCode.SYSTEM_ERROR);
        }

        // 7. 预留：异步发送邮件通知房东（无需等待结果）
        if (orderEmailService != null) {
            try {
                UserInfo landlord = userService.getById(house.getLandlordId());
                UserInfo tenantUser = userService.getById(tenantId);
                orderEmailService.sendNewOrderNotification(landlord, tenantUser, house, rentOrder);
            } catch (Exception e) {
                // 邮件发送失败不影响主流程，仅记录日志
                System.out.println("[邮件通知] 新订单邮件发送失败（不影响订单创建）: " + e.getMessage());
            }
        }

        return Result.success(rentOrder);
    }

    /**
     * 租客查看自己的订单列表
     *
     * <pre>{@code GET /api/order/my?page=1&size=10}</pre>
     */
    @GetMapping("/my")
    public Result<PageResult<RentOrder>> myOrders(PageQuery query) {
        String tenantId = getCurrentUserId();
        Page<RentOrder> page = new Page<>(query.getPageNum(), query.getPageSize());
        Page<RentOrder> result = rentOrderService.listByTenantId(page, tenantId);
        return Result.success(PageResult.of(result));
    }

    /**
     * 租客取消订单（仅待确认状态可取消）
     *
     * <pre>{@code PUT /api/order/{orderId}/cancel}</pre>
     */
    @PutMapping("/{orderId}/cancel")
    public Result<Void> cancelOrder(@PathVariable String orderId) {
        String tenantId = getCurrentUserId();
        RentOrder order = getMyOrder(orderId, tenantId);

        if (order.getStatus() != 0) {
            throw new BizException(ErrorCode.ORDER_STATUS_ERROR);
        }
        order.setStatus(3); // 已取消
        rentOrderService.updateById(order);

        return Result.success();
    }

    // ==================== 房东接口 ====================

    /**
     * 房东确认租房订单（待确认 → 已确认）
     *
     * <p>房东确认后房源状态变为已出租，同时自动生成支付单供租客支付。
     *
     * <pre>{@code PUT /api/order/{orderId}/confirm}</pre>
     */
    @PutMapping("/{orderId}/confirm")
    public Result<Void> confirmOrder(@PathVariable String orderId) {
        String landlordId = getCurrentUserId();
        RentOrder order = getOrderForLandlord(orderId, landlordId);

        if (order.getStatus() != 0) {
            throw new BizException(ErrorCode.ORDER_STATUS_ERROR);
        }

        // 更新订单状态
        order.setStatus(1); // 已确认
        rentOrderService.updateById(order);

        // 更新房源状态为已出租
        HouseInfo house = houseService.getById(order.getHouseId());
        if (house != null && house.getStatus() == 1) {
            house.setStatus(2); // 已出租
            houseService.updateById(house);
        }

        // 自动生成支付单（押金 + 首月租金）
        PaymentOrder payment = new PaymentOrder();
        String payOrderNo = "PO" + LocalDateTime.now().format(ORDER_NO_FMT)
                + String.format("%04d", (int) (Math.random() * 10000));
        payment.setOrderNo(payOrderNo);
        payment.setPayerId(order.getTenantId());
        payment.setPayeeId(order.getLandlordId());
        payment.setOrderType(1); // 押金+租金
        BigDecimal payDeposit = order.getDeposit() != null ? order.getDeposit() : BigDecimal.ZERO;
        BigDecimal payRent = order.getRentMonthly() != null ? order.getRentMonthly() : BigDecimal.ZERO;
        payment.setAmount(payDeposit.add(payRent));
        payment.setStatus(0); // 待支付
        payment.setExpireAt(LocalDateTime.now().plusHours(24));
        paymentOrderService.save(payment);

        // 关联支付单到租房订单
        order.setPaymentOrderId(payment.getOrderId());
        rentOrderService.updateById(order);

        return Result.success();
    }

    /**
     * 房东拒绝租房订单（待确认 → 已拒绝）
     *
     * <pre>{@code PUT /api/order/{orderId}/reject}</pre>
     */
    @PutMapping("/{orderId}/reject")
    public Result<Void> rejectOrder(@PathVariable String orderId) {
        String landlordId = getCurrentUserId();
        RentOrder order = getOrderForLandlord(orderId, landlordId);

        if (order.getStatus() != 0) {
            throw new BizException(ErrorCode.ORDER_STATUS_ERROR);
        }

        order.setStatus(4); // 房东已拒绝
        rentOrderService.updateById(order);

        return Result.success();
    }

    /**
     * 房东查看收到的订单列表
     *
     * <pre>{@code GET /api/order/received?page=1&size=10}</pre>
     */
    @GetMapping("/received")
    public Result<PageResult<RentOrder>> receivedOrders(PageQuery query) {
        String landlordId = getCurrentUserId();
        Page<RentOrder> page = new Page<>(query.getPageNum(), query.getPageSize());
        Page<RentOrder> result = rentOrderService.listByLandlordId(page, landlordId);
        return Result.success(PageResult.of(result));
    }

    // ==================== 内部方法 ====================

    /**
     * 获取并验证订单归属（租客）
     */
    private RentOrder getMyOrder(String orderId, String tenantId) {
        RentOrder order = rentOrderService.getById(orderId);
        if (order == null) {
            throw new BizException(ErrorCode.ORDER_NOT_FOUND);
        }
        if (!order.getTenantId().equals(tenantId)) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }
        return order;
    }

    /**
     * 获取并验证订单归属（房东）
     */
    private RentOrder getOrderForLandlord(String orderId, String landlordId) {
        RentOrder order = rentOrderService.getById(orderId);
        if (order == null) {
            throw new BizException(ErrorCode.ORDER_NOT_FOUND);
        }
        if (!order.getLandlordId().equals(landlordId)) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }
        return order;
    }

    private String getCurrentUserId() {
        return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
