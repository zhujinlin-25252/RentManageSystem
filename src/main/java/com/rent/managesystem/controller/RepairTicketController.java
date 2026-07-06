package com.rent.managesystem.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rent.managesystem.common.Result;
import com.rent.managesystem.common.ErrorCode;
import com.rent.managesystem.dto.PageQuery;
import com.rent.managesystem.dto.PageResult;
import com.rent.managesystem.entity.RepairTicket;
import com.rent.managesystem.service.RepairTicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 报修工单控制器
 *
 * @author 连梓祺 & 团队
 */
@RestController
@RequestMapping("/repair")
@RequiredArgsConstructor
public class RepairTicketController {

    private final RepairTicketService repairTicketService;

    /** 租客提交报修工单 */
    @PostMapping
    public Result<RepairTicket> submitTicket(@RequestBody RepairTicket ticket) {
        ticket.setReporterId(getCurrentUserId());  // 报修人=当前登录用户
        ticket.setStatus(0);                       // 待处理
        ticket.setPriority(ticket.getPriority() != null ? ticket.getPriority() : 1);

        boolean saved = repairTicketService.save(ticket);
        return saved ? Result.success(ticket) : Result.error(ErrorCode.SYSTEM_ERROR);
    }

    /** 查看工单详情 */
    @GetMapping("/{ticketId}")
    public Result<RepairTicket> getTicket(@PathVariable String ticketId) {
        RepairTicket ticket = repairTicketService.getById(ticketId);
        if (ticket == null) {
            return Result.error(ErrorCode.TICKET_NOT_FOUND);
        }
        return Result.success(ticket);
    }

    /** 查看我的报修记录 */
    @GetMapping("/my")
    public Result<PageResult<RepairTicket>> myTickets(PageQuery query) {
        Page<RepairTicket> page = new Page<>(query.getPageNum(), query.getPageSize());
        Page<RepairTicket> result = repairTicketService.lambdaQuery()
                .eq(RepairTicket::getReporterId, getCurrentUserId())
                .orderByDesc(RepairTicket::getCreateTime)
                .page(page);
        return Result.success(PageResult.of(result));
    }

    /** 客服处理工单 */
    @PutMapping("/{ticketId}/handle")
    public Result<RepairTicket> handleTicket(@PathVariable String ticketId,
                                              @RequestBody RepairTicket ticketBody) {
        RepairTicket ticket = repairTicketService.getById(ticketId);
        if (ticket == null) {
            return Result.error(ErrorCode.TICKET_NOT_FOUND);
        }
        ticket.setStatus(1);  // 处理中
        ticket.setHandlerId(getCurrentUserId());
        ticket.setHandlerReply(ticketBody.getHandlerReply());
        repairTicketService.updateById(ticket);
        return Result.success(ticket);
    }

    /** 租客关闭工单并评价 */
    @PutMapping("/{ticketId}/close")
    public Result<RepairTicket> closeTicket(@PathVariable String ticketId,
                                            @RequestBody RepairTicket ticketBody) {
        RepairTicket ticket = repairTicketService.getById(ticketId);
        if (ticket == null) {
            return Result.error(ErrorCode.TICKET_NOT_FOUND);
        }
        if (!ticket.getReporterId().equals(getCurrentUserId())) {
            return Result.error(ErrorCode.FORBIDDEN);
        }
        ticket.setStatus(3);       // 已关闭
        ticket.setSatisfaction(ticketBody.getSatisfaction());
        ticket.setFeedback(ticketBody.getFeedback());
        ticket.setClosedAt(java.time.LocalDateTime.now());
        repairTicketService.updateById(ticket);
        return Result.success(ticket);
    }

    private String getCurrentUserId() {
        return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
