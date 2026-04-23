package com.campus.module.tutor.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TutorPublicVO {
    private Long id;
    private Long userId;
    private String realName;
    private String universityName;
    private String major;
    private Integer education;
    private Integer enrollYear;
    private String teachSubjects;
    private String teachGrades;
    private String teachStyle;
    private String introduction;
    private BigDecimal expectPrice;
    private Integer canVisit;
    private Integer canOnline;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String address;
    private Integer certStatus;
    private BigDecimal rating;
    private Integer orderCount;
}
