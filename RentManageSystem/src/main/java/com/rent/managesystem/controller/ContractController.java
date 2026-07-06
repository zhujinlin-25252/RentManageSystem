package com.rent.managesystem.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rent.managesystem.common.Result;
import com.rent.managesystem.common.exception.BizException;
import com.rent.managesystem.common.ErrorCode;
import com.rent.managesystem.dto.PageQuery;
import com.rent.managesystem.dto.PageResult;
import com.rent.managesystem.entity.ContractInfo;
import com.rent.managesystem.service.ContractService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

/**
 * 合同管理控制器
 *
 * @author 连梓祺 & 团队
 */
@RestController
@RequestMapping("/contract")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;

    /** 创建合同申请（租客发起） */
    @PostMapping
    public Result<ContractInfo> applyContract(@RequestBody ContractInfo contract) {
        contract.setTenantId(getCurrentUserId());
        contract.setStatus(0);  // 草稿状态
        boolean saved = contractService.save(contract);
        return saved ? Result.success(contract) : Result.error(ErrorCode.SYSTEM_ERROR);
    }

    /** 查看合同详情 */
    @GetMapping("/{contractId}")
    public Result<ContractInfo> getContract(@PathVariable String contractId) {
        ContractInfo contract = checkAccess(contractId);
        return Result.success(contract);
    }

    /** 租客查看自己的合同列表 */
    @GetMapping("/my")
    public Result<PageResult<ContractInfo>> myContracts(PageQuery query) {
        String userId = getCurrentUserId();
        Page<ContractInfo> page = new Page<>(query.getPageNum(), query.getPageSize());
        Page<ContractInfo> result = contractService.lambdaQuery()
                .eq(ContractInfo::getTenantId, userId)
                .or().eq(ContractInfo::getLandlordId, userId)
                .orderByDesc(ContractInfo::getCreateTime)
                .page(page);
        return Result.success(PageResult.of(result));
    }

    /** 签署合同（租客或房东） */
    @PutMapping("/{contractId}/sign")
    public Result<ContractInfo> signContract(@PathVariable String contractId) {
        ContractInfo contract = checkAccess(contractId);
        String userId = getCurrentUserId();

        // 根据当前用户身份判断签署哪一方
        if (userId.equals(contract.getTenantId())) {
            if (contract.getStatus() != 0 && contract.getStatus() != 1) {
                throw new BizException(ErrorCode.CONTRACT_STATUS_ERROR);
            }
            contract.setStatus(2);  // 待房东签
        } else if (userId.equals(contract.getLandlordId())) {
            if (contract.getStatus() != 2) {
                throw new BizException(ErrorCode.CONTRACT_STATUS_ERROR);
            }
            contract.setStatus(3);  // 已生效
        } else {
            throw new BizException(ErrorCode.FORBIDDEN);
        }

        contractService.updateById(contract);
        return Result.success(contract);
    }

    /** 验证当前用户是否有权查看该合同 */
    private ContractInfo checkAccess(String contractId) {
        ContractInfo contract = contractService.getById(contractId);
        if (contract == null) {
            throw new BizException(ErrorCode.CONTRACT_NOT_FOUND);
        }
        String userId = getCurrentUserId();
        if (!userId.equals(contract.getTenantId()) && !userId.equals(contract.getLandlordId())) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }
        return contract;
    }

    private String getCurrentUserId() {
        return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
