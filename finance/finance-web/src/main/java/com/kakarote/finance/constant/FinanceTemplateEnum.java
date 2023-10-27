package com.kakarote.finance.constant;


import cn.hutool.core.util.StrUtil;

/**
 * 财务模板枚举
 *
 * @author zhangzhiwei
 */
public enum FinanceTemplateEnum {
    /*
        初始科目
     */
    SUBJECT(0, "初始科目"),
    /*
        资产负债表
     */
    BALANCE_SHEET(1, "资产负债表"),
    INCOME_STATEMENT(2, "利润表"),
    CASH_FLOW_STATEMENT(3, "会计准则"),
    STATEMENT_TEMPLATE(4, "结账模板表"),
    STATEMENT(5, "结账表"),
    CASH_FLOW_STATEMENT_EXTEND(6, "辅助表格"),
    NULL(999, "NULL");

    private final int index;

    private final String name;

    FinanceTemplateEnum(int index, String name) {
        this.index = index;
        this.name = name;
    }

    public static FinanceTemplateEnum parseName(String name) {
        for (FinanceTemplateEnum nameEnum : FinanceTemplateEnum.values()) {
            if (name.contains(nameEnum.getName())) {
                return nameEnum;
            }
        }
        return NULL;
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public String getServiceName() {
        String name = StrUtil.toCamelCase(name().toLowerCase());
        if (name.contains("Template")) {
            name = name.replace("Template", "");
        }
        return name + "TemplateService";
    }
}
