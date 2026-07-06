package com.rent.managesystem.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rent.managesystem.common.Result;
import com.rent.managesystem.dto.PageQuery;
import com.rent.managesystem.dto.PageResult;
import com.rent.managesystem.entity.ExpenseRecord;
import com.rent.managesystem.service.ExpenseRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 费用记录控制器
 *
 * @author 连梓祺 & 团队
 */
@RestController
@RequestMapping("/expense")
@RequiredArgsConstructor
public class ExpenseRecordController {

    private final ExpenseRecordService expenseRecordService;

    /** 房东录入费用 */
    @PostMapping
    public Result<ExpenseRecord> recordExpense(@RequestBody ExpenseRecord expense) {
        expense.setCreatorId(getCurrentUserId());
        expense.setStatus(0);  // 待缴
        boolean saved = expenseRecordService.save(expense);
        return saved ? Result.success(expense) : Result.error(com.rent.managesystem.common.ErrorCode.SYSTEM_ERROR);
    }

    /** 查看待缴费用列表 */
    @GetMapping("/list")
    public Result<PageResult<ExpenseRecord>> listExpenses(PageQuery query,
                                                          @RequestParam(required = false) Integer type) {
        Page<ExpenseRecord> page = new Page<>(query.getPageNum(), query.getPageSize());

        var lambdaQuery = expenseRecordService.lambdaQuery()
                .eq(type != null, ExpenseRecord::getExpenseType, type)
                .orderByDesc(ExpenseRecord::getPeriodStart);

        if (type == null) {
            lambdaQuery.page(page);
        } else {
            lambdaQuery.page(page);
        }

        // 重新执行查询确保条件生效
        Page<ExpenseRecord> result = expenseRecordService.lambdaQuery()
                .eq(type != null, ExpenseRecord::getExpenseType, type)
                .orderByDesc(ExpenseRecord::getPeriodStart)
                .page(page);

        return Result.success(PageResult.of(result));
    }

    private String getCurrentUserId() {
        return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
