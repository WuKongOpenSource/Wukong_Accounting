package com.kakarote.finance.service;

import com.kakarote.core.servlet.BaseService;
import com.kakarote.finance.entity.BO.FinanceSearchCertificateBO;
import com.kakarote.finance.entity.PO.FinanceCertificateDetail;

import java.util.List;

/**
 * <p>
 * 凭证详情 服务类
 * </p>
 *
 * @author zhangzhiwei
 * @since 2021-08-25
 */
public interface IFinanceCertificateDetailService extends BaseService<FinanceCertificateDetail> {

    /**
     * 根据条件查询凭证id
     */
    public List<Long> queryIdsByCondition(FinanceSearchCertificateBO searchCertificateBO);

}
