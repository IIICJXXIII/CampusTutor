package com.campus.module.map.dto;

import lombok.Data;
import java.util.List;

/**
 * 逆地址解析结果
 */
@Data
public class GeocoderResult {

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
     * 高德兼容 - 结果数据别名
     */
    private ResultData resultData;

    public void setResult(ResultData data) {
        this.resultData = data;
        // 同时设置兼容字段
        if (this.result == null) {
            this.result = new Result();
        }
        if (data != null) {
            this.result.setAddress(data.getAddress());
            this.result.setAddressComponent(data.getAddressComponent());
        }
    }

    @Data
    public static class ResultData {
        private String address;
        private Location location;
        private AddressComponent addressComponent;
    }

    @Data
    public static class Location {
        private Double lat;
        private Double lng;
    }

    @Data
    public static class Result {
        /**
         * 详细地址
         */
        private String address;

        /**
         * 格式化地址
         */
        private FormattedAddress formattedAddresses;

        /**
         * 地址组件
         */
        private AddressComponent addressComponent;

        /**
         * POI信息
         */
        private List<Poi> pois;
    }

    @Data
    public static class FormattedAddress {
        private String recommend;
        private String rough;
    }

    @Data
    public static class AddressComponent {
        /**
         * 国家
         */
        private String nation;
        /**
         * 省
         */
        private String province;
        /**
         * 市
         */
        private String city;
        /**
         * 区
         */
        private String district;
        /**
         * 街道
         */
        private String street;
        /**
         * 门牌号
         */
        private String streetNumber;
    }

    @Data
    public static class Poi {
        /**
         * POI唯一标识
         */
        private String id;
        /**
         * POI名称
         */
        private String title;
        /**
         * POI地址
         */
        private String address;
        /**
         * POI分类
         */
        private String category;
        /**
         * 距离(米)
         */
        private Double distance;
    }
}
