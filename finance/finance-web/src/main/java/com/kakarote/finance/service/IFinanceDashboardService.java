package com.kakarote.finance.service;

import com.alibaba.fastjson.JSONObject;

import java.util.List;

/**
 * @description:仪表盘
 * @author: zjj
 * @date: 2021-09-02 11:13
 */
public interface IFinanceDashboardService {

    /**
     * 利润表统计
     *
     * @return
     */
    List<JSONObject> incomeStatement(String strDate);
}
