package com.rent.managesystem.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rent.managesystem.entity.NoticeInfo;
import com.rent.managesystem.mapper.NoticeInfoMapper;
import com.rent.managesystem.service.NoticeInfoService;
import org.springframework.stereotype.Service;

/**
 * 公告通知 Service 实现类
 *
 * @author 连梓祺 & 团队
 */
@Service
public class NoticeInfoServiceImpl extends ServiceImpl<NoticeInfoMapper, NoticeInfo> implements NoticeInfoService {
}
