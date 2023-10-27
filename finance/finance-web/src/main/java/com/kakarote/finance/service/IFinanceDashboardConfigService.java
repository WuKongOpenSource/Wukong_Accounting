package com.kakarote.finance.service;

import com.kakarote.core.servlet.BaseService;
import com.kakarote.finance.entity.PO.FinanceDashboardConfig;

/**
 * @description:仪表盘设置
 * @author: zjj
 * @date: 2021-09-02 17:46
 */
public interface IFinanceDashboardConfigService extends BaseService<FinanceDashboardConfig> {

    FinanceDashboardConfig getDashboardConfig();

    void updateConfig(FinanceDashboardConfig config);
}
