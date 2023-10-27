package com.kakarote.finance.service;

import com.kakarote.core.servlet.BaseService;
import com.kakarote.finance.entity.PO.FinanceStatementSettle;

/**
 * <p>
 * 结账清单表 服务类
 * </p>
 *
 * @author zhangzhiwei
 * @since 2021-08-27
 */
public interface IFinanceStatementSettleService extends BaseService<FinanceStatementSettle> {

    FinanceStatementSettle getByPeriod(String period, Long accountId);

    String getCurrentPeriod();

}
