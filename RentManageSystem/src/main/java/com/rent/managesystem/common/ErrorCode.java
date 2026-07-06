package com.rent.managesystem.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 全局错误码枚举定义
 *
 * <p>统一管理系统中所有业务异常的错误码和提示信息。
 * 错误码规则：
 * <ul>
 *   <li>1xx：参数校验相关错误</li>
 *   <li>2xx：用户认证与权限相关错误</li>
 *   <li>3xx：房源业务相关错误</li>
 *   <li>4xx：合同业务相关错误</li>
 *   <li>5xx：支付与订单相关错误</li>
 *   <li>9xx：系统内部错误</li>
 * </ul>
 *
 * <p><b>新增错误码时请遵循以下规范：</b>
 * <ol>
 *   <li>在对应的分类区域添加新枚举值</li>
 *   <li>错误码不重复</li>
 *   <li>message 使用中文，面向最终用户友好</li>
 * </ol>
 *
 * @author 连梓祺 & 团队
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    // ==================== 通用成功 ====================
    SUCCESS(200, "操作成功"),

    // ==================== 1xx 参数校验错误 ==================== PARAM_*

    /** 请求参数不能为空 */
    PARAM_MISSING(101, "请求参数不能为空"),
    /** 参数格式不合法 */
    PARAM_INVALID(102, "参数格式不合法"),
    /** 手机号格式不正确 */
    PHONE_INVALID(103, "手机号格式不正确"),
    /** 密码格式不符合要求（8-20位，包含字母和数字） */
    PASSWORD_INVALID(104, "密码需8-20位，且包含字母和数字"),
    /** 身份证号格式不正确 */
    ID_CARD_INVALID(105, "身份证号格式不正确"),
    /** 分页参数非法（page/size超出范围） */
    PAGE_INVALID(106, "分页参数非法"),

    // ==================== 2xx 用户认证与权限错误 ==================== USER_*

    /** 用户名或密码错误 */
    LOGIN_FAILED(201, "用户名或密码错误"),
    /** 账户已被禁用 */
    USER_DISABLED(202, "账户已被禁用，请联系管理员"),
    /** 账户已被锁定（多次登录失败） */
    USER_LOCKED(203, "账户已被锁定，请30分钟后重试"),
    /** Token已过期，需要重新登录 */
    TOKEN_EXPIRED(204, "登录已过期，请重新登录"),
    /** Token无效（被篡改或伪造） */
    TOKEN_INVALID(205, "无效的登录凭证"),
    /** 未登录或Token缺失 */
    NOT_LOGIN(206, "请先登录"),
    /** 无权限访问该资源 */
    FORBIDDEN(207, "没有权限执行此操作"),
    /** 该手机号已注册 */
    PHONE_ALREADY_EXISTS(208, "该手机号已被注册"),
    /** 用户不存在 */
    USER_NOT_FOUND(209, "用户不存在"),
    /** 原密码错误 */
    OLD_PASSWORD_WRONG(210, "原密码输入错误"),
    /** 验证码错误或已过期 */
    VERIFY_CODE_ERROR(211, "验证码错误或已过期"),
    /** 验证码发送过于频繁 */
    VERIFY_CODE_FREQ_LIMIT(212, "验证码发送过于频繁，请60秒后再试"),

    // ==================== 3xx 房源业务错误 ==================== HOUSE_*

    /** 房源不存在 */
    HOUSE_NOT_FOUND(301, "房源不存在"),
    /** 房源状态不允许此操作（如已下架的房源无法编辑） */
    HOUSE_STATUS_ERROR(302, "当前房源状态不允许此操作"),
    /** 没有该房源的操作权限 */
    HOUSE_NO_PERMISSION(303, "您没有该房源的操作权限"),
    /** 房源图片数量超限（最多9张） */
    HOUSE_IMAGE_LIMIT(304, "房源图片最多上传9张"),
    /** 房源已处于下架状态 */
    HOUSE_ALREADY_OFFLINE(305, "房源已下架，无需重复操作"),
    /** 只有已下架的房源才能重新上架 */
    HOUSE_NOT_OFFLINE(306, "只有已下架的房源才能重新上架"),
    /** 已发布/已出租的房源需先下架才能删除 */
    HOUSE_CANNOT_DELETE(307, "请先下架该房源后再删除"),
    /** 房源状态不是待审核，无法执行审核操作 */
    HOUSE_NOT_PENDING_AUDIT(308, "该房源当前不在待审核状态"),
    /** 房源已下架 */
    HOUSE_OFF_SHELF(310, "该房源已下架"),
    /** 无管理员权限 */
    FORBIDDEN_NOT_ADMIN(309, "需要管理员权限才能执行此操作"),

    // ==================== 4xx 合同业务错误 ==================== CONTRACT_*

    /** 合同不存在 */
    CONTRACT_NOT_FOUND(401, "合同不存在"),
    /** 合同状态不允许此操作 */
    CONTRACT_STATUS_ERROR(402, "当前合同状态不允许此操作"),
    /** 房源已被租出，无法创建合同 */
    HOUSE_ALREADY_RENTED(403, "该房源已被租出，无法签约"),
    /** 合同日期范围不合法 */
    CONTRACT_DATE_INVALID(404, "合同日期范围不合法"),

    // ==================== 5xx 支付与订单错误 ==================== PAYMENT_*

    /** 订单不存在 */
    ORDER_NOT_FOUND(501, "订单不存在"),

    // ==================== 6xx 报修工单错误 ==================== TICKET_*

    /** 工单不存在 */
    TICKET_NOT_FOUND(601, "工单不存在"),
    /** 工单状态不允许此操作 */
    TICKET_STATUS_ERROR(602, "当前工单状态不允许此操作"),
    /** 无权操作该工单 */
    TICKET_NO_PERMISSION(603, "您没有权限操作此工单"),
    /** 订单状态不允许此操作 */
    ORDER_STATUS_ERROR(502, "当前订单状态不允许此操作"),
    /** 余额不足 */
    BALANCE_NOT_ENOUGH(503, "账户余额不足"),
    /** 支付金额不匹配 */
    PAY_AMOUNT_MISMATCH(504, "支付金额不一致"),

    // ==================== 9xx 系统内部错误 ==================== SYS_*

    /** 系统繁忙，请稍后重试 */
    SYSTEM_ERROR(901, "系统繁忙，请稍后重试"),
    /** 数据库操作失败 */
    DB_ERROR(902, "数据操作失败，请稍后重试"),
    /** 文件上传失败 */
    FILE_UPLOAD_ERROR(903, "文件上传失败"),
    /** 文件类型不支持 */
    FILE_TYPE_NOT_SUPPORT(904, "不支持该文件格式");

    /** 错误码（整数） */
    private final int code;

    /** 错误提示信息（面向用户的中文描述） */
    private final String message;

}
