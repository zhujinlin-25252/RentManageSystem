package com.rent.managesystem.service;

import com.rent.managesystem.entity.HouseInfo;
import com.rent.managesystem.entity.PaymentOrder;
import com.rent.managesystem.entity.RentOrder;
import com.rent.managesystem.entity.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * 订单邮件通知服务（预留接口）
 *
 * <p>提供租房订单相关的邮件发送能力：
 * <ul>
 *   <li>新订单通知 — 租客下单后通知房东</li>
 *   <li>订单确认通知 — 房东确认后通知租客</li>
 *   <li>订单取消通知 — 任一方取消时通知对方</li>
 * </ul>
 *
 * <p><b>启用方式：</b>在 application.yml 中配置以下参数后自动激活：
 * <pre>
 * spring:
 *   mail:
 *     host: smtp.qq.com
 *     port: 465
 *     username: your@qq.com
 *     password: xxxxx（授权码，非QQ密码）
 *     properties:
 *       mail:
 *         ssl:
 *           enable: true
 * </pre>
 *
 * @author 连梓祺 & 团队
 */
@Service
@ConditionalOnProperty(name = "spring.mail.host") // 仅配置了邮件主机时才生效
public class OrderEmailService {

    @Autowired(required = false) // 未配置时为 null，不会报错
    private JavaMailSender mailSender;

    /**
     * 系统发件人地址，默认使用配置中的邮箱账号
     */
    @Value("${spring.mail.username:noreply@rent-system.com}")
    private String fromAddress;

    // ==================== 公开接口（Controller调用） ====================

    /**
     * 发送新订单通知给房东
     *
     * <p>租客提交租房订单后异步调用此方法，
     * 告知房东有新的租房申请需要处理。
     */
    public void sendNewOrderNotification(UserInfo landlord, UserInfo tenant,
                                          HouseInfo house, RentOrder order) {
        if (mailSender == null || landlord == null || landlord.getEmail() == null) {
            System.out.println("[邮件] 跳过新订单通知：邮件服务未配置或房东无邮箱");
            return;
        }

        String subject = "【青年品质租房】您有新的租房订单 - " + order.getOrderNo();
        String content = buildNewOrderEmail(landlord, tenant, house, order);

        sendHtmlMail(landlord.getEmail(), subject, content);
    }

    /**
     * 发送订单确认通知给租客
     *
     * <p>房东确认租房订单后，通知租客订单已被受理。
     */
    public void sendOrderConfirmNotification(UserInfo tenant, RentOrder order) {
        if (mailSender == null || tenant == null || tenant.getEmail() == null) {
            System.out.println("[邮件] 跳过确认通知：邮件服务未配置或租客无邮箱");
            return;
        }

        String subject = "【青年品质租房】您的租房订单已确认 - " + order.getOrderNo();
        String content = buildConfirmEmail(tenant, order);

        sendHtmlMail(tenant.getEmail(), subject, content);
    }

    /**
     * 发送订单取消通知
     *
     * <p>任一方取消订单时通知对方。
     */
    public void sendOrderCancelNotification(UserInfo receiver, RentOrder order, String cancelReason) {
        if (mailSender == null || receiver == null || receiver.getEmail() == null) {
            System.out.println("[邮件] 跳过取消通知：邮件服务未配置或接收方无邮箱");
            return;
        }

        String subject = "【青年品质租房】订单已取消 - " + order.getOrderNo();
        String content = buildCancelEmail(receiver, order, cancelReason);

        sendHtmlMail(receiver.getEmail(), subject, content);
    }

    /**
     * 发送支付成功通知给租客
     *
     * <p>租客完成支付后，将订单支付信息发送到其邮箱作为记录。
     */
    public void sendPaymentSuccessNotification(UserInfo tenant, RentOrder rentOrder, PaymentOrder paymentOrder) {
        if (mailSender == null || tenant == null || tenant.getEmail() == null) {
            System.out.println("[邮件] 跳过支付成功通知：邮件服务未配置或租客无邮箱");
            return;
        }

        String subject = "【青年品质租房】支付成功 - " + paymentOrder.getOrderNo();
        String content = buildPaymentSuccessEmail(tenant, rentOrder, paymentOrder);

        sendHtmlMail(tenant.getEmail(), subject, content);
    }

