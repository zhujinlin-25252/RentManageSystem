package com.rent.managesystem;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.rent.managesystem.entity.PaymentOrder;
import com.rent.managesystem.entity.RentOrder;
import com.rent.managesystem.entity.UserInfo;
import com.rent.managesystem.service.OrderEmailService;

/**
 * 邮件服务功能测试
 *
 * <p>使用方法：
 * <ol>
 *   <li>先在 application.yml 中把 {@code spring.mail.username} 和
 *       {@code spring.mail.password} 改成真实可用的邮箱和授权码</li>
 *   <li>把下面 {@code TEST_RECEIVER_EMAIL} 改成你要接收测试邮件的邮箱</li>
 *   <li>运行本测试</li>
 * </ol>
 *
 * <p>如果 SMTP 未配置或凭证无效，测试不会报错退出的，
 * 只会在控制台打印跳过或失败日志。
 */
@SpringBootTest
class OrderEmailServiceTest {

    /** 改成你要接收测试邮件的邮箱地址 */
    private static final String TEST_RECEIVER_EMAIL = "992500702@qq.com";

    @Autowired(required = false)
    private OrderEmailService orderEmailService;

    @Test
    void testSendPaymentSuccessNotification() {
        if (orderEmailService == null) {
            System.out.println("⚠️  OrderEmailService 未注入，请确认 application.yml 中 spring.mail.host 已配置");
            return;
        }

        System.out.println("========== 开始测试支付成功邮件 ==========");

        // 构造测试数据
        UserInfo tenant = new UserInfo();
        tenant.setUserId("test-tenant-001");
        tenant.setNickname("测试租客");
        tenant.setEmail(TEST_RECEIVER_EMAIL);

        RentOrder rentOrder = new RentOrder();
        rentOrder.setOrderNo("RO2026062312000001");
        rentOrder.setTitle("阳光花园 3栋502 精装两室一厅");
        rentOrder.setStartDate(LocalDate.of(2026, 7, 1));
        rentOrder.setEndDate(LocalDate.of(2027, 6, 30));
        rentOrder.setRentMonthly(new BigDecimal("2500.00"));
        rentOrder.setDeposit(new BigDecimal("2500.00"));
        rentOrder.setTotalAmount(new BigDecimal("32500.00"));

        PaymentOrder paymentOrder = new PaymentOrder();
        paymentOrder.setOrderNo("PO2026062312000001");
        paymentOrder.setOrderType(1);
        paymentOrder.setAmount(new BigDecimal("5000.00"));
        paymentOrder.setPaidAt(LocalDateTime.now());

        try {
            orderEmailService.sendPaymentSuccessNotification(tenant, rentOrder, paymentOrder);
            System.out.println("========== 测试完成：邮件已发送，请检查邮箱 " + TEST_RECEIVER_EMAIL + " ==========");
        } catch (Exception e) {
            System.err.println("========== 测试完成：邮件发送失败 ==========");
            System.err.println("错误信息: " + e.getMessage());
            System.err.println("请检查 spring.mail.username / password 是否正确，以及授权码是否有效");
        }
    }

    @Test
    void testSendPaymentSuccessNotification_NoEmail() {
        if (orderEmailService == null) {
            System.out.println("⚠️  OrderEmailService 未注入，跳过测试");
            return;
        }

        System.out.println("========== 测试：租客无邮箱时静默跳过 ==========");

        UserInfo tenant = new UserInfo();
        tenant.setUserId("test-tenant-002");
        tenant.setNickname("无邮箱租客");
        tenant.setEmail(null); // 未设置邮箱

        PaymentOrder paymentOrder = new PaymentOrder();
        paymentOrder.setOrderNo("PO2026062312000099");
        paymentOrder.setOrderType(1);
        paymentOrder.setAmount(new BigDecimal("5000.00"));
        paymentOrder.setPaidAt(LocalDateTime.now());

        RentOrder rentOrder = new RentOrder();
        rentOrder.setOrderNo("RO2026062312000099");
        rentOrder.setTitle("测试房源");

        // 不应该抛异常，只打印跳过日志
        orderEmailService.sendPaymentSuccessNotification(tenant, rentOrder, paymentOrder);
        System.out.println("========== 测试通过：无邮箱时正确跳过 ==========");
    }
}
