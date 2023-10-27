package com.kakarote.finance.service;

import cn.hutool.core.util.ObjectUtil;
import com.kakarote.core.servlet.BaseService;
import com.kakarote.finance.entity.PO.FinanceCashFlowStatementExtendConfig;

import java.util.List;

/**
 * @author 10323
 * @description: 现金流量扩展表
 * @date 2021/8/2910:35
 */
public interface IFinanceCashFlowStatementExtendConfigService extends BaseService<FinanceCashFlowStatementExtendConfig> {

    default FinanceCashFlowStatementExtendConfig filterConfig(List<FinanceCashFlowStatementExtendConfig> configs, Integer sort) {
        return configs.stream().filter(c -> ObjectUtil.equal(sort, c.getSort())).findFirst().orElse(null);
    }
}
