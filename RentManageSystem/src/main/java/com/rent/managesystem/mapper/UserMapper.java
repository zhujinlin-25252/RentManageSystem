package com.rent.managesystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rent.managesystem.entity.UserInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户信息 Mapper 接口
 *
 * <p>继承 MyBatis-Plus 的 {@link BaseMapper}，
 * 自动拥有以下能力：
 * <ul>
 *   <li>insert / deleteById / updateById — 单行增删改</li>
 *   <li>selectById / selectList / selectCount / selectPage — 查询</li>
 *   <li>条件构造器（QueryWrapper/LambdaQueryWrapper）— 动态条件查询</li>
 * </ul>
 *
 * <p><b>复杂SQL：</b>写在 {@code resources/mapper/UserMapper.xml} 中
 *
 * @author 连梓祺 & 团队
 * @see com.rent.managesystem.entity.UserInfo
 */
@Mapper
public interface UserMapper extends BaseMapper<UserInfo> {

}
