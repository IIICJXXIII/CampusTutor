package com.campus.module.map.dto;

import java.util.List;

/**
 * 路径规划结果
 */
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

    /**
     * 高德地图兼容 - 直接设置Route
     */
    public void setResult(Route route) {
        if (this.result == null) {
            this.result = new Result();
        }
        this.result.setRoutes(List.of(route));
    }

    public static class Result {
        /**
         * 路线列表
         */
        private List<Route> routes;

        public List<Route> getRoutes() {
            return routes;
        }

        public void setRoutes(List<Route> routes) {
            this.routes = routes;
        }
    }

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

        public String getDirection() {
            return direction;
        }

        public void setDirection(String direction) {
            this.direction = direction;
        }

        public List<Double> getPolyline() {
            return polyline;
        }

        public void setPolyline(List<Double> polyline) {
            this.polyline = polyline;
        }

        public List<Step> getSteps() {
            return steps;
        }

        public void setSteps(List<Step> steps) {
            this.steps = steps;
        }
    }

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

        public String getInstruction() {
            return instruction;
        }

        public void setInstruction(String instruction) {
            this.instruction = instruction;
        }

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

        public String getRoadName() {
            return roadName;
        }

        public void setRoadName(String roadName) {
            this.roadName = roadName;
        }

        public String getDirDesc() {
            return dirDesc;
        }

        public void setDirDesc(String dirDesc) {
            this.dirDesc = dirDesc;
        }

        public String getPolyline() {
            return polyline;
        }

        public void setPolyline(String polyline) {
            this.polyline = polyline;
        }
    }
}