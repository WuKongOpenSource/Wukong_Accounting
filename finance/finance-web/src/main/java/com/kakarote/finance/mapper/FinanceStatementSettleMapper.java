package com.kakarote.finance.mapper;

import com.kakarote.core.servlet.BaseMapper;
import com.kakarote.finance.entity.PO.FinanceStatementSettle;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 * 结账清单表 Mapper 接口
 * </p>
 *
 * @author zhangzhiwei
 * @since 2021-08-27
 */
public interface FinanceStatementSettleMapper extends BaseMapper<FinanceStatementSettle> {

    @Select(GET_BT_PERIOD_SQL)
    FinanceStatementSettle getByPeriod(@Param("period") String period, @Param("accountId") Long accountId);

    String GET_BT_PERIOD_SQL = "select * from wk_finance_statement_settle where date_format(settle_time, '%Y%m') = #{period}  and account_id = #{accountId}";

}
