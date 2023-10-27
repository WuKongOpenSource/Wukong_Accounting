package com.kakarote.finance.mapper;

import com.kakarote.core.servlet.BaseMapper;
import com.kakarote.finance.entity.PO.FinanceSubjectTemplate;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @description:科目模板
 * @author: zjj
 * @date: 2021-08-31 10:13
 */
public interface FinanceSubjectTemplateMapper extends BaseMapper<FinanceSubjectTemplate> {

    @Select(QUERY_LIS_SQL)
    List<FinanceSubjectTemplate> queryList();

    String QUERY_LIS_SQL = "select * from wk_finance_subject_template";
}