    // ==================== 邮件内容构建 ====================

    /** 构建新订单通知HTML邮件 */
    private String buildNewOrderEmail(UserInfo landlord, UserInfo tenant,
                                       HouseInfo house, RentOrder order) {
        return String.format("""
            <!DOCTYPE html>
            <html><body style="font-family:'Microsoft YaHei',sans-serif;padding:20px;background:#f5f5f5;">
            <div style="max-width:600px;margin:0 auto;background:#fff;border-radius:12px;overflow:hidden;box-shadow:0 2px 12px rgba(0,0,0,.08);">
              <div style="background:linear-gradient(135deg,#ff6b35,#ff9a56);padding:24px;color:#fff;">
                <h2 style="margin:0;">🏠 新的租房申请</h2>
                <p style="margin:8px 0 0;opacity:.9;">租客对您的房源发起了租房订单</p>
              </div>
              <div style="padding:24px;">
                <h3 style="color:#333;margin-top:0;">📋 订单信息</h3>
                <table style="width:100%%;border-collapse:collapse;font-size:14px;">
                  <tr><td style="padding:8px 0;border-bottom:1px solid #eee;color:#888;width:100px;">订单编号</td><td style="padding:8px 0;border-bottom:1px solid #eee;"><b>%s</b></td></tr>
                  <tr><td style="padding:8px 0;border-bottom:1px solid #eee;color:#888;">房源标题</td><td style="padding:8px 0;border-bottom:1px solid #eee;">%s</td></tr>
                  <tr><td style="padding:8px 0;border-bottom:1px solid #eee;color:#888;">租客姓名</td><td style="padding:8px 0;border-bottom:1px solid #eee;">%s</td></tr>
                  <tr><td style="padding:8px 0;border-bottom:1px solid #eee;color:#888;">联系电话</td><td style="padding:8px 0;border-bottom:1px solid #eee;">%s</td></tr>
                  <tr><td style="padding:8px 0;border-bottom:1px solid #eee;color:#888;">租期</td><td style="padding:8px 0;border-bottom:1px solid #eee;">%s 至 %s</td></tr>
                  <tr><td style="padding:8px 0;border-bottom:1px solid #eee;color:#888;">月租金 / 押金</td><td style="padding:8px 0;border-bottom:1px solid #eee;"><b style="color:#ff6b35">¥%s/月 · 押金 ¥%s</b></td></tr>
                  <tr><td style="padding:8px 0;border-bottom:1px solid #eee;color:#888;">总金额</td><td style="padding:8px 0;border-bottom:1px solid #eee;"><b style="color:#e74c3c;font-size:16px;">¥%s</b></td></tr>
                  %s
                </table>
                <div style="margin-top:20px;text-align:center;">
                  <a href="#" style="display:inline-block;padding:10px 28px;background:linear-gradient(135deg,#ff6b35,#ff9a56);color:#fff;border-radius:6px;text-decoration:none;font-weight:bold;">登录系统处理订单 →</a>
                </div>
                <p style="margin-top:20px;font-size:12px;color:#999;text-align:center;">
                  此邮件由系统自动发送，请勿直接回复。<br/>
                  如非本人操作，请忽略此邮件。
                </p>
              </div>
            </div></body></html>
            """,
            escapeHtml(order.getOrderNo()),
            escapeHtml(order.getTitle() != null ? order.getTitle() : ""),
            escapeHtml(tenant.getNickname() != null ? tenant.getNickname() : tenant.getRealName()),
            escapeHtml(order.getContactPhone() != null ? order.getContactPhone() : ""),
            order.getStartDate(),
            order.getEndDate(),
            order.getRentMonthly(),
            order.getDeposit(),
            order.getTotalAmount(),
            (order.getRemark() != null && !order.getRemark().isEmpty())
                    ? String.format("<tr><td style=\"padding:8px 0;border-bottom:1px solid #eee;color:#888;\">备注</td><td style=\"padding:8px 0;border-bottom:1px solid #eee;\">%s</td></tr>", escapeHtml(order.getRemark()))
                    : ""
        );
    }

