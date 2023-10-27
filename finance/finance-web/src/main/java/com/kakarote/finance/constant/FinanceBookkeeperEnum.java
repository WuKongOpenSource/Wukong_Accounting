package com.kakarote.finance.constant;

import cn.hutool.core.util.ObjectUtil;

public enum FinanceBookkeeperEnum {
    TYPE_0(1, "小企业会计准则（2013年颁）"),
    NULL(99, null),
    ;

    private int type;

    private String name;

    FinanceBookkeeperEnum(int type, String name) {
        this.type = type;
        this.name = name;
    }

    public static FinanceBookkeeperEnum parseName(String name) {
        for (FinanceBookkeeperEnum bookkeeperEnum : FinanceBookkeeperEnum.values()) {
            if (ObjectUtil.equal(name, bookkeeperEnum.getName())) {
                return bookkeeperEnum;
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
