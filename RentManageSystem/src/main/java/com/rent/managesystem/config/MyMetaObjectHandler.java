package com.rent.managesystem.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 自动填充处理器
 *
 * <p>当 Entity 中字段标注了 {@code @TableField(fill = FieldFill.INSERT)}
 * 或 {@code @TableField(fill = FieldFill.INSERT_UPDATE)} 时，
 * 在执行 insert / update 操作时自动填充值，无需手动 set。
 *
 * <p><b>自动填充的字段：</b>
 * <ul>
 *   <li>{@code create_time} — INSERT 时自动设为当前时间</li>
 *   <li>{@code update_time} — INSERT 和 UPDATE 时都自动刷新为当前时间</li>
 * </ul>
 *
 * <p>使用示例：
 * <pre>{@code
 * // 不需要手动设置时间了！
 * UserInfo user = new UserInfo();
 * user.setPhone("13800138000");
 * // user.setCreateTime(LocalDateTime.now()); ← 不需要！自动填充
 * userMapper.insert(user);  // createTime 和 updateTime 自动写入
 * }</pre>
 *
 * @author 连梓祺 & 团队
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    /**
     * 插入操作时自动填充
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }

    /**
     * 更新操作时自动填充
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }

}
