package com.kakarote.finance.service;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.kakarote.core.servlet.BaseService;
import com.kakarote.finance.entity.BO.FinanceFormulaBO;
import com.kakarote.finance.entity.BO.FinanceReportRequestBO;
import com.kakarote.finance.entity.BO.FinanceUpdateReportConfigBO;
import com.kakarote.finance.entity.PO.FinanceBalanceSheetReport;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 资产负债表
 */
public interface IFinanceBalanceSheetReportService extends BaseService<FinanceBalanceSheetReport>, IFinanceReportService {

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
        return report.stream().filter(r -> ObjectUtil.equal(targetSort, r.getInteger("sort")))
                .findFirst().orElse(null);
    }

    /**
     * 筛选报表数据
     *
     * @param report
     * @param subjectNumber
     * @return
     */
    default JSONObject filterReport(List<JSONObject> report, String subjectNumber) {
        return report.stream().filter(r -> StrUtil.contains(r.getString("formula"), subjectNumber))
                .findFirst().orElse(null);
    }


    /**
     * 资产负债表平衡结果
     *
     * @param requestBO
     * @return
     */
    JSONObject balanceCheck(FinanceReportRequestBO requestBO);

    /**
     * 修改资产负债表配置
     *
     * @param configBO
     */
    void updateBalanceSheetConfig(FinanceUpdateReportConfigBO configBO);

    /**
     * 导出资产负债表
     *
     * @param requestBO
     */
    void exportBalanceSheetReport(FinanceReportRequestBO requestBO);

    BigDecimal calculateEndValue(FinanceFormulaBO formulaBO, Map<Long, JSONObject> subjectBalanceMap);

}
