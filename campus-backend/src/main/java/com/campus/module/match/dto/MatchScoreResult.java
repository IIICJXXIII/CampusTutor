package com.campus.module.match.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class MatchScoreResult extends TutorSearchResult {

    @JsonIgnore
    private Double matchScore;

    @JsonIgnore
    private Double subjectScore;

    @JsonIgnore
    private Double gradeScore;

    @JsonIgnore
    private Double distanceScore;

    @JsonIgnore
    private Double priceScore;

    @JsonIgnore
    private Double ratingScore;

    @JsonIgnore
    private Double experienceScore;

    @JsonIgnore
    private Double educationScore;

    @JsonIgnore
    private Double specialtyScore;

    @JsonIgnore
    private Double hotnessScore;

    @JsonIgnore
    private Double teachModeScore;

    @JsonIgnore
    private Double cfScore;

    @JsonIgnore
    private Double deepFmScore;

    private List<String> matchTags;

}
