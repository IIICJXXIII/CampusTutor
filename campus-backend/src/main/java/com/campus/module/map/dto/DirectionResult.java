package com.campus.module.map.dto;

import lombok.Data;
import java.util.List;

/**
 * 路径规划结果
 */
@Data
public class DirectionResult {

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
     * 高德地图兼容 - 直接设置Route
     */
    public void setResult(Route route) {
        if (this.result == null) {
            this.result = new Result();
        }
        this.result.setRoutes(List.of(route));
    }

    @Data
    public static class Result {
        /**
         * 路线列表
         */
        private List<Route> routes;
    }

    @Data
    public static class Route {
        /**
         * 方案距离(米)
         */
        private Integer distance;

        /**
         * 预估耗时(秒)
         */
        private Integer duration;

        /**
         * 路线方向描述
         */
        private String direction;

        /**
         * 路线轨迹点(压缩后的坐标串)
         */
        private List<Double> polyline;

        /**
         * 路段列表
         */
        private List<Step> steps;

        public void setSteps(List<Step> steps) {
            this.steps = steps;
        }
    }

    @Data
    public static class Step {
        /**
         * 路段指令
         */
        private String instruction;

        /**
         * 路段距离(米)
         */
        private Integer distance;

        /**
         * 路段耗时(秒)
         */
        private Integer duration;

        /**
         * 路段名
         */
        private String roadName;

        /**
         * 转向类型
         */
        private String dirDesc;

        /**
         * 路段轨迹点(高德返回的polyline字符串)
         */
        private String polyline;
    }
}