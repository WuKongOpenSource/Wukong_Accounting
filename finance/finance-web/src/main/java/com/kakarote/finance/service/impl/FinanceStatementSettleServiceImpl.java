package com.kakarote.finance.service.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjectUtil;
import com.kakarote.core.servlet.BaseServiceImpl;
import com.kakarote.finance.common.AccountSet;
import com.kakarote.finance.entity.PO.FinanceParameter;
import com.kakarote.finance.entity.PO.FinanceStatementSettle;
import com.kakarote.finance.mapper.FinanceStatementSettleMapper;
import com.kakarote.finance.service.IFinanceParameterService;
import com.kakarote.finance.service.IFinanceStatementSettleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * <p>
 * 结账清单表 服务实现类
 * </p>
 *
 * @author zhangzhiwei
 * @since 2021-08-27
 */
@Service
public class FinanceStatementSettleServiceImpl extends BaseServiceImpl<FinanceStatementSettleMapper, FinanceStatementSettle> implements IFinanceStatementSettleService {

    @Autowired
    private IFinanceParameterService parameterService;

    @Override
    public FinanceStatementSettle getByPeriod(String period, Long accountId) {
        return baseMapper.getByPeriod(period,accountId);
    }

    @Override
    public String getCurrentPeriod() {
        FinanceStatementSettle settle = lambdaQuery()
                .eq(FinanceStatementSettle::getAccountId, AccountSet.getAccountSetId())
                .orderByDesc(FinanceStatementSettle::getSettleTime).one();
        LocalDateTime date = LocalDateTimeUtil.now();
        if (ObjectUtil.isNotNull(settle)) {
            date = settle.getSettleTime();
            date = LocalDateTimeUtil.offset(date, 1, ChronoUnit.MONTHS);
        } else {
            FinanceParameter parameter = parameterService.lambdaQuery()
                    .eq(FinanceParameter::getAccountId, AccountSet.getAccountSetId())
                    .one();
            if (ObjectUtil.isNotNull(parameter)) {
                date = parameter.getStartTime();
            }
        }
        return LocalDateTimeUtil.format(date, DatePattern.SIMPLE_MONTH_PATTERN);
    }
}
