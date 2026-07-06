package com.rent.managesystem.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

/**
 * 统一API响应结果封装
 *
 * <p>项目中所有Controller接口的返回值都必须使用此类，保证前后端交互格式一致：
 * <pre>
 * {
 *   "code": 200,          // 业务状态码（非HTTP状态码）
 *   "message": "success", // 提示信息
 *   "data": { ... }       // 实际业务数据（可能为null）
 * }
 * </pre>
 *
 * <p><b>使用示例：</b>
 * <pre>{@code
 * // 成功返回（带数据）
 * return Result.success(userList);
 *
 * // 成功返回（无数据）
 * return Result.success();
 *
 * // 失败返回
 * return Result.error(ErrorCode.USER_NOT_FOUND);
 * }</pre>
 *
 * @param <T> 响应数据的泛型类型
 * @author 连梓祺 & 团队
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> implements Serializable {

    /** 序列化版本号 */
    private static final long serialVersionUID = 1L;

    /** 业务状态码：200=成功, 其他=失败（参考 ErrorCode 枚举） */
    private int code;

    /** 提示信息：成功时为"操作成功"，失败时为具体错误原因 */
    private String message;

    /** 业务数据：查询接口返回具体对象/列表，写接口可返回null或操作结果 */
    private T data;

    // ==================== 成功响应 ====================

    /**
     * 成功响应（无数据）
     * 适用于：新增、修改、删除等写操作
     */
    public static <T> Result<T> success() {
        return new Result<>(200, "操作成功", null);
    }

    /**
     * 成功响应（带数据）
     * 适用于：查询列表、详情等读操作
     *
     * @param data 要返回的业务数据
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    /**
     * 成功响应（自定义消息 + 数据）
     *
     * @param message 自定义提示消息
     * @param data    要返回的业务数据
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data);
    }

    // ==================== 失败响应 ====================

    /**
     * 失败响应（使用预定义错误码）
     *
     * @param errorCode 错误码枚举值（来自 ErrorCode 类）
     */
    public static <T> Result<T> error(ErrorCode errorCode) {
        return new Result<>(errorCode.getCode(), errorCode.getMessage(), null);
    }

    /**
     * 失败响应（自定义错误码和消息）
     *
     * @param code    错误码（建议使用 ErrorCode 中定义的值）
     * @param message 错误提示信息（会展示给前端用户）
     */
    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }

}
