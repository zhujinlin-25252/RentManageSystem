package com.rent.managesystem.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

import java.util.List;

/**
 * 分页结果响应DTO
 *
 * <p>包装 MyBatis-Plus 的 Page 对象为前端友好的格式：
 * <pre>
 * {
 *   "records": [...],       // 数据列表
 *   "total": 100,           // 总记录数
 *   "page": 1,              // 当前页码
 *   "size": 10,             // 每页条数
 *   "pages": 10             // 总页数
 * }
 * </pre>
 *
 * @param <T> 列表元素类型
 * @author 连梓祺 & 团队
 */
@Data
public class PageResult<T> {

    /** 当前页数据列表 */
    private List<T> records;

    /** 总记录数 */
    private long total;

    /** 当前页码 */
    private long page;

    /** 每页条数 */
    private long size;

    /** 总页数 */
    private long pages;

    /**
     * 从 MyBatis-Plus 的 Page 对象转换
     *
     * @param mpPage MP分页结果
     * @return 统一分页响应
     */
    public static <T> PageResult<T> of(Page<T> mpPage) {
        PageResult<T> result = new PageResult<>();
        result.setRecords(mpPage.getRecords());
        result.setTotal(mpPage.getTotal());
        result.setPage(mpPage.getCurrent());
        result.setSize(mpPage.getSize());
        result.setPages(mpPage.getPages());
        return result;
    }
}
