package com.campus.module.map.dto;

import java.util.List;

/**
 * 逆地址解析结果
 */
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

    public ResultData getResultData() {
        return resultData;
    }

    public void setResultData(ResultData resultData) {
        this.resultData = resultData;
    }

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

    public static class ResultData {
        private String address;
        private Location location;
        private AddressComponent addressComponent;

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public Location getLocation() {
            return location;
        }

        public void setLocation(Location location) {
            this.location = location;
        }

        public AddressComponent getAddressComponent() {
            return addressComponent;
        }

        public void setAddressComponent(AddressComponent addressComponent) {
            this.addressComponent = addressComponent;
        }
    }

    public static class Location {
        private Double lat;
        private Double lng;

        public Double getLat() {
            return lat;
        }

        public void setLat(Double lat) {
            this.lat = lat;
        }

        public Double getLng() {
            return lng;
        }

        public void setLng(Double lng) {
            this.lng = lng;
        }
    }

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

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public FormattedAddress getFormattedAddresses() {
            return formattedAddresses;
        }

        public void setFormattedAddresses(FormattedAddress formattedAddresses) {
            this.formattedAddresses = formattedAddresses;
        }

        public AddressComponent getAddressComponent() {
            return addressComponent;
        }

        public void setAddressComponent(AddressComponent addressComponent) {
            this.addressComponent = addressComponent;
        }

        public List<Poi> getPois() {
            return pois;
        }

        public void setPois(List<Poi> pois) {
            this.pois = pois;
        }
    }

    public static class FormattedAddress {
        private String recommend;
        private String rough;

        public String getRecommend() {
            return recommend;
        }

        public void setRecommend(String recommend) {
            this.recommend = recommend;
        }

        public String getRough() {
            return rough;
        }

        public void setRough(String rough) {
            this.rough = rough;
        }
    }

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

        public String getNation() {
            return nation;
        }

        public void setNation(String nation) {
            this.nation = nation;
        }

        public String getProvince() {
            return province;
        }

        public void setProvince(String province) {
            this.province = province;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getDistrict() {
            return district;
        }

        public void setDistrict(String district) {
            this.district = district;
        }

        public String getStreet() {
            return street;
        }

        public void setStreet(String street) {
            this.street = street;
        }

        public String getStreetNumber() {
            return streetNumber;
        }

        public void setStreetNumber(String streetNumber) {
            this.streetNumber = streetNumber;
        }
    }

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

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public Double getDistance() {
            return distance;
        }

        public void setDistance(Double distance) {
            this.distance = distance;
        }
    }
}