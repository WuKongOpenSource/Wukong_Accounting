package com.kakarote.finance.mapper;

import com.alibaba.fastjson.JSONObject;
import com.kakarote.core.servlet.BaseMapper;
import com.kakarote.finance.entity.PO.FinanceAdjuvant;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 辅助核算表 Mapper 接口
 * </p>
 *
 * @author zhangzhiwei
 * @since 2021-08-24
 */
public interface FinanceAdjuvantMapper extends BaseMapper<FinanceAdjuvant> {

    /**
     * 获取科目的辅助核算信息
     *
     * @param subjectId
     * @return
     */
    @Select(SQL_QUERY_ADJUVANT_INFO_BY_SUBJECT_ID)
    List<JSONObject> queryAdjuvantInfoBySubjectId(@Param("subjectId") Long subjectId);

    String SQL_QUERY_ADJUVANT_INFO_BY_SUBJECT_ID = "select a.subject_id, a.subject_name, b.assist_id, e.adjuvant_id, " +
            "f.label, g.carte_number, g.carte_name\n" +
            "from wk_finance_subject a\n" +
            "         left join wk_finance_assist as b on b.subject_id = a.subject_id\n" +
            "         left join wk_finance_assist_adjuvant e on e.assist_id = b.assist_id\n" +
            "         left join wk_finance_adjuvant f on e.adjuvant_id = f.adjuvant_id\n" +
            "         left join wk_finance_adjuvant_carte g on e.relation_id = g.carte_id\n" +
            "where a.subject_id = #{subjectId}";

}