    /** 构建确认通知HTML邮件 */
    private String buildConfirmEmail(UserInfo tenant, RentOrder order) {
        return String.format("""
            <!DOCTYPE html>
            <html><body style="font-family:'Microsoft YaHei',sans-serif;padding:20px;background:#f5f5f5;">
            <div style="max-width:600px;margin:0 auto;background:#fff;border-radius:12px;overflow:hidden;box-shadow:0 2px 12px rgba(0,0,0,.08);">
              <div style="background:linear-gradient(135deg,#27ae60,#2ecc71);padding:24px;color:#fff;">
                <h2 style="margin:0;">✅ 租房订单已确认</h2>
                <p style="margin:8px 0 0;opacity:.9;">恭喜！房东已确认您的租房申请</p>
              </div>
              <div style="padding:24px;">
                <h3 style="color:#333;margin-top:0;">📋 订单详情</h3>
                <table style="width:100%%;border-collapse:collapse;font-size:14px;">
                  <tr><td style="padding:8px 0;border-bottom:1px solid #eee;color:#888;width:100px;">订单编号</td><td style="padding:8px 0;border-bottom:1px solid #eee;"><b>%s</b></td></tr>
                  <tr><td style="padding:8px 0;border-bottom:1px solid #eee;color:#888;">房源</td><td style="padding:8px 0;border-bottom:1px solid #eee;">%s</td></tr>
                  <tr><td style="padding:8px 0;border-bottom:1px solid #eee;color:#888;">租期</td><td style="padding:8px 0;border-bottom:1px solid #eee;">%s 至 %s</td></tr>
                  <tr><td style="padding:8px 0;border-bottom:1px solid #eee;color:#888;">总金额</td><td style="padding:8px 0;border-bottom:1px solid #eee;"><b style="color:#27ae60;font-size:16px;">¥%s</b></td></tr>
                </table>
                <p style="margin-top:16px;color:#666;font-size:14px;">请尽快与房东联系，约定签约和入住事宜。</p>
              </div>
            </div></body></html>
            """,
            escapeHtml(order.getOrderNo()),
            escapeHtml(order.getTitle() != null ? order.getTitle() : ""),
            order.getStartDate(),
            order.getEndDate(),
            order.getTotalAmount()
        );
    }

    /** 构建取消通知HTML邮件 */
    private String buildCancelEmail(UserInfo receiver, RentOrder order, String reason) {
        return String.format("""
            <!DOCTYPE html>
            <html><body style="font-family:'Microsoft YaHei',sans-serif;padding:20px;background:#f5f5f5;">
            <div style="max-width:600px;margin:0 auto;background:#fff;border-radius:12px;overflow:hidden;box-shadow:0 2px 12px rgba(0,0,0,.08);">
              <div style="background:linear-gradient(135deg,#95a5a6,#7f8c8d);padding:24px;color:#fff;">
                <h2 style="margin:0;">❌ 订单已取消</h2>
                <p style="margin:8px 0 0;opacity:.9;">租房订单已被取消</p>
              </div>
              <div style="padding:24px;">
                <h3 style="color:#333;margin-top:0;">订单信息</h3>
                <p style="font-size:14px;color:#666;">订单编号：<b>%s</b></p>
                <p style="font-size:14px;color:#666;">房源：%s</p>
                <p style="font-size:14px;color:#e74c3c;">原因：%s</p>
              </div>
            </div></body></html>
            """,
            escapeHtml(order.getOrderNo()),
            escapeHtml(order.getTitle() != null ? order.getTitle() : ""),
            escapeHtml(reason != null ? reason : "未说明")
        );
    }

