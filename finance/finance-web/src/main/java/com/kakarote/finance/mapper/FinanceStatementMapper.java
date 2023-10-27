package com.kakarote.finance.mapper;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.kakarote.core.servlet.BaseMapper;
import com.kakarote.finance.entity.BO.FinanceDetailAccountBO;
import com.kakarote.finance.entity.BO.FinanceStatementCertificateBO;
import com.kakarote.finance.entity.PO.FinanceStatement;
import com.kakarote.finance.entity.VO.FinanceStatementVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 结账表 Mapper 接口
 * </p>
 *
 * @author zhangzhiwei
 * @since 2021-08-27
 */
public interface FinanceStatementMapper extends BaseMapper<FinanceStatement> {

    public List<JSONObject> queryList(@Param("settleTime") String settleTime, @Param("accountId") Long accountId);

    /**
     * 判断是否符合生成增值税
     */
    public JSONObject appreciationJudgeStatement(@Param("settleTime") String settleTime
            , @Param("subjectIds") List<Long> subjectIds);

    @InterceptorIgnore(tenantLine = "true")
    public JSONObject queryDetailBalanceAccount(@Param("data") FinanceDetailAccountBO accountBO);

    public Integer queryByTime(@Param("data") FinanceStatementVO statementVO);

    public List<JSONObject> querySubject(@Param("statementId") Long statementId, @Param("accountId") Long accountId);

    public List<JSONObject> queryListByIds(@Param("data") FinanceStatementCertificateBO statementCertificateBO);

    public List<JSONObject> queryCertificate(@Param("statementId") Long statementId,
                                             @Param("settleTime") String settleTime,
                                             @Param("accountId") Long accountId);

    public JSONObject queryBalance(@Param("statementId") Long statementId, @Param("settleTime") String settleTime, @Param("accountId") Long accountId);

    public JSONObject queryProfitBalance(@Param("statementId") Long statementId, @Param("settleTime") String settleTime, @Param("accountId") Long accountId);

    public JSONObject queryBalanceBySubject(@Param("statementId") Long statementId, @Param("settleTime") String settleTime, @Param("accountId") Long accountId);

    public Integer queryProfitSubjectCount(@Param("settleTime") String settleTime, @Param("accountId") Long accountId);

    public Integer queryCertificateCheckStatusCount(@Param("settleTime") String settleTime, @Param("accountId") Long accountId);

    public JSONObject queryIsPayments(@Param("settleTime") String settleTime, @Param("accountId") Long accountId);

    public List<Integer> queryCertificateNumsByVoucherId(@Param("settleTime") String settleTime,
                                                         @Param("accountId") Long accountId,
                                                         @Param("voucherId") Long voucherId);

    /**
     * 根据账期查询损益类科目凭证详情
     *
     * @param period    格式：yyyyMM
     * @param accountId
     * @return
     */
    @Select(QUERY_LOSS_DETAIL_BY_PERIOD_SQL)
    List<JSONObject> queryLossDetailByPeriod(@Param("period") String period, @Param("accountId") Long accountId);

    String QUERY_LOSS_DETAIL_BY_PERIOD_SQL = "select a.*\n" +
            "from wk_finance_certificate_detail as a\n" +
            "         left join wk_finance_subject as b on a.subject_id = b.subject_id and a.account_id = b.account_id\n" +
            "         left join wk_finance_certificate as c on a.certificate_id = c.certificate_id and b.account_id = c.account_id\n" +
            "where a.account_id =                            #{accountId}\n" +
            "  and DATE_FORMAT(c.certificate_time, '%Y%m') = #{period}\n" +
            "  and b.type = 5";
}
