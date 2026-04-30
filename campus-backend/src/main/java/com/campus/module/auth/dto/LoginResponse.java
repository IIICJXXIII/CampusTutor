package com.campus.module.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "登录响应")
public class LoginResponse {

    @Schema(description = "JWT Token")
    private String token;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "角色: 0-管理员, 1-教员, 2-家长")
    private Integer role;

    @Schema(description = "是否需要绑定手机号")
    private Boolean needBind;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "经度")
    private BigDecimal longitude;

    @Schema(description = "纬度")
    private BigDecimal latitude;

    @Schema(description = "地址")
    private String address;

    public LoginResponse() {
    }

    public LoginResponse(String token, Long userId, String username, String nickname, String avatar,
                         Integer role, Boolean needBind, String phone,
                         BigDecimal longitude, BigDecimal latitude, String address) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.nickname = nickname;
        this.avatar = avatar;
        this.role = role;
        this.needBind = needBind;
        this.phone = phone;
        this.longitude = longitude;
        this.latitude = latitude;
        this.address = address;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public Integer getRole() { return role; }
    public void setRole(Integer role) { this.role = role; }
    public Boolean getNeedBind() { return needBind; }
    public void setNeedBind(Boolean needBind) { this.needBind = needBind; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String token;
        private Long userId;
        private String username;
        private String nickname;
        private String avatar;
        private Integer role;
        private Boolean needBind;
        private String phone;
        private BigDecimal longitude;
        private BigDecimal latitude;
        private String address;

        public Builder token(String token) { this.token = token; return this; }
        public Builder userId(Long userId) { this.userId = userId; return this; }
        public Builder username(String username) { this.username = username; return this; }
        public Builder nickname(String nickname) { this.nickname = nickname; return this; }
        public Builder avatar(String avatar) { this.avatar = avatar; return this; }
        public Builder role(Integer role) { this.role = role; return this; }
        public Builder needBind(Boolean needBind) { this.needBind = needBind; return this; }
        public Builder phone(String phone) { this.phone = phone; return this; }
        public Builder longitude(BigDecimal longitude) { this.longitude = longitude; return this; }
        public Builder latitude(BigDecimal latitude) { this.latitude = latitude; return this; }
        public Builder address(String address) { this.address = address; return this; }

        public LoginResponse build() {
            return new LoginResponse(token, userId, username, nickname, avatar, role, needBind, phone,
                    longitude, latitude, address);
        }
    }
}
