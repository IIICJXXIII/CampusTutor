package com.campus.module.match.dto;

/**
 * 匹配视角类型枚举
 * 用于区分家长端和教师端的不同评分逻辑
 */
public enum MatchViewType {

    /**
     * 家长视角：找老师
     * 价格逻辑：教师报价越低越好
     * 关注：教师质量、评分、学历
     */
    PARENT_VIEW,

    /**
     * 教师视角：找需求
     * 价格逻辑：需求预算越高越好
     * 关注：需求热度、家长响应度
     */
    TEACHER_VIEW
}
