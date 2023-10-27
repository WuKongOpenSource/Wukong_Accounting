package com.kakarote.finance.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.kakarote.common.utils.UserUtil;
import com.kakarote.core.servlet.BaseServiceImpl;
import com.kakarote.finance.common.AccountSet;
import com.kakarote.finance.constant.FinanceTemplateType;
import com.kakarote.finance.entity.PO.FinanceCashFlowStatementConfig;
import com.kakarote.finance.entity.PO.FinanceCashFlowStatementReport;
import com.kakarote.finance.entity.PO.FinanceReportTemplate;
import com.kakarote.finance.mapper.FinanceCashFlowStatementConfigMapper;
import com.kakarote.finance.service.IFinanceCashFlowStatementConfigService;
import com.kakarote.finance.service.IFinanceCashFlowStatementReportService;
import com.kakarote.finance.service.IFinanceInitService;
import com.kakarote.finance.service.IFinanceReportTemplateService;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Service("cashFlowStatementTemplateService")
public class FinanceCashFlowStatementConfigServiceImpl extends BaseServiceImpl<FinanceCashFlowStatementConfigMapper, FinanceCashFlowStatementConfig>
        implements IFinanceCashFlowStatementConfigService, IFinanceInitService {

    @Autowired
    private IFinanceReportTemplateService reportTemplateService;

    @Autowired
    private IFinanceCashFlowStatementReportService cashFlowStatementReportService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importTemplate(Sheet sheet) {
        List<String> headers = Arrays.asList("项目", "行次", "公式", "备注", "等级", "项目", "行次", "公式", "备注", "等级", "表格标识");
        Row headerRow = sheet.getRow(0);
        Map<String, Integer> headIndexMap = getHeadIndexMap(headers, headerRow);
        AtomicInteger atomicInteger = new AtomicInteger(0);
        List<FinanceReportTemplate> templates = new ArrayList<>();
        // 先删除原有配置
        reportTemplateService.lambdaUpdate()
                .eq(FinanceReportTemplate::getType, FinanceTemplateType.CASH_FLOW_STATEMENT.getType())
                .remove();
        int index = 0;
        int index_1 = sheet.getLastRowNum();
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);

            String name_0 = row.getCell(MapUtil.getInt(headIndexMap, "项目")).getStringCellValue().trim();
            Cell number_0_cell = row.getCell(MapUtil.getInt(headIndexMap, "公式"));
            String number_0 = ObjectUtil.isNull(number_0_cell) ? "" : number_0_cell.getStringCellValue().trim();
            double sort_0 = row.getCell(MapUtil.getInt(headIndexMap, "行次")).getNumericCellValue();
            Cell remark_0_cell = row.getCell(MapUtil.getInt(headIndexMap, "备注"));
            String remark = ObjectUtil.isNull(remark_0_cell) ? "" : remark_0_cell.getStringCellValue().trim();
            double grade_0 = row.getCell(MapUtil.getInt(headIndexMap, "等级")).getNumericCellValue();
            double tableRemark = row.getCell(MapUtil.getInt(headIndexMap, "表格标识")).getNumericCellValue();

            Integer rowId = atomicInteger.getAndIncrement();

            FinanceReportTemplate template = new FinanceReportTemplate();
            template.setName(name_0);
            if (StrUtil.isNotEmpty(number_0)) {
                template.setFormula(JSON.toJSONString(Arrays.asList(number_0)));
            } else {
                template.setFormula(JSON.toJSONString(new ArrayList<>()));
            }
            template.setSort(new Double(sort_0).intValue());
            template.setGrade(new Double(grade_0).intValue());
            template.setSortIndex(index++);
            template.setRowId(rowId);
            template.setType(FinanceTemplateType.CASH_FLOW_STATEMENT.getType());
            template.setCategory(new Double(tableRemark).intValue());
            template.setEditable(false);
            template.setRemark(remark);
            template.setCreateTime(DateUtil.date());
            templates.add(template);
            if (headIndexMap.size() > 7) {
                String name_1 = row.getCell(MapUtil.getInt(headIndexMap, "项目_1")).getStringCellValue().trim();
                Cell number_1_cell = row.getCell(MapUtil.getInt(headIndexMap, "公式_1"));
                String number_1 = ObjectUtil.isNull(number_1_cell) ? "" : number_1_cell.getStringCellValue().trim();
                double sort_1 = row.getCell(MapUtil.getInt(headIndexMap, "行次_1")).getNumericCellValue();
                Cell remark_1_cell = row.getCell(MapUtil.getInt(headIndexMap, "备注_1"));
                String remark_1 = ObjectUtil.isNull(remark_1_cell) ? "" : remark_1_cell.getStringCellValue().trim();
                double grade_1 = row.getCell(MapUtil.getInt(headIndexMap, "等级_1")).getNumericCellValue();

                FinanceReportTemplate template_1 = new FinanceReportTemplate();

                template_1.setName(name_1);
                if (StrUtil.isNotEmpty(number_0)) {
                    template_1.setFormula(JSON.toJSONString(Arrays.asList(number_1)));
                } else {
                    template_1.setFormula(JSON.toJSONString(new ArrayList<>()));
                }
                template_1.setSort(new Double(sort_1).intValue());
                template_1.setGrade(new Double(grade_1).intValue());
                template_1.setSortIndex(index_1++);
                template_1.setRowId(rowId);
                template_1.setType(FinanceTemplateType.CASH_FLOW_STATEMENT.getType());
                template_1.setCategory(new Double(tableRemark).intValue());
                template_1.setEditable(false);
                template_1.setRemark(remark_1);
                template_1.setCreateTime(DateUtil.date());
                templates.add(template_1);
            }
        }
        reportTemplateService.saveBatch(templates);
    }

    @Override
    public void init() {
        List<FinanceReportTemplate> templates = reportTemplateService.listByType(FinanceTemplateType.CASH_FLOW_STATEMENT.getType());
        if (CollUtil.isEmpty(templates)) {
            return;
        }
        lambdaUpdate()
                .eq(FinanceCashFlowStatementConfig::getAccountId, AccountSet.getAccountSetId())
                .remove();
        cashFlowStatementReportService.lambdaUpdate()
                .eq(FinanceCashFlowStatementReport::getAccountId, AccountSet.getAccountSetId())
                .remove();
        List<FinanceCashFlowStatementConfig> configs = JSON.parseArray(JSON.toJSONString(templates),
                FinanceCashFlowStatementConfig.class);
        for (FinanceCashFlowStatementConfig config : configs) {
            config.setId(null);
            config.setAccountId(AccountSet.getAccountSetId());
            config.setCreateUserId(UserUtil.getUserId());
            config.setCreateTime(LocalDateTime.now());
        }
        saveBatch(configs);
    }

}
