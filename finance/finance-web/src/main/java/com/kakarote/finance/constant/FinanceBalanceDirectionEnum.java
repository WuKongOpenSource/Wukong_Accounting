package com.kakarote.finance.constant;

import java.util.Objects;

public enum FinanceBalanceDirectionEnum {
    LEND(1, "借"),
    LOAN(2, "贷"),
    NULL(99, null),
    ;

    private int type;

    private String name;

    FinanceBalanceDirectionEnum(int type, String name) {
        this.type = type;
        this.name = name;
    }

    public static FinanceBalanceDirectionEnum parseName(String name) {
        for (FinanceBalanceDirectionEnum nameEnum : FinanceBalanceDirectionEnum.values()) {
            if (Objects.equals(nameEnum.getName(), name)) {
                return nameEnum;
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
