package com.kakarote.finance.mapper;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.kakarote.core.entity.BasePage;
import com.kakarote.core.servlet.BaseMapper;
import com.kakarote.finance.entity.BO.*;
import com.kakarote.finance.entity.PO.FinanceCertificate;
import com.kakarote.finance.entity.PO.FinanceCertificateDetail;
import com.kakarote.finance.entity.VO.FinanceCertificateVO;
import com.kakarote.finance.entity.VO.FinanceSubjectVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 凭证表 Mapper 接口
 * </p>
 *
 * @author zhangzhiwei
 * @since 2021-08-25
 */
public interface FinanceCertificateMapper extends BaseMapper<FinanceCertificate> {

    /**
     * 查询通话记录列表
     */
    @InterceptorIgnore(tenantLine = "true")
    BasePage<FinanceCertificateVO> pageCallRecordList(BasePage<FinanceCertificateVO> page, @Param("data") FinanceSearchCertificateBO searchCertificateBO);

    /**
     * 查询凭证详情
     */
    public List<FinanceCertificateDetail> queryCertificateDetailById(@Param("data") FinanceCertificateVO vo);

    public List<Long> querySubjectIds2(@Param("data") FinanceCollectCertificateBO certificateBO);

    /**
     * @param certificateBO
     * @return
     */
    public List<Long> querySubjectIdsByTime(@Param("data") FinanceCollectCertificateBO certificateBO);

    public JSONObject queryBalanceBySubjectIds(@Param("ids") List<Long> ids,
                                               @Param("subjectIds") List<Long> subjectIds,
                                               @Param("accountId") Long accountId, @Param("data") FinanceCollectCertificateBO certificateBO);

    @InterceptorIgnore(tenantLine = "true")
    public List<JSONObject> queryDetailAccountBySubjectIds(@Param("data") FinanceDetailAccountBO accountBO);


    @InterceptorIgnore(tenantLine = "true")
    public List<JSONObject> queryDetailUpAccount(@Param("data") FinanceDetailAccountBO accountBO);

    public List<Long> queryDebtorSubjectIds(@Param("data") FinanceDetailAccountBO accountBO);

    @InterceptorIgnore(tenantLine = "true")
    public List<JSONObject> queryDiversification(@Param("data") FinanceDetailAccountBO accountBO);

    @InterceptorIgnore(tenantLine = "true")
    public List<JSONObject> queryListDetailBalanceAccount(@Param("data") FinanceDetailAccountBO accountBO);

    public FinanceCertificate queryByTime(@Param("data") FinanceCertificate certificate);

    /**
     * 根据条件查询符合条件的凭证
     */
    public FinanceCertificate queryCertificate(@Param("data") FinanceCertificateInsertBO insertBO);

    /**
     * 修改凭证信息
     */
    public void updateCertificate(@Param("data") FinanceCertificateInsertBO insertBO);

    /**
     * 获取当前账套最早录凭证日期和最晚录凭证日期
     */
    @InterceptorIgnore(tenantLine = "true")
    public JSONObject queryCertificateTime(@Param("accountId") Long accountId);

    public List<FinanceCertificate> queryListByNum(@Param("data") FinanceCertificateSettleBO settleBO);

    public Integer queryBalanceByIds(@Param("ids") List<Long> ids,
                                     @Param("accountId") Long accountId);


    /**
     * 查询账期的发生额-按照科目和月份分組
     *
     * @param accountId
     * @param fromPeriod
     * @param toPeriod
     * @return
     */
    List<JSONObject> queryCurrentBalance(@Param("accountId") Long accountId,
                                         @Param("fromPeriod") String fromPeriod,
                                         @Param("toPeriod") String toPeriod);

    /**
     * 查询账期的发生额-按照科目和月份分組-不含结转凭证
     *
     * @param accountId
     * @param fromPeriod
     * @param toPeriod
     * @return
     */
    List<JSONObject> queryCurrentBalanceNoSettle(@Param("accountId") Long accountId,
                                                 @Param("fromPeriod") String fromPeriod,
                                                 @Param("toPeriod") String toPeriod);

    /***
     * 查询账期的发生额-含结转凭证
     *
     * @param accountId
     * @param fromPeriod
     * @param toPeriod
     * @return
     */
    List<JSONObject> queryAccumulatedAmount(@Param("accountId") Long accountId,
                                            @Param("fromPeriod") String fromPeriod,
                                            @Param("toPeriod") String toPeriod);

    /***
     * 查询账期的发生额-不含结转凭证
     *
     * @param accountId
     * @param fromPeriod
     * @param toPeriod
     * @return
     */
    List<JSONObject> queryAccumulatedAmountNoSettle(@Param("accountId") Long accountId,
                                                    @Param("fromPeriod") String fromPeriod,
                                                    @Param("toPeriod") String toPeriod);

    /**
     * 查詢辅助核算的账期发生额
     *
     * @param accountId
     * @param fromPeriod
     * @param toPeriod
     * @return
     */
    List<JSONObject> queryAccumulatedAmountAssist(@Param("accountId") Long accountId,
                                                  @Param("fromPeriod") String fromPeriod,
                                                  @Param("toPeriod") String toPeriod);


