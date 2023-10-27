package com.kakarote.finance.service.impl;

import com.kakarote.common.log.entity.OperationLog;
import com.kakarote.common.utils.UserUtil;
import com.kakarote.core.servlet.BaseServiceImpl;
import com.kakarote.finance.common.AccountSet;
import com.kakarote.finance.entity.PO.FinanceCurrency;
import com.kakarote.finance.mapper.FinanceCurrencyMapper;
import com.kakarote.finance.service.IFinanceCurrencyService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 币种 服务实现类
 * </p>
 *
 * @author zhangzhiwei
 * @since 2021-08-24
 */
@Service
public class FinanceCurrencyServiceImpl extends BaseServiceImpl<FinanceCurrencyMapper, FinanceCurrency> implements IFinanceCurrencyService {

    @Override
    public OperationLog saveAndUpdate(FinanceCurrency currency) {
        OperationLog operationLog = new OperationLog();
        if (currency.getCurrencyId() == null) {
            currency.setCreateTime(LocalDateTime.now());
            currency.setCreateUserId(UserUtil.getUserId());
            currency.setAccountId(AccountSet.getAccountSetId());
            save(currency);
            operationLog.setOperationObject(currency.getCurrencyName());
            operationLog.setOperationInfo("新建币种：" + currency.getCurrencyName());
        } else {
            updateById(currency);
            operationLog.setOperationObject(currency.getCurrencyName());
            operationLog.setOperationInfo("编辑币种：" + currency.getCurrencyName());
        }
        return operationLog;
    }

    @Override
    public List<FinanceCurrency> queryList() {
        return lambdaQuery().eq(FinanceCurrency::getStatus, 1).eq(FinanceCurrency::getAccountId, AccountSet.getAccountSetId()).list();
    }

    @Override
    public List<FinanceCurrency> queryListByAccountId(Long id) {
        return lambdaQuery().eq(FinanceCurrency::getStatus, 1).eq(FinanceCurrency::getAccountId, id).list();
    }

    @Override
    public OperationLog deleteById(Long id) {
        OperationLog operationLog = new OperationLog();
        FinanceCurrency financeCurrency = getById(id);
        operationLog.setOperationObject(financeCurrency.getCurrencyName());
        operationLog.setOperationInfo("删除币种：" + financeCurrency.getCurrencyName());
        lambdaUpdate().set(FinanceCurrency::getStatus, 3).eq(FinanceCurrency::getCurrencyId, id).update();
        return operationLog;
    }

    /**
     * 获取本位币名称，（导出用）
     *
     * @param id
     * @return
     */
    @Override
    public String getCurrencyName(Long id) {
        return lambdaQuery().eq(FinanceCurrency::getStatus, 1).eq(FinanceCurrency::getCurrencyId, id).one().getCurrencyName();
    }
}
