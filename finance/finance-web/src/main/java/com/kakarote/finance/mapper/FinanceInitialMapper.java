package com.kakarote.finance.mapper;

import com.alibaba.fastjson.JSONObject;
import com.kakarote.core.entity.BasePage;
import com.kakarote.core.servlet.BaseMapper;
import com.kakarote.finance.entity.BO.FinanceDetailAccountBO;
import com.kakarote.finance.entity.BO.FinanceInitialPageBO;
import com.kakarote.finance.entity.PO.FinanceInitial;
import com.kakarote.finance.entity.PO.FinanceInitialAssistAdjuvant;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 财务初始余额 Mapper 接口
 * </p>
 *
 * @author zhangzhiwei
 * @since 2021-08-25
 */
public interface FinanceInitialMapper extends BaseMapper<FinanceInitial> {
    /**
     * 查询财务初始余额列表
     */
    public List<JSONObject> queryListBySubjectType(@Param("subjectType") Integer subjectType, @Param("accountId") Long accountId);

    public BasePage<JSONObject> queryPageBySubjectType(BasePage<FinanceInitialPageBO> page, @Param("data") FinanceInitialPageBO bo);

    public List<FinanceInitialAssistAdjuvant> queryAssistAdjuvant(@Param("assistId") Long assistId, @Param("accountId") Long accountId);

    /**
     * 查询财务初始余额列表
     */
    public List<JSONObject> queryListBysubjectId(@Param("subjectId") Long subjectId, @Param("accountId") Long accountId);

    /**
     * 试算平衡
     */
    public JSONObject queryTrialBalance(@Param("accountId") Long accountId);

    /**
     * 查询科目的初始余额
     *
     * @param accountId
     * @return
     */
    @Select(SQL_QUERY_ALL_INITIAL)
    List<JSONObject> queryAllInitial(@Param("accountId") Long accountId);


    String SQL_QUERY_ALL_INITIAL = "select a.subject_id,\n" +
            "       a.subject_name,\n" +
            "       b.initial_id,\n" +
            "       b.initial_balance,\n" +
            "       b.beginning_balance,\n" +
            "       b.add_up_debtor_balance,\n" +
            "       b.add_up_credit_balance,\n" +
            "       b.profit_balance,\n" +
            "       b.is_assist,\n" +
            "       c.finance_assist_id as assist_id\n" +
            "from wk_finance_subject a\n" +
            "         left join wk_finance_initial b\n" +
            "                   on a.subject_id = b.subject_id\n" +
            "         left join wk_finance_initial_assist c on b.initial_id = c.initial_id and b.subject_id = c.subject_id\n" +
            "where a.account_id = #{accountId}";

    /**
     * 查询核算科目的初始余额
     *
     * @return
     */
    List<JSONObject> queryAdjuvantInitial(@Param("data") FinanceDetailAccountBO accountBO);

    /**
     * 查詢初始余额列表
     *
     * @param page
     * @param bo
     * @return
     */
    @Select(SQL_QUERY_PAGE_LIST_SUBJECT_TYPE)
    BasePage<JSONObject> queryPageListBySubjectType(BasePage<FinanceInitialPageBO> page, @Param("data") FinanceInitialPageBO bo);

    String SQL_QUERY_PAGE_LIST_SUBJECT_TYPE = "select *\n" +
            "from (\n" +
            "         (select a.subject_id,\n" +
            "                 a.number,\n" +
            "                 a.subject_name,\n" +
            "                 a.parent_id,\n" +
            "                 a.is_amount,\n" +
            "                 a.balance_direction,\n" +
            "                 b.initial_id,\n" +
            "                 b.initial_balance,\n" +
            "                 b.initial_num,\n" +
            "                 b.add_up_debtor_balance,\n" +
            "                 b.add_up_debtor_num,\n" +
            "                 b.add_up_credit_balance,\n" +
            "                 b.add_up_credit_num,\n" +
            "                 b.beginning_balance,\n" +
            "                 b.beginning_num,\n" +
            "                 b.profit_balance,\n" +
            "                 null assist_id,\n" +
            "                 null carte_id,\n" +
            "                 null carte_number,\n" +
            "                 null carte_name\n" +
            "          from wk_finance_subject a\n" +
            "                   left join wk_finance_initial b on a.subject_id = b.subject_id and b.is_assist = false\n" +
            "          where a.account_id = #{data.accountId}\n" +
            "            and a.type = #{data.subjectType})\n" +
            "         union all\n" +
            "         (select c.subject_id,\n" +
            "                 c.number,\n" +
            "                 c.subject_name,\n" +
            "                 c.parent_id,\n" +
            "                 c.is_amount,\n" +
            "                 c.balance_direction,\n" +
            "                 b.initial_id,\n" +
            "                 b.initial_balance,\n" +
            "                 b.initial_num,\n" +
            "                 b.add_up_debtor_balance,\n" +
            "                 b.add_up_debtor_num,\n" +
            "                 b.add_up_credit_balance,\n" +
            "                 b.add_up_credit_num,\n" +
            "                 b.beginning_balance,\n" +
            "                 b.beginning_num,\n" +
            "                 b.profit_balance,\n" +
            "                 a.assist_id,\n" +
            "                 e.carte_id,\n" +
            "                 e.carte_number,\n" +
            "                 e.carte_name\n" +
            "          from wk_finance_initial_assist a\n" +
            "                   left join wk_finance_initial b on a.initial_id = b.initial_id\n" +
            "                   left join wk_finance_subject c on a.subject_id = c.subject_id\n" +
            "                   left join wk_finance_initial_assist_adjuvant d on a.assist_id = d.assist_id\n" +
            "                   left join wk_finance_adjuvant_carte e on d.relation_id = e.carte_id and e.status = 1\n" +
            "          where c.type = #{data.subjectType}\n" +
            "            and c.account_id = #{data.accountId}\n" +
            "          order by e.carte_number)\n" +
            "     ) a\n" +
            "order by a.number, a.carte_number";


    /**
     * 获取指定科目的财务初始余额辅助核算信息
     *
     * @param subjectId
     * @param accountId
     * @return
     */
    @Select(SQL_QUERY_INITIAL_ASSIST_ADJUVANT)
    List<JSONObject> queryInitialAssistAdjuvant(@Param("subjectId") Long subjectId, @Param("accountId") Long accountId);

    String SQL_QUERY_INITIAL_ASSIST_ADJUVANT = "select a.assist_id, a.subject_id, a.initial_id, b.relation_id\n" +
            "from wk_finance_initial_assist a\n" +
            "         left join wk_finance_initial_assist_adjuvant b on a.assist_id = b.assist_id\n" +
            "where a.subject_id = #{subjectId} and a.account_id = #{accountId}";


    /**
     * 查询账套的初始余额详情列表
     *
     * @param accountId
     * @return
     */
    @Select(SQL_LIST_INITIAL_WITH_ASSIST)
    List<JSONObject> listInitialWithAssist(@Param("accountId") Long accountId);

    String SQL_LIST_INITIAL_WITH_ASSIST = "select b.subject_id,\n" +
            "       b.subject_name,\n" +
            "       b.number,\n" +
            "       a.initial_balance,\n" +
            "       a.beginning_balance,\n" +
            "       a.add_up_debtor_balance,\n" +
            "       a.add_up_credit_balance,\n" +
            "       a.profit_balance,\n" +
            "       a.is_assist,\n" +
            "       d.assist_id,\n" +
            "       d.label,\n" +
            "       e.adjuvant_id,\n" +
            "       e.carte_id,\n" +
            "       e.carte_number,\n" +
            "       e.carte_name\n" +
            "from wk_finance_initial a\n" +
            "         inner join wk_finance_subject b on a.subject_id = b.subject_id\n" +
            "         left join wk_finance_initial_assist c on a.initial_id = c.initial_id\n" +
            "         left join wk_finance_assist_adjuvant d on c.finance_assist_id = d.assist_id\n" +
            "         left join wk_finance_adjuvant_carte e on d.relation_id = e.carte_id\n" +
            "where a.account_id = #{accountId}\n" +
            "order by b.number";

}