    /**
     * 查询辅助核算科目
     *
     * @param accountId
     * @return
     */
    @Select(SQL_QUERY_SUBJECT_ASSIST)
    List<JSONObject> querySubjectAssist(@Param("accountId") Long accountId);

    String SQL_QUERY_SUBJECT_ASSIST = "select subject_id,\n" +
            "       assist_id,\n" +
            "       number                                                            subjectNumber,\n" +
            "       concat(number, '_', group_concat(carte_number separator '_'))     number,\n" +
            "       concat(subject_name, '_', group_concat(carte_name separator '_')) subject_name,\n" +
            "       balance_direction,\n" +
            "       true                                                              isAssist,\n" +
            "       grade\n" +
            "from (select distinct a.subject_id,\n" +
            "                      b.assist_id,\n" +
            "                      a.number,\n" +
            "                      a.subject_name,\n" +
            "                      e.label,\n" +
            "                      f.carte_number,\n" +
            "                      f.carte_name,\n" +
            "                      f.adjuvant_id,\n" +
            "                      f.carte_id,\n" +
            "                      a.balance_direction,\n" +
            "                      a.grade,\n" +
            "                      true isAssist\n" +
            "      from wk_finance_subject a\n" +
            "               left join wk_finance_assist as b on b.subject_id = a.subject_id\n" +
            "               left join wk_finance_assist_adjuvant e on e.assist_id = b.assist_id\n" +
            "               left join wk_finance_adjuvant_carte f on e.relation_id = f.carte_id\n" +
            "      where a.account_id = #{accountId}\n" +
            "        and b.assist_id is not null\n" +
            "      order by number, label, carte_number) a\n" +
            "group by subject_id, assist_id";

    @InterceptorIgnore(tenantLine = "true")
    public List<JSONObject> queryAmountDetailAccount(@Param("data") FinanceDetailAccountBO accountBO);

    @InterceptorIgnore(tenantLine = "true")
    public JSONObject queryAmountDetailUpAccount(@Param("data") FinanceDetailAccountBO accountBO);

    @InterceptorIgnore(tenantLine = "true")
    public List<JSONObject> queryItemsDetailAccount(@Param("data") FinanceDetailAccountBO accountBO);

    public BigDecimal countDetailAccount(@Param("data") FinanceDetailAccountBO accountBO);


    @InterceptorIgnore(tenantLine = "true")
    public List<JSONObject> queryFinalDetailBalanceAccount(@Param("data") FinanceDetailAccountBO accountBO);

    @InterceptorIgnore(tenantLine = "true")
    public List<JSONObject> queryAdjuvantDetailBalanceAccount(@Param("data") FinanceDetailAccountBO accountBO);


    @Select(SQL_QUERY_CERTIFICATE_WITH_VOUCHER)
    List<CertificateWithVoucherBO> queryCertificateWithVoucher(@Param(Constants.WRAPPER) Wrapper queryWrapper);

    String SQL_QUERY_CERTIFICATE_WITH_VOUCHER = "select *\n" +
            "from (select a.certificate_time, b.voucher_name, a.certificate_num voucherNum, a.account_id\n" +
            "      from wk_finance_certificate a\n" +
            "               left join wk_finance_voucher b on a.voucher_id = b.voucher_id) as t ${ew.customSqlSegment}";


    /**
     * 查询存在凭证的科目信息
     *
     * @param time      当前期数
     * @param accountId 账套ID
     * @return 存在值的账套信息
     */
    public List<FinanceSubjectVO> queryExistDataSubject(@Param("time") String time, @Param("accountId") Long accountId);


    /**
     * 查询存在凭证的辅助核算信息
     *
     * @param accountBO 查询条件
     * @return data
     */
    @InterceptorIgnore(tenantLine = "true")
    public List<JSONObject> queryLabelNameByData(@Param("data") FinanceCertificateAssociationBO accountBO);

    /**
     * 查询指定账期的凭证详情列表
     *
     * @param accountId  账套 ID
     * @param fromPeriod 起始账期
     * @param toPeriod   结束账期
     * @return
     */
    @Select(SQL_LIST_BY_PERIOD)
    List<JSONObject> listByPeriod(@Param("accountId") Long accountId,
                                  @Param("fromPeriod") String fromPeriod,
                                  @Param("toPeriod") String toPeriod);

    String SQL_LIST_BY_PERIOD = "select a.certificate_id,\n" +
            "       DATE_FORMAT(a.certificate_time, '%Y%m') period,\n" +
            "       b.subject_id,\n" +
            "       c.subject_name,\n" +
            "       c.number,\n" +
            "       c.balance_direction,\n" +
            "       c.grade,\n" +
            "       b.assist_id,\n" +
            "       b.debtor_balance,\n" +
            "       b.credit_balance\n" +
            "from wk_finance_certificate a\n" +
            "         left join wk_finance_certificate_detail b on a.certificate_id = b.certificate_id\n" +
            "         left join wk_finance_subject c on b.subject_id = c.subject_id\n" +
            "where a.account_id = #{accountId}\n" +
            "  and (\n" +
            "            DATE_FORMAT(a.certificate_time, '%Y%m') <= #{toPeriod}\n" +
            "        and\n" +
            "            DATE_FORMAT(a.certificate_time, '%Y%m') >= #{fromPeriod}\n" +
            "    )";
}
