package com.rent.managesystem.util;

/**
 * 敏感数据脱敏工具类
 *
 * <p>用于对手机号、身份证号、姓名等敏感信息进行脱敏处理，
 * 防止在日志、响应中泄露用户隐私。
 *
 * <p><b>使用示例：</b>
 * <pre>{@code
 * // 手机号脱敏: 138****1111
 * DesensitizeUtil.phone("13800138000")
 *
 * // 身份证脱敏: 110101********1234
 * DesensitizeUtil.idCard("110101199001011234")
 *
 * // 姓名脱敏: 张*
 * DesensitizeUtil.name("张三")
 * }</pre>
 *
 * @author 连梓祺 & 团队
 */
public class DesensitizeUtil {

    private DesensitizeUtil() { /* 工具类禁止实例化 */ }

    /**
     * 手机号脱敏：保留前3位和后4位，中间用*替代
     *
     * <p>示例：13800138000 → 138****8000
     *
     * @param phone 原始手机号
     * @return 脱敏后的手机号，格式错误则原样返回
     */
    public static String phone(String phone) {
        if (phone == null || phone.length() != 11) return phone;
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    /**
     * 身份证号脱敏：保留前6位和后4位
     *
     * <p>示例：110101199001011234 → 110101********1234
     *
     * @param idCard 原始身份证号
     * @return 脱敏后的身份证号
     */
    public static String idCard(String idCard) {
        if (idCard == null || idCard.length() < 10) return idCard;
        int len = idCard.length();
        return idCard.substring(0, 6) + "*".repeat(Math.max(0, len - 10)) + idCard.substring(len - 4);
    }

    /**
     * 姓名脱敏：只显示姓氏，名字用*替代
     *
     * <p>示例：
     * <ul>
     *   <li>张三 → 张*</li>
     *   <li>欧阳锋 → 欧**</li>
     * </ul>
     *
     * @param name 原始姓名
     * @return 脱敏后的姓名
     */
    public static String name(String name) {
        if (name == null || name.isEmpty()) return name;
        if (name.length() <= 1) return "*";
        return name.charAt(0) + "*".repeat(name.length() - 1);
    }

    /**
     * 邮箱脱敏：@前的部分只显示首尾字符
     *
     * <p>示例：zhangsan@example.com → z***n@example.com
     *
     * @param email 原始邮箱
     * @return 脱敏后的邮箱
     */
    public static String email(String email) {
        if (email == null || !email.contains("@")) return email;
        int atIndex = email.indexOf("@");
        String local = email.substring(0, atIndex);
        String domain = email.substring(atIndex);
        if (local.length() <= 2) return "*" + domain;
        return local.charAt(0) + "*".repeat(local.length() - 2) + local.charAt(local.length() - 1) + domain;
    }
}
