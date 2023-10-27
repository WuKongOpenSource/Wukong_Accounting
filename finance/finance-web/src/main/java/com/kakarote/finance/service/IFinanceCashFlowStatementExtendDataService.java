package com.kakarote.finance.service;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.kakarote.core.servlet.BaseService;
import com.kakarote.finance.entity.BO.FinanceCashFlowStatementExtendDataUpdateBO;
import com.kakarote.finance.entity.BO.FinanceFormulaBO;
import com.kakarote.finance.entity.BO.FinanceReportRequestBO;
import com.kakarote.finance.entity.BO.FinanceUpdateReportConfigBO;
import com.kakarote.finance.entity.PO.FinanceCashFlowStatementExtendData;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: zjj
 * @date: 2021-08-30 09:32
 */
public interface IFinanceCashFlowStatementExtendDataService extends BaseService<FinanceCashFlowStatementExtendData> {

    List<FinanceCashFlowStatementExtendData> queryList(FinanceReportRequestBO requestBO);

    /**
     * 编辑扩展数据
     *
     * @param updateBO
     */
    void updateData(FinanceCashFlowStatementExtendDataUpdateBO updateBO);

    /**
     * 修改现金流量表扩展公式
     *
     * @param configBO
     */
    void updateCashFlowStatementExtendConfig(FinanceUpdateReportConfigBO configBO);

    BigDecimal calculateCurrentValue(FinanceFormulaBO formulaBO, Map<Long, JSONObject> subjectBalanceMap);

    default FinanceCashFlowStatementExtendData filterData(List<FinanceCashFlowStatementExtendData> dataList,
                                                          Integer sort) {
        return dataList.stream().filter(c -> ObjectUtil.equal(sort, c.getSort())).findFirst().orElse(null);
    }
}
