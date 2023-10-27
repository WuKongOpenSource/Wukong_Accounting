package com.kakarote.finance.mapper;

import com.alibaba.fastjson.JSONObject;
import com.kakarote.core.entity.BasePage;
import com.kakarote.core.servlet.BaseMapper;
import com.kakarote.finance.entity.BO.FinanceAdjuvantCarteBO;
import com.kakarote.finance.entity.BO.FinanceAdjuvantCarteImportBO;
import com.kakarote.finance.entity.PO.FinanceAdjuvantCarte;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author zhangzhiwei
 * @since 2021-08-24
 */
public interface FinanceAdjuvantCarteMapper extends BaseMapper<FinanceAdjuvantCarte> {

    public BasePage<FinanceAdjuvantCarte> queryByAdjuvantId(BasePage<FinanceAdjuvantCarte> parse, @Param("data") FinanceAdjuvantCarteBO carteBO);

    public List<LinkedHashMap<String, Object>> getAdjuvantData(@Param("data") FinanceAdjuvantCarteBO carteBO);

    /**
     * 查询辅助核算信息
     *
     * @param accountId 账套 ID
     * @return data
     */
    @Select(SQL_QUERY_ADJUVANT_INFO)
    List<FinanceAdjuvantCarteImportBO> queryAdjuvantInfo(@Param("accountId") Long accountId);

    String SQL_QUERY_ADJUVANT_INFO = "select a.adjuvant_id, a.carte_id, a.carte_name, a.carte_number, b.label, b.adjuvant_name\n" +
            "from wk_finance_adjuvant_carte a\n" +
            "         left join wk_finance_adjuvant b on a.adjuvant_id = b.adjuvant_id\n" +
            "where a.account_id = #{accountId}\n" +
            "order by b.label";

    /**
     * 通过账套ID查询辅助核算
     *
     * @param accountId 账套ID
     * @return 辅助核算信息
     */
    public List<JSONObject> querySubjectAdjuvantByAccountId(@Param("accountId") Long accountId);
}
