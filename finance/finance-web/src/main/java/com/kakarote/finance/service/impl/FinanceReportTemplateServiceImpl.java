package com.kakarote.finance.service.impl;

import com.kakarote.core.servlet.ApplicationContextHolder;
import com.kakarote.core.servlet.BaseServiceImpl;
import com.kakarote.finance.constant.FinanceTemplateEnum;
import com.kakarote.finance.entity.PO.FinanceReportTemplate;
import com.kakarote.finance.mapper.FinanceReportTemplateMapper;
import com.kakarote.finance.service.IFinanceInitService;
import com.kakarote.finance.service.IFinanceReportTemplateService;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * @description:财务系统报表配置模板
 * @author: zjj
 * @date: 2021-08-31 09:55
 */
@Service
public class FinanceReportTemplateServiceImpl extends BaseServiceImpl<FinanceReportTemplateMapper,
        FinanceReportTemplate> implements IFinanceReportTemplateService {

    @Override
    public List<FinanceReportTemplate> listByType(Integer type) {
        return getBaseMapper().listByType(type);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importTemplate() throws IOException {
        ClassPathResource classPathResource = new ClassPathResource("excelTemplates/FINANCE_TABLE.xlsx");
        InputStream inputStream = classPathResource.getInputStream();
        XSSFWorkbook wb = new XSSFWorkbook(inputStream);
        System.out.println(wb.getAllNames());
        Iterator<Sheet> sheetIterator = wb.sheetIterator();
        while (sheetIterator.hasNext()) {
            Sheet sheet = sheetIterator.next();
            String name = sheet.getSheetName();
            FinanceTemplateEnum dashboardNameEnum = FinanceTemplateEnum.parseName(name);
            if (!Arrays.asList(FinanceTemplateEnum.NULL, FinanceTemplateEnum.STATEMENT_TEMPLATE, FinanceTemplateEnum.STATEMENT, FinanceTemplateEnum.SUBJECT).contains(dashboardNameEnum)) {
                IFinanceInitService initService = ApplicationContextHolder.getBean(dashboardNameEnum.getServiceName());
                initService.importTemplate(sheet);
            }
        }
    }

}
