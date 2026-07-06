package com.rent.managesystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rent.managesystem.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志 Mapper
 *
 * @author 连梓祺 & 团队
 */
@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {
}
