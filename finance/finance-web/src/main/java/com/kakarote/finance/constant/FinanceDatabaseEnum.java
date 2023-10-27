package com.kakarote.finance.constant;


/**
 * @author dsc
 * @description: 财务数据初始化表名枚举
 * @date 2021/10/22 13:26
 */
public enum FinanceDatabaseEnum {

    WK_FINANCE_ACCOUNT_SET("账套表"),
    WK_FINANCE_ACCOUNT_USER("账套员工对应关系表"),
    WK_FINANCE_ADJUVANT("辅助核算表"),
    WK_FINANCE_ADJUVANT_CARTE("辅助核算关联类型表"),
    WK_FINANCE_ASSIST("辅助核算辅助表"),
    WK_FINANCE_ASSIST_ADJUVANT("辅助核算关联模块表"),
    WK_FINANCE_BALANCE_SHEET_CONFIG("资产负债表配置"),
    WK_FINANCE_BALANCE_SHEET_REPORT("资产负债表报表数据"),
    WK_FINANCE_CASH_FLOW_STATEMENT_CONFIG("现金流量表"),
    WK_FINANCE_CASH_FLOW_STATEMENT_EXTEND_CONFIG("现金流量扩展表"),
    WK_FINANCE_CASH_FLOW_STATEMENT_EXTEND_DATA("现金流量扩展表"),
    WK_FINANCE_CASH_FLOW_STATEMENT_REPORT("现金流量表数据"),
    WK_FINANCE_CERTIFICATE("凭证表"),
    WK_FINANCE_CERTIFICATE_ASSOCIATION("凭证详情关联标签类型表"),
    WK_FINANCE_CERTIFICATE_DETAIL("凭证详情"),
    WK_FINANCE_CURRENCY("币种"),
    WK_FINANCE_DASHBOARD_CONFIG("仪表盘设置表"),
    WK_FINANCE_DIGEST("凭证摘要"),
    WK_FINANCE_FLOWS("现金流量初始余额"),
    WK_FINANCE_INCOME_STATEMENT_CONFIG("利润表配置"),
    WK_FINANCE_INCOME_STATEMENT_REPORT("利润表报表数据"),
    WK_FINANCE_INITIAL("财务初始余额"),
    WK_FINANCE_INITIAL_ASSIST("初始余额辅助核算"),
    WK_FINANCE_INITIAL_ASSIST_ADJUVANT("财务初始余额管理辅助核算表"),
    WK_FINANCE_PARAMETER("系统参数设置"),
    WK_FINANCE_STATEMENT("结账表"),
    WK_FINANCE_STATEMENT_CERTIFICATE("结账和凭证关联表"),
    WK_FINANCE_STATEMENT_SETTLE("结账清单表"),
    WK_FINANCE_STATEMENT_SUBJECT("结账和科目关联表（除结转损益类型）"),
    WK_FINANCE_STATEMENT_TEMPLATE("结账模板表"),
    WK_FINANCE_STATEMENT_TEMPLATE_SUBJECT("结账模板关联科目表"),
    WK_FINANCE_SUBJECT("科目"),
    WK_FINANCE_SUBJECT_ADJUVANT("科目和辅助核算关联表"),
    WK_FINANCE_SUBJECT_CURRENCY("科目和币种关联表"),
    WK_FINANCE_TEMPLATE("凭证模板"),
    WK_FINANCE_TEMPLATE_TYPE("凭证模板类型"),
    WK_FINANCE_VOUCHER("凭证字");

    private final String tableName;

    FinanceDatabaseEnum(String tableName) {
        this.tableName = tableName;
    }

    public String getTableName() {
        return tableName;
    }
}
