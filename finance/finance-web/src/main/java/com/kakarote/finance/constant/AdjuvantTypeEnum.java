package com.kakarote.finance.constant;


/**
 * @author zhangzhiwei
 * crm模块枚举
 */

public enum AdjuvantTypeEnum {
    /**
     * 线索
     */
    CUSTOMER(1, "客户"),
    /**
     * 客户
     */
    SUPPLIER(2, "供应商"),
    /**
     * 联系人
     */
    STAFF(3, "职员"),
    /**
     * 产品
     */
    PROJECT(4, "项目"),
    /**
     * 商机
     */
    DEPT(5, "部门"),
    /**
     * 合同
     */
    STOCK(6, "存货"),
    /**
     * 自定义
     */
    CUSTOM(7, "自定义"),
    /**
     * NULL
     */
    NULL(0, "");

    AdjuvantTypeEnum(Integer type, String remarks) {
        this.type = type;
        this.remarks = remarks;
    }

    private Integer type;
    private String remarks;

    public String getRemarks() {
        return remarks;
    }

    public Integer getType() {
        return type;
    }

    public static AdjuvantTypeEnum parse(Integer type) {
        for (AdjuvantTypeEnum crmEnum : AdjuvantTypeEnum.values()) {
            if (crmEnum.getType().equals(type)) {
                return crmEnum;
            }
        }
        return NULL;
    }

    public static AdjuvantTypeEnum parse(String name) {
        for (AdjuvantTypeEnum crmEnum : AdjuvantTypeEnum.values()) {
            if (crmEnum.name().equals(name)) {
                return crmEnum;
            }
        }
        return NULL;
    }

    public String getIndex() {
        return name().toLowerCase();
    }

    public String getTableName() {
        return name().toLowerCase();
    }

    @Override
    public String toString() {
        return this.type.toString();
    }
}
