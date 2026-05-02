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

    public Double getMatchScore() { return matchScore; }
    public void setMatchScore(Double matchScore) { this.matchScore = matchScore; }
    public Double getCfScore() { return cfScore; }
    public void setCfScore(Double cfScore) { this.cfScore = cfScore; }
    public Double getDeepFmScore() { return deepFmScore; }
    public void setDeepFmScore(Double deepFmScore) { this.deepFmScore = deepFmScore; }
    public List<String> getMatchTags() { return matchTags; }
    public void setMatchTags(List<String> matchTags) { this.matchTags = matchTags; }
}
