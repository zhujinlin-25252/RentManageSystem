package com.rent.managesystem.dto;

import lombok.Data;

/**
 * 分页查询通用请求DTO
 *
 * <p>所有列表接口的统一分页参数封装。
 *
 * <p><b>使用示例：</b>
 * <pre>{@code
 * GET /api/house/list?page=1&size=10&keyword=整租
 * }</pre>
 *
 * @author 连梓祺 & 团队
 */
@Data
public class PageQuery {

    /** 当前页码（默认第1页） */
    private Integer page = 1;

    /** 每页条数（默认10条，最大100条） */
    private Integer size = 10;

    /** 搜索关键字（可选，用于模糊搜索标题/昵称等） */
    private String keyword;

    /**
     * 获取当前页码（带边界校验）
     */
    public int getPageNum() {
        if (page == null || page < 1) return 1;
        return page;
    }

    /**
     * 获取每页大小（限制最大值防恶意请求）
     */
    public int getPageSize() {
        if (size == null || size < 1) return 10;
        return Math.min(size, 100);  // 单页最多100条
    }
}
