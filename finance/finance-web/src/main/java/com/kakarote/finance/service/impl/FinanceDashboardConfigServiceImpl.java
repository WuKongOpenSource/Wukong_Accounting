package com.kakarote.finance.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.kakarote.common.utils.UserUtil;
import com.kakarote.core.servlet.BaseServiceImpl;
import com.kakarote.finance.common.AccountSet;
import com.kakarote.finance.entity.PO.FinanceDashboardConfig;
import com.kakarote.finance.mapper.FinanceDashboardConfigMapper;
import com.kakarote.finance.service.IFinanceDashboardConfigService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * @description:
 * @author: zjj
 * @date: 2021-09-02 17:47
 */
@Service
public class FinanceDashboardConfigServiceImpl extends BaseServiceImpl<FinanceDashboardConfigMapper,
        FinanceDashboardConfig> implements IFinanceDashboardConfigService {

    @Override
    public FinanceDashboardConfig getDashboardConfig() {
        FinanceDashboardConfig dashboardConfig = lambdaQuery()
                .eq(FinanceDashboardConfig::getAccountId, AccountSet.getAccountSetId())
                .eq(FinanceDashboardConfig::getCreateUserId, UserUtil.getUserId())
                .one();
        if (ObjectUtil.isNull(dashboardConfig)) {
            dashboardConfig = new FinanceDashboardConfig();
            dashboardConfig.setAccountId(AccountSet.getAccountSetId());
            dashboardConfig.setCreateUserId(UserUtil.getUserId());
            dashboardConfig.setCreateTime(LocalDateTime.now());
            dashboardConfig.setConfig("001,006,004");
            save(dashboardConfig);
        }
        return dashboardConfig;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateConfig(FinanceDashboardConfig config) {
        lambdaUpdate()
                .eq(FinanceDashboardConfig::getAccountId, AccountSet.getAccountSetId())
                .eq(FinanceDashboardConfig::getCreateUserId, UserUtil.getUserId())
                .remove();
        config.setAccountId(AccountSet.getAccountSetId());
        config.setCreateUserId(UserUtil.getUserId());
        config.setCreateTime(LocalDateTime.now());
        config.setId(null);
        save(config);
    }
}
