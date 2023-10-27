package com.kakarote.finance.service.impl;

import com.kakarote.core.servlet.BaseServiceImpl;
import com.kakarote.finance.entity.PO.FinanceSubjectCurrency;
import com.kakarote.finance.mapper.FinanceSubjectCurrencyMapper;
import com.kakarote.finance.service.IFinanceSubjectCurrencyService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 项目和币种关联表 服务实现类
 * </p>
 *
 * @author zhangzhiwei
 * @since 2021-08-24
 */
@Service
public class FinanceSubjectCurrencyServiceImpl extends BaseServiceImpl<FinanceSubjectCurrencyMapper, FinanceSubjectCurrency> implements IFinanceSubjectCurrencyService {

}
