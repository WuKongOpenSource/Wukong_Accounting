package com.kakarote.finance.service.impl;

import com.kakarote.core.servlet.BaseServiceImpl;
import com.kakarote.finance.common.AccountSet;
import com.kakarote.finance.entity.PO.FinanceFlows;
import com.kakarote.finance.mapper.FinanceFlowsMapper;
import com.kakarote.finance.service.IFinanceFlowsService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 现金流量初始余额 服务实现类
 * </p>
 *
 * @author zhangzhiwei
 * @since 2021-08-24
 */
@Service
public class FinanceFlowsServiceImpl extends BaseServiceImpl<FinanceFlowsMapper, FinanceFlows> implements IFinanceFlowsService {

    @Override
    public void updateByIds(List<FinanceFlows> flows) {
        for (FinanceFlows financeFlows : flows) {
            financeFlows.setAccountId(AccountSet.getAccountSetId());
        }
        saveOrUpdateBatch(flows);
    }

    @Override
    public List<FinanceFlows> queryList() {
        return lambdaQuery().eq(FinanceFlows::getAccountId, AccountSet.getAccountSetId()).list();
    }

}
