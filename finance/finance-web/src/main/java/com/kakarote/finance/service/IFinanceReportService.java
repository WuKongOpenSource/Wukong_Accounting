package com.kakarote.finance.service;

import com.alibaba.fastjson.JSONObject;
import com.kakarote.finance.entity.BO.FinanceReportRequestBO;

/**
 * @description:
 * @author: zjj
 * @date: 2021-08-30 15:20
 */
public interface IFinanceReportService {

    /**
     * 获取表达式的值
     *
     * @param requestBO
     * @return
     */
    JSONObject filterByExpression(FinanceReportRequestBO requestBO);
}
