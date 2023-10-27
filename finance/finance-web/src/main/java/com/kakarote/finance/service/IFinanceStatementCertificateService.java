package com.kakarote.finance.service;

import com.kakarote.core.servlet.BaseService;
import com.kakarote.finance.entity.PO.FinanceStatementCertificate;

/**
 * <p>
 * 结账和凭证关联表 服务类
 * </p>
 *
 * @author zhangzhiwei
 * @since 2021-08-27
 */
public interface IFinanceStatementCertificateService extends BaseService<FinanceStatementCertificate> {

    FinanceStatementCertificate getByPeriod(String period, Long accountId);

    boolean lossSubjectSettled(String period);


}
