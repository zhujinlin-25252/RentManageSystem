package com.rent.managesystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rent.managesystem.entity.UserInfo;

/**
 * 用户 Service 接口
 *
 * <p>定义用户相关的业务方法。继承 IService 后自带 CRUD 能力，
 * 这里只添加业务特有的自定义方法。
 *
 * @author 连梓祺 & 团队
 */
public interface UserService extends IService<UserInfo> {

    /**
     * 根据手机号查询用户
     *
     * @param 手机号
     * @return 用户对象，不存在则返回 null
     */
    UserInfo getByPhone(String phone);

    /**
     * 用户登录验证
     *
     * @param phone      手机号
     * @param rawPassword 明文密码
     * @return 登录成功返回用户对象
     * @throws RuntimeException 密码错误或账户异常时抛出
     */
    UserInfo login(String phone, String rawPassword);

}
