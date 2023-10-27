package com.kakarote.finance.mapper;

import com.kakarote.core.servlet.BaseMapper;
import com.kakarote.finance.entity.PO.FinanceStatementCertificate;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 * 结账和凭证关联表 Mapper 接口
 * </p>
 *
 * @author zhangzhiwei
 * @since 2021-08-27
 */
public interface FinanceStatementCertificateMapper extends BaseMapper<FinanceStatementCertificate> {

    @Select(GET_BY_PERIOD)
    FinanceStatementCertificate getByPeriod(@Param("period") String period, @Param("accountId") Long accountId);

    String GET_BY_PERIOD = "select b.* from wk_finance_statement a left join wk_finance_statement_certificate b\n" +
            "    on a.statement_id = b.statement_id  and a.account_id = b.account_id\n" +
            "where date_format(b.certificate_time, '%Y%m') = #{period}  and a.account_id =#{accountId} and a.statement_type = 2 " +
            "limit 1";

}
