package com.kakarote.finance.mapper;

import com.kakarote.core.servlet.BaseMapper;
import com.kakarote.finance.entity.PO.FinanceReportTemplate;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @description: 财务系统报表配置模板
 * @author: zjj
 * @date: 2021-08-31 09:53
 */
public interface FinanceReportTemplateMapper extends BaseMapper<FinanceReportTemplate> {

    @Select(LIST_BY_TYPE_SQL)
    List<FinanceReportTemplate> listByType(@Param("type") Integer type);

    String LIST_BY_TYPE_SQL = "select * from wk_finance_report_template where type = #{type}";

}
