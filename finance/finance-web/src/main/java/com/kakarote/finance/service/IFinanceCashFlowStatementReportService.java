package com.kakarote.finance.service;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.kakarote.core.servlet.BaseService;
import com.kakarote.finance.entity.BO.FinanceCashFlowStatementUpdateBO;
import com.kakarote.finance.entity.BO.FinanceReportRequestBO;
import com.kakarote.finance.entity.PO.FinanceCashFlowStatementReport;

import java.util.List;

/**
 * @author 10323
 * @description: 现金流量表数据
 * @date 2021/8/2918:29
 */
public interface IFinanceCashFlowStatementReportService extends BaseService<FinanceCashFlowStatementReport> {

    /**
     * 报表数据
     *
     * @param requestBO
     * @return
     */
    List<JSONObject> report(FinanceReportRequestBO requestBO);

    void updateReports(FinanceCashFlowStatementUpdateBO updateBO);

    /**
     * 筛选报表数据
     *
     * @param report
     * @param targetSort
     * @return
     */
    default JSONObject filterReport(List<JSONObject> report, Integer targetSort) {
        return report.stream().filter(r -> ObjectUtil.equal(targetSort, r.getInteger("sort"))).findFirst().orElse(null);
    }

    /**
     * 现金流量表平衡结果
     *
     * @param requestBO
     * @return
     */
    JSONObject balanceCheck(FinanceReportRequestBO requestBO);

    /**
     * 导出现金流量表
     *
     * @param requestBO
     */
    void exportCashFlowStatementReport(FinanceReportRequestBO requestBO);

}
