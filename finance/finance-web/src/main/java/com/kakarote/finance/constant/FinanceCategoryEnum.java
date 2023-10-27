package com.kakarote.finance.constant;

import cn.hutool.core.util.ObjectUtil;

import java.util.Objects;

public enum FinanceCategoryEnum {
    CATEGORY_ONE(1, 1, "流动资产"),
    CATEGORY_TWO(1, 2, "非流动资产"),
    CATEGORY_THREE(2, 1, "流动负债"),
    CATEGORY_FOUR(2, 2, "非流动负债"),
    CATEGORY_FIVE(3, 1, "所有者权益"),
    CATEGORY_SIX(4, 1, "成本"),
    CATEGORY_SEVEN(5, 1, "营业收入"),
    CATEGORY_EIGHT(5, 2, "其他收益"),
    CATEGORY_NINE(5, 3, "期间费用"),
    CATEGORY_TEN(5, 4, "其他损失"),
    CATEGORY_ELEVEN(5, 5, "营业成本及税金"),
    CATEGORY_TWELVE(5, 6, "以前年度损益调整"),
    CATEGORY_THIRTEEN(5, 7, "所得税"),
    CATEGORY_JOINTLY(6, 1, "共同"),
    NULL(999, 999, null);;

    private int type;

    private int category;

    private String name;

    public static FinanceCategoryEnum parseName(String name) {
        for (FinanceCategoryEnum categoryEnum : FinanceCategoryEnum.values()) {
            if (Objects.equals(categoryEnum.getName(), name)) {
                return categoryEnum;
            }
        }
        return NULL;
    }

    public static String parseCategory(Integer type, Integer category) {
        for (FinanceCategoryEnum categoryEnum : FinanceCategoryEnum.values()) {
            if (ObjectUtil.equal(categoryEnum.getType(), type) && ObjectUtil.equal(categoryEnum.getCategory(), category)) {
                return categoryEnum.getName();
            }
        }
        return "";
    }

    FinanceCategoryEnum(int type, int category, String name) {
        this.type = type;
        this.category = category;
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
