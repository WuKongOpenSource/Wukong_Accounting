package com.kakarote.finance.service;


import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.kakarote.core.servlet.BaseService;
import com.kakarote.finance.entity.BO.FinanceReportRequestBO;
import com.kakarote.finance.entity.BO.FinanceUpdateReportConfigBO;
import com.kakarote.finance.entity.PO.FinanceIncomeStatementReport;

import java.util.List;

/**
 * 利润表
 */
public interface IFinanceIncomeStatementReportService extends BaseService<FinanceIncomeStatementReport>, IFinanceReportService {

    /**
     * 报表数据
     *
     * @param requestBO
     * @return
     */
    List<JSONObject> report(FinanceReportRequestBO requestBO);

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
     * 利润表平衡结果
     *
     * @param requestBO
     * @return
     */
    JSONObject balanceCheck(FinanceReportRequestBO requestBO);

    /**
     * 修改利润表配置
     *
     * @param configBO
     */
    void updateIncomeStatementConfig(FinanceUpdateReportConfigBO configBO);

    /**
     * 导出利润表
     *
     * @param requestBO
     */
    void exportIncomeStatementReport(FinanceReportRequestBO requestBO);
}
