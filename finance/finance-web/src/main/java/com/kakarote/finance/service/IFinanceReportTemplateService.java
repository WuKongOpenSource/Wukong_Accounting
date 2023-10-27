package com.kakarote.finance.service;

import com.kakarote.core.servlet.BaseService;
import com.kakarote.finance.entity.PO.FinanceReportTemplate;

import java.io.IOException;
import java.util.List;

/**
 * @description:财务系统报表配置模板
 * @author: zjj
 * @date: 2021-08-31 09:55
 */
public interface IFinanceReportTemplateService extends BaseService<FinanceReportTemplate> {

    List<FinanceReportTemplate> listByType(Integer type);

    /**
     * 导入模板
     *
     * @throws IOException
     */
    void importTemplate() throws IOException;

}
