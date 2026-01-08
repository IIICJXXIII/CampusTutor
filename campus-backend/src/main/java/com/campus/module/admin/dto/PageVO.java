package com.campus.module.admin.dto;

import lombok.Data;
import java.util.List;

/**
 * 通用分页响应
 */
@Data
public class PageVO<T> {

    /** 数据列表 */
    private List<T> records;

    /** 总记录数 */
    private Long total;

    /** 当前页码 */
    private Integer page;

    /** 每页大小 */
    private Integer size;

    /** 总页数 */
    private Integer pages;

    public static <T> PageVO<T> of(List<T> records, Long total, Integer page, Integer size) {
        PageVO<T> vo = new PageVO<>();
        vo.setRecords(records);
        vo.setTotal(total);
        vo.setPage(page);
        vo.setSize(size);
        vo.setPages((int) Math.ceil((double) total / size));
        return vo;
    }
}
