package com.campus.module.ocr.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * OCR 识别结果 DTO
 */
@Data
@Schema(description = "OCR识别结果")
public class OcrResultDTO {

    @Schema(description = "识别是否成功")
    private Boolean success;

    @Schema(description = "错误信息")
    private String errorMsg;

    // ===== 学生证识别结果 =====
    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "学校名称")
    private String universityName;

    @Schema(description = "专业")
    private String major;

    @Schema(description = "学号")
    private String studentId;

    @Schema(description = "入学年份")
    private Integer enrollYear;

    // ===== 身份证识别结果 =====
    @Schema(description = "身份证号")
    private String idCard;

    @Schema(description = "性别")
    private String gender;

    @Schema(description = "民族")
    private String nation;

    @Schema(description = "出生日期")
    private String birthDate;

    @Schema(description = "地址")
    private String address;

    // Getters and Setters
    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getUniversityName() {
        return universityName;
    }

    public void setUniversityName(String universityName) {
        this.universityName = universityName;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public Integer getEnrollYear() {
        return enrollYear;
    }

    public void setEnrollYear(Integer enrollYear) {
        this.enrollYear = enrollYear;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getNation() {
        return nation;
    }

    public void setNation(String nation) {
        this.nation = nation;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
