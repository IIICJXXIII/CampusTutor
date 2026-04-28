package com.campus.module.map.dto;

import java.util.List;

/**
 * 距离计算结果
 */
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

    // 显式的getter和setter方法
    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public List<Element> getElements() {
        return elements;
    }

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

    public static class Result {
        /**
         * 距离矩阵
         */
        private List<DistanceElement> rows;

        public List<DistanceElement> getRows() {
            return rows;
        }

        public void setRows(List<DistanceElement> rows) {
            this.rows = rows;
        }
    }

    public static class DistanceElement {
        /**
         * 元素列表
         */
        private List<Element> elements;

        public List<Element> getElements() {
            return elements;
        }

        public void setElements(List<Element> elements) {
            this.elements = elements;
        }
    }

    public static class Element {
        /**
         * 距离(米)
         */
        private Integer distance;

        /**
         * 耗时(秒)
         */
        private Integer duration;

        public Integer getDistance() {
            return distance;
        }

        public void setDistance(Integer distance) {
            this.distance = distance;
        }

        public Integer getDuration() {
            return duration;
        }

        public void setDuration(Integer duration) {
            this.duration = duration;
        }
    }
}