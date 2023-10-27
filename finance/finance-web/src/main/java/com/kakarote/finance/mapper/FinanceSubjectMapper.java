package com.kakarote.finance.mapper;

import com.alibaba.fastjson.JSONObject;
import com.kakarote.core.servlet.BaseMapper;
import com.kakarote.finance.entity.BO.FinanceCollectCertificateBO;
import com.kakarote.finance.entity.BO.FinanceDetailAccountBO;
import com.kakarote.finance.entity.BO.FinanceSubjectImportBO;
import com.kakarote.finance.entity.PO.FinanceSubject;
import com.kakarote.finance.entity.VO.FinanceSubjectVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 科目 Mapper 接口
 * </p>
 *
 * @author zhangzhiwei
 * @since 2021-08-24
 */
public interface FinanceSubjectMapper extends BaseMapper<FinanceSubject> {

    /**
     * 列表查询项目
     */
    public List<FinanceSubjectVO> queryListByType(@Param("ids") List<Long> ids, @Param("type") Integer type
            , @Param("accountId") Long accountId, @Param("status") Integer status);

    public List<JSONObject> querySubjectIds(@Param("data") FinanceCollectCertificateBO certificateBO);

    public List<Long> queryIds(@Param("ids") List<Long> ids,
                               @Param("minLevel") Integer minLevel,
                               @Param("maxLevel") Integer maxLevel,
                               @Param("accountId") Long accountId);


    public List<Long> queryIdsByType(@Param("certificateTime") String certificateTime, @Param("accountId") Long accountId);

    public List<Long> queryIdsByUnit(@Param("accountId") Long accountId);


    public List<Long> querySubjectIdsByAdjuvant(@Param("accountId") Long accountId, @Param("adjuvantId") Long adjuvantId, @Param("relationId") Long relationId);

    public List<JSONObject> querySubjectByIds(@Param("data") FinanceDetailAccountBO accountBO);


    @Select(SQL_QUERY_SUBJECT_WITH_ADJUVANT)
    List<FinanceSubjectImportBO> querySubjectWithAdjuvant(@Param("accountId") Long accountId);

    String SQL_QUERY_SUBJECT_WITH_ADJUVANT = "select a.subject_id,\n" +
            "       a.number,\n" +
            "       a.subject_name,\n" +
            "       a.type,\n" +
            "       a.balance_direction,\n" +
            "       b.adjuvant_id,\n" +
            "       b.label,\n" +
            "       b.label_name,\n" +
            "       a.is_cash\n" +
            "from wk_finance_subject a\n" +
            "         left join wk_finance_subject_adjuvant b on a.subject_id = b.subject_id\n" +
            "where a.account_id = #{accountId}\n" +
            "  and a.status = 1;";


    /**
     * 查询科目信息-辅助项-核算类型-核算项
     *
     * @param accountId 账套 ID
     * @return data
     */
    @Select(SQL_QUERY_SUBJECT_WITH_ADJUVANT_AND_CART)
    List<JSONObject> querySubjectWithAdjuvantAndCart(@Param("accountId") Long accountId);

    String SQL_QUERY_SUBJECT_WITH_ADJUVANT_AND_CART = "select distinct a.subject_id,\n" +
            "                a.subject_name,\n" +
            "                a.number,\n" +
            "                b.assist_id,\n" +
            "                e.label,\n" +
            "                f.carte_id,\n" +
            "                f.carte_name,\n" +
            "                f.carte_number,\n" +
            "                f.adjuvant_id,\n" +
            "                a.balance_direction,\n" +
            "                a.grade,\n" +
            "                true isAssist\n" +
            "from wk_finance_subject a\n" +
            "         inner join wk_finance_assist as b on b.subject_id = a.subject_id\n" +
            "         inner join wk_finance_assist_adjuvant e on e.assist_id = b.assist_id\n" +
            "         inner join wk_finance_adjuvant_carte f on e.relation_id = f.carte_id\n" +
            "where a.account_id = #{accountId}\n" +
            "order by number, assist_id,label, carte_number";

}