    /** 构建支付成功通知HTML邮件 */
    private String buildPaymentSuccessEmail(UserInfo tenant, RentOrder rentOrder, PaymentOrder paymentOrder) {
        String typeLabel = switch (paymentOrder.getOrderType() != null ? paymentOrder.getOrderType() : 1) {
            case 1 -> "押金 + 首月租金";
            case 2 -> "租金";
            case 3 -> "物业费";
            case 4 -> "水电费";
            case 5 -> "其他费用";
            default -> "押金 + 租金";
        };
        return String.format("""
            <!DOCTYPE html>
            <html><body style="font-family:'Microsoft YaHei',sans-serif;padding:20px;background:#f5f5f5;">
            <div style="max-width:600px;margin:0 auto;background:#fff;border-radius:12px;overflow:hidden;box-shadow:0 2px 12px rgba(0,0,0,.08);">
              <div style="background:linear-gradient(135deg,#27ae60,#2ecc71);padding:24px;color:#fff;">
                <h2 style="margin:0;">✅ 支付成功</h2>
                <p style="margin:8px 0 0;opacity:.9;">您的租房订单已支付成功，请妥善保存此记录</p>
              </div>
              <div style="padding:24px;">
                <h3 style="color:#333;margin-top:0;">📋 支付详情</h3>
                <table style="width:100%%;border-collapse:collapse;font-size:14px;">
                  <tr><td style="padding:8px 0;border-bottom:1px solid #eee;color:#888;width:100px;">支付单号</td><td style="padding:8px 0;border-bottom:1px solid #eee;"><b>%s</b></td></tr>
                  <tr><td style="padding:8px 0;border-bottom:1px solid #eee;color:#888;">租房订单</td><td style="padding:8px 0;border-bottom:1px solid #eee;">%s</td></tr>
                  <tr><td style="padding:8px 0;border-bottom:1px solid #eee;color:#888;">房源</td><td style="padding:8px 0;border-bottom:1px solid #eee;">%s</td></tr>
                  <tr><td style="padding:8px 0;border-bottom:1px solid #eee;color:#888;">费用类型</td><td style="padding:8px 0;border-bottom:1px solid #eee;">%s</td></tr>
                  <tr><td style="padding:8px 0;border-bottom:1px solid #eee;color:#888;">支付金额</td><td style="padding:8px 0;border-bottom:1px solid #eee;"><b style="color:#27ae60;font-size:16px;">¥%s</b></td></tr>
                  <tr><td style="padding:8px 0;border-bottom:1px solid #eee;color:#888;">支付时间</td><td style="padding:8px 0;border-bottom:1px solid #eee;">%s</td></tr>
                  <tr><td style="padding:8px 0;border-bottom:1px solid #eee;color:#888;">租期</td><td style="padding:8px 0;border-bottom:1px solid #eee;">%s 至 %s</td></tr>
                </table>
                <p style="margin-top:20px;font-size:12px;color:#999;text-align:center;">
                  此邮件由系统自动发送，请勿直接回复。<br/>
                  如非本人操作，请立即联系平台客服。
                </p>
              </div>
            </div></body></html>
            """,
            escapeHtml(paymentOrder.getOrderNo()),
            escapeHtml(rentOrder.getOrderNo() != null ? rentOrder.getOrderNo() : ""),
            escapeHtml(rentOrder.getTitle() != null ? rentOrder.getTitle() : ""),
            escapeHtml(typeLabel),
            paymentOrder.getAmount() != null ? paymentOrder.getAmount().toString() : "0.00",
            paymentOrder.getPaidAt() != null ? paymentOrder.getPaidAt().toString() : "",
            rentOrder.getStartDate(),
            rentOrder.getEndDate()
        );
    }

    // ==================== 底层发送 ====================

    /**
     * 发送HTML格式邮件
     */
    private void sendHtmlMail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true = HTML格式
            mailSender.send(message);
            System.out.println("[邮件] ✅ 发送成功 -> " + to + " | " + subject);
        } catch (MessagingException e) {
            System.err.println("[邮件] ❌ 发送失败 -> " + to + " | 错误: " + e.getMessage());
            throw new RuntimeException("邮件发送失败", e);
        }
    }

    /**
     * HTML转义防XSS
     */
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
}
