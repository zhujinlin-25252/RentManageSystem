package com.rent.managesystem.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rent.managesystem.common.Result;
import com.rent.managesystem.common.exception.BizException;
import com.rent.managesystem.common.ErrorCode;
import com.rent.managesystem.dto.PageQuery;
import com.rent.managesystem.dto.PageResult;
import com.rent.managesystem.entity.NoticeInfo;
import com.rent.managesystem.service.NoticeInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 公告通知控制器
 *
 * @author 连梓祺 & 团队
 */
@RestController
@RequestMapping("/notice")
@RequiredArgsConstructor
public class NoticeInfoController {

    private final NoticeInfoService noticeInfoService;

    /** 公开查看公告列表（分页） */
    @GetMapping("/public/list")
    public Result<PageResult<NoticeInfo>> listPublicNotices(PageQuery query) {
        Page<NoticeInfo> page = new Page<>(query.getPageNum(), query.getPageSize());
        Page<NoticeInfo> result = noticeInfoService.lambdaQuery()
                .eq(NoticeInfo::getStatus, 1)  // 已发布
                .orderByDesc(NoticeInfo::getIsTop)
                .orderByDesc(NoticeInfo::getPublishAt)
                .page(page);
        return Result.success(PageResult.of(result));
    }

    /** 查看公告详情（公开） */
    @GetMapping("/public/{noticeId}")
    public Result<NoticeInfo> getNoticeDetail(@PathVariable String noticeId) {
        NoticeInfo notice = noticeInfoService.getById(noticeId);
        if (notice == null || notice.getStatus() != 1) {
            return Result.error(ErrorCode.USER_NOT_FOUND);  // 复用错误码
        }
        // 阅读次数 +1
        notice.setViewCount(notice.getViewCount() == null ? 1 : notice.getViewCount() + 1);
        noticeInfoService.updateById(notice);
        return Result.success(notice);
    }

    /** 管理员创建公告 */
    @PostMapping
    public Result<NoticeInfo> createNotice(@RequestBody NoticeInfo notice) {
        notice.setPublisherId(getCurrentUserId());
        notice.setStatus(0);  // 默认草稿
        boolean saved = noticeInfoService.save(notice);
        return saved ? Result.success(notice) : Result.error(ErrorCode.SYSTEM_ERROR);
    }

    /** 发布公告 */
    @PutMapping("/{noticeId}/publish")
    public Result<NoticeInfo> publishNotice(@PathVariable String noticeId) {
        NoticeInfo notice = noticeInfoService.getById(noticeId);
        if (notice == null) {
            throw new BizException(ErrorCode.USER_NOT_FOUND);
        }
        notice.setStatus(1);  // 已发布
        notice.setPublishAt(java.time.LocalDateTime.now());
        noticeInfoService.updateById(notice);
        return Result.success(notice);
    }

    /** 撤回公告 */
    @PutMapping("/{noticeId}/withdraw")
    public Result<NoticeInfo> withdrawNotice(@PathVariable String noticeId) {
        NoticeInfo notice = noticeInfoService.getById(noticeId);
        if (notice == null) {
            throw new BizException(ErrorCode.USER_NOT_FOUND);
        }
        notice.setStatus(2);  // 已撤回
        noticeInfoService.updateById(notice);
        return Result.success(notice);
    }

    private String getCurrentUserId() {
        return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
