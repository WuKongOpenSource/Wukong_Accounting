package com.kakarote.finance.mapper;

import com.kakarote.core.servlet.BaseMapper;
import com.kakarote.finance.entity.BO.FinanceSearchCertificateBO;
import com.kakarote.finance.entity.PO.FinanceCertificateDetail;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 凭证详情 Mapper 接口
 * </p>
 *
 * @author zhangzhiwei
 * @since 2021-08-25
 */
public interface FinanceCertificateDetailMapper extends BaseMapper<FinanceCertificateDetail> {

    public List<Long> queryIdsByCondition(@Param("data") FinanceSearchCertificateBO searchCertificateBO);

}
