package com.kakarote.finance.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.kakarote.finance.common.AccountSet;
import com.kakarote.finance.common.PeriodUtils;
import com.kakarote.finance.entity.BO.FinanceReportRequestBO;
import com.kakarote.finance.service.IFinanceDashboardService;
import com.kakarote.finance.service.IFinanceIncomeStatementReportService;
import com.kakarote.finance.service.IFinanceParameterService;
import com.kakarote.finance.service.IFinanceStatementSettleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @description:仪表盘
 * @author: zjj
 * @date: 2021-09-02 11:14
 */
@Service
public class FinanceDashboardServiceImpl implements IFinanceDashboardService {

    @Autowired
    private IFinanceIncomeStatementReportService incomeStatementService;

    @Autowired
    private IFinanceStatementSettleService settleService;

    @Autowired
    private IFinanceParameterService parameterService;

    @Override
    public List<JSONObject> incomeStatement(String strDate) {
        Long accountSetId = AccountSet.getAccountSetId();
        if (ObjectUtil.isNull(accountSetId)) {
            return CollUtil.newArrayList();
        }
        List<String> periods = new ArrayList<>();
        if (StrUtil.isNotEmpty(strDate)) {
            periods.add(strDate);
        } else {
            String currentPeriod = settleService.getCurrentPeriod();
            periods = PeriodUtils.getOneYearPeriod(currentPeriod);
        }
        // 初始账期
        String startPeriod = parameterService.getStartPeriod();
        FinanceReportRequestBO requestBO = new FinanceReportRequestBO();
        requestBO.setType(1);
        requestBO.setAccountId(AccountSet.getAccountSetId());
        List<JSONObject> result = new ArrayList<>();
        for (String period : periods) {
            List<JSONObject> report = new ArrayList<>();
            if (PeriodUtils.isAfter(period, startPeriod)) {
                requestBO.setFromPeriod(period);
                requestBO.setToPeriod(period);
                report = incomeStatementService.report(requestBO);
            }
            // 营业收入
            JSONObject o1 = incomeStatementService.filterReport(report, 1);
            // 营业成本
            JSONObject o2 = incomeStatementService.filterReport(report, 2);
            // 税金及附加
            JSONObject o3 = incomeStatementService.filterReport(report, 3);
            // 利润总额
            JSONObject o4 = incomeStatementService.filterReport(report, 30);
            // 销售费用
            JSONObject o5 = incomeStatementService.filterReport(report, 11);
            // 管理费用
            JSONObject o6 = incomeStatementService.filterReport(report, 14);
            // 财务费用
            JSONObject o7 = incomeStatementService.filterReport(report, 18);

            BigDecimal income = new BigDecimal(0);
            BigDecimal firstCost = new BigDecimal(0);
            BigDecimal profit = new BigDecimal(0);
            BigDecimal cost = new BigDecimal(0);
            BigDecimal other = new BigDecimal(0);
            // 收入= 营业收入
            if (ObjectUtil.isNotEmpty(o1)) {
                income = o1.getBigDecimal("monthValue");
            } else {
                income = BigDecimal.ZERO;
            }
            // 成本=营业成本+税金及附加
            if (ObjectUtil.isNotEmpty(o2) && ObjectUtil.isNotEmpty(o3)) {
                firstCost = o2.getBigDecimal("monthValue").add(o3.getBigDecimal("monthValue"));
            } else {
                firstCost = BigDecimal.ZERO;
            }

            // 利润=利润总额
            if (ObjectUtil.isNotEmpty(o4)) {
                profit = o4.getBigDecimal("monthValue");
            } else {
                profit = BigDecimal.ZERO;
            }
            // 费用=销售费用+管理费用+财务费用
            if (ObjectUtil.isNotEmpty(o5) && ObjectUtil.isNotEmpty(o6) && ObjectUtil.isNotEmpty(o7)) {
                cost = o5.getBigDecimal("monthValue").add(o6.getBigDecimal("monthValue")).add(o7.getBigDecimal(
                        "monthValue"));
            } else {
                cost = BigDecimal.ZERO;
            }
            // 其他=利润+费用+成本-收入
            other = profit.add(cost).add(firstCost).subtract(income);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("income", income);
            jsonObject.put("firstCost", firstCost);
            jsonObject.put("profit", profit);
            jsonObject.put("cost", cost);
            jsonObject.put("other", other);
            jsonObject.put("period", period);
            result.add(jsonObject);
        }
        return result;
    }
}
