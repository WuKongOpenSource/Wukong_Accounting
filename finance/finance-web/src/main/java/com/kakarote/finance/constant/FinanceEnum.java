package com.kakarote.finance.constant;

import cn.hutool.core.util.ObjectUtil;

/**
 * @author 10323
 * @description: 财务系统枚举
 * @date 2021/8/2814:04
 */
public enum FinanceEnum {

    REPORT(4, "报表"),
    NULL(0, "NULL"),
    ;

    private final int type;

    private final String remarks;

    FinanceEnum(int type, String remarks) {
        this.type = type;
        this.remarks = remarks;
    }

    public static FinanceEnum parseType(Integer type) {
        for (FinanceEnum financeEnum : FinanceEnum.values()) {
            if (ObjectUtil.equal(type, financeEnum.getType())) {
                return financeEnum;
            }
        }
        return NULL;
    }

    public int getType() {
        return type;
    }

    public String getRemarks() {
        return remarks;
    }
}
