package com.rent.managesystem.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rent.managesystem.common.ErrorCode;
import com.rent.managesystem.common.Result;
import com.rent.managesystem.dto.PageQuery;
import com.rent.managesystem.dto.PageResult;
import com.rent.managesystem.entity.PaymentOrder;
import com.rent.managesystem.entity.RentOrder;
import com.rent.managesystem.entity.UserInfo;
import com.rent.managesystem.service.OrderEmailService;
import com.rent.managesystem.service.PaymentOrderService;
import com.rent.managesystem.service.RentOrderService;
import com.rent.managesystem.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 支付订单控制器
 *
 * @author 连梓祺 & 团队
 */
@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentOrderController {

    private final PaymentOrderService paymentOrderService;
    private final UserService userService;
    private final RentOrderService rentOrderService;
    private final PasswordEncoder passwordEncoder;

    /** 邮件服务（可选注入 — 未配置SMTP时为null，不影响核心功能） */
    @Autowired(required = false)
    private OrderEmailService orderEmailService;

    /** 查询我的支付单列表 */
    @GetMapping("/orders")
    public Result<PageResult<PaymentOrder>> myOrders(PageQuery query) {
        Page<PaymentOrder> page = new Page<>(query.getPageNum(), query.getPageSize());
        Page<PaymentOrder> result = paymentOrderService.lambdaQuery()
                .eq(PaymentOrder::getPayerId, getCurrentUserId())
                .orderByDesc(PaymentOrder::getCreateTime)
                .page(page);
        return Result.success(PageResult.of(result));
    }

    /** 根据租房订单ID查询关联支付单 */
    @GetMapping("/order/by-rent/{rentOrderId}")
    public Result<PaymentOrder> getByRentOrder(@PathVariable String rentOrderId) {
        RentOrder rentOrder = rentOrderService.getById(rentOrderId);
        if (rentOrder == null || rentOrder.getPaymentOrderId() == null) {
            return Result.success(null);
        }
        PaymentOrder payment = paymentOrderService.getById(rentOrder.getPaymentOrderId());
        return Result.success(payment);
    }

    /** 支付（验证支付密码） */
    @PutMapping("/order/{orderId}/pay")
    public Result<PaymentOrder> pay(@PathVariable String orderId, @RequestBody Map<String, String> body) {
        String userId = getCurrentUserId();

        PaymentOrder order = paymentOrderService.getById(orderId);
        if (order == null || !order.getPayerId().equals(userId)) {
            return Result.error(ErrorCode.ORDER_NOT_FOUND);
        }
        if (order.getStatus() != 0) {
            return Result.error(ErrorCode.ORDER_STATUS_ERROR);
        }

        // 验证支付密码
        String password = body.get("password");
        UserInfo user = userService.getById(userId);
        if (user.getPaymentPassword() == null || user.getPaymentPassword().isEmpty()) {
            return Result.error(400, "请先在个人信息中设置支付密码");
        }
        if (password == null || !passwordEncoder.matches(password, user.getPaymentPassword())) {
            return Result.error(400, "支付密码错误");
        }

        // 标记支付成功
        order.setStatus(2); // 已支付
        order.setPaidAt(LocalDateTime.now());
        paymentOrderService.updateById(order);

        // 同步更新关联的租房订单为已完成
        RentOrder rentOrder = rentOrderService.lambdaQuery()
                .eq(RentOrder::getPaymentOrderId, orderId)
                .one();
        if (rentOrder != null && rentOrder.getStatus() == 1) {
            rentOrder.setStatus(2); // 已完成
            rentOrderService.updateById(rentOrder);
        }

        // 预留：异步发送支付成功邮件通知租客（无需等待结果）
        if (orderEmailService != null && rentOrder != null) {
            try {
                UserInfo tenant = userService.getById(order.getPayerId());
                orderEmailService.sendPaymentSuccessNotification(tenant, rentOrder, order);
            } catch (Exception e) {
                System.out.println("[邮件通知] 支付成功邮件发送失败（不影响支付流程）: " + e.getMessage());
            }
        }

        return Result.success(order);
    }

    private String getCurrentUserId() {
        return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
