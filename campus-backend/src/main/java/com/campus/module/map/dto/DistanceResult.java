package com.campus.module.map.dto;

import lombok.Data;
import java.util.List;

/**
 * 距离计算结果
 */
@Data
public class DistanceResult {

    /**
     * 状态码，0表示成功
     */
    private Integer status;

    /**
     * 状态描述
     */
    private String message;

    /**
     * 结果数据
     */
    private Result result;

    /**
     * 高德地图兼容 - 直接存储元素列表
     */
    private List<Element> elements;

    public void setElements(List<Element> elements) {
        this.elements = elements;
        // 同时设置兼容的result结构
        if (this.result == null) {
            this.result = new Result();
        }
        DistanceElement de = new DistanceElement();
        de.setElements(elements);
        this.result.setRows(List.of(de));
    }

    @Data
    public static class Result {
        /**
         * 距离矩阵
         */
        private List<DistanceElement> rows;
    }

    @Data
    public static class DistanceElement {
        /**
         * 元素列表
         */
        private List<Element> elements;
    }

    @Data
    public static class Element {
        /**
         * 距离(米)
         */
        private Integer distance;

        /**
         * 耗时(秒)
         */
        private Integer duration;
    }
}