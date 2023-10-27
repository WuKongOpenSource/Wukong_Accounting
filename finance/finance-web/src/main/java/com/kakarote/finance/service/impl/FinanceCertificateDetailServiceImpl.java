package com.kakarote.finance.service.impl;

import com.kakarote.core.servlet.BaseServiceImpl;
import com.kakarote.finance.common.AccountSet;
import com.kakarote.finance.entity.BO.FinanceSearchCertificateBO;
import com.kakarote.finance.entity.PO.FinanceCertificateDetail;
import com.kakarote.finance.mapper.FinanceCertificateDetailMapper;
import com.kakarote.finance.service.IFinanceCertificateDetailService;
import com.kakarote.finance.service.IFinanceSubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 凭证详情 服务实现类
 * </p>
 *
 * @author zhangzhiwei
 * @since 2021-08-25
 */
@Service
public class FinanceCertificateDetailServiceImpl extends BaseServiceImpl<FinanceCertificateDetailMapper, FinanceCertificateDetail> implements IFinanceCertificateDetailService {

    @Autowired
    private IFinanceSubjectService financeSubjectService;

    @Override
    public List<Long> queryIdsByCondition(FinanceSearchCertificateBO searchCertificateBO) {
        if (searchCertificateBO.getSubjectId() != null) {
            List<Long> subjectIds = financeSubjectService.queryIds(searchCertificateBO.getSubjectId(), null, null);
            searchCertificateBO.setSubjectIds(subjectIds);
        }
        searchCertificateBO.setAccountId(AccountSet.getAccountSetId());
        return getBaseMapper().queryIdsByCondition(searchCertificateBO);
    }
}
