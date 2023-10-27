package com.kakarote.finance.constant;

import java.util.Objects;

/**
 * @description:类型 1 资产负债表 2 利润表 3 现金流量表 4 扩展表
 * @author: zjj
 * @date: 2021-08-31 10:43
 */
public enum FinanceTemplateType {
    BALANCE_SHEET(1, "资产负债表"),
    INCOME_STATEMENT(2, "利润表"),
    CASH_FLOW_STATEMENT(3, "现金流量表"),
    CASH_FLOW_STATEMENT_EXTEND(4, "扩展表"),
    NULL(999, null);;
    private final int type;

    private final String name;

    public static FinanceTemplateType parseIndex(Integer type) {
        for (FinanceTemplateType templateType : FinanceTemplateType.values()) {
            if (Objects.equals(templateType.getType(), type)) {
                return templateType;
            }
        }
        return NULL;
    }

    FinanceTemplateType(int type, String name) {
        this.type = type;
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public String getName() {
        return name;
    }
}
