package com.rent.managesystem.common.exception;

import com.rent.managesystem.common.ErrorCode;
import lombok.Getter;

/**
 * 业务自定义异常
 *
 * <p>当业务逻辑中出现预期内的错误情况时，抛出此异常。
 * 由 {@link com.rent.managesystem.common.exception.GlobalExceptionHandler} 统一捕获并返回给前端。
 *
 * <p><b>何时使用：</b>
 * <ul>
 *   <li>用户不存在 → throw new BizException(ErrorCode.USER_NOT_FOUND)</li>
 *   <li>余额不足 → throw new BizException(ErrorCode.BALANCE_NOT_ENOUGH)</li>
 *   <li>房源已下架 → throw new BizException(ErrorCode.HOUSE_STATUS_ERROR)</li>
 * </ul>
 *
 * <p><b>何时不使用：</b>
 * <ul>
 *   <li>参数校验失败 → 用 @Valid/@Validated 注解自动拦截</li>
 *   <li>数据库连接失败等系统级错误 → 不应捕获，交给全局异常兜底</li>
 * </ul>
 *
 * @author 连梓祺 & 团队
 */
@Getter
public class BizException extends RuntimeException {

    /** 序列化版本号 */
    private static final long serialVersionUID = 1L;

    /** 业务错误码（对应 ErrorCode 枚举中的值） */
    private final int code;

    /**
     * 使用预定义的错误码构造异常
     *
     * @param errorCode 错误码枚举
     */
    public BizException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    /**
     * 使用自定义错误码和消息构造异常
     *
     * @param code    错误码
     * @param message 错误提示
     */
    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

}
