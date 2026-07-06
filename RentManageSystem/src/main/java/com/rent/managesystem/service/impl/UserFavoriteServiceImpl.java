package com.rent.managesystem.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rent.managesystem.entity.UserFavorite;
import com.rent.managesystem.mapper.UserFavoriteMapper;
import com.rent.managesystem.service.UserFavoriteService;
import org.springframework.stereotype.Service;

/**
 * 用户收藏 Service 实现类
 *
 * @author 连梓祺 & 团队
 */
@Service
public class UserFavoriteServiceImpl extends ServiceImpl<UserFavoriteMapper, UserFavorite> implements UserFavoriteService {
}
