package com.kakarote.finance.constant;

import java.util.Objects;

/**
 * 取数规则
 */
public enum FinanceRuleEnum {
    BALANCE(0, "余额"),
    LEND_BALANCE(1, "借方余额"),
    LOAN_BALANCE(2, "贷方余额"),
    SUBJECT_LEND_BALANCE(3, "科目借方余额"),
    SUBJECT_LOAN_BALANCE(4, "科目贷方余额"),
    DEBIT_AMOUNT(5, "借方发生额"),
    CREDIT_AMOUNT(6, "贷方发生额"),
    PROFIT_LOSS_AMOUNT(7, "损益发生额"),
    NULL(99, null),
    ;

    private int type;

    private String name;

    FinanceRuleEnum(int type, String name) {
        this.type = type;
        this.name = name;
    }

    public static FinanceRuleEnum parseType(Integer type) {
        for (FinanceRuleEnum ruleEnum : FinanceRuleEnum.values()) {
            if (Objects.equals(ruleEnum.getType(), type)) {
                return ruleEnum;
            }
        }
        return NULL;
    }

    public static FinanceRuleEnum parseName(String name) {
        for (FinanceRuleEnum ruleEnum : FinanceRuleEnum.values()) {
            if (Objects.equals(ruleEnum.getName(), name)) {
                return ruleEnum;
            }
        }
        return NULL;
    }

    public int getType() {
        return type;
    }

    public String getName() {
        return name;
    }
}
