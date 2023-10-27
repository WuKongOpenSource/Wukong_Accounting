package com.kakarote.finance.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.kakarote.common.utils.UserUtil;
import com.kakarote.core.servlet.BaseServiceImpl;
import com.kakarote.core.utils.SeparatorUtil;
import com.kakarote.finance.common.AccountSet;
import com.kakarote.finance.common.CashFlowStatementReportHolder;
import com.kakarote.finance.constant.FinanceRuleEnum;
import com.kakarote.finance.constant.FinanceTemplateType;
import com.kakarote.finance.entity.BO.FinanceFormulaBO;
import com.kakarote.finance.entity.PO.FinanceIncomeStatementConfig;
import com.kakarote.finance.entity.PO.FinanceReportTemplate;
import com.kakarote.finance.entity.PO.FinanceSubject;
import com.kakarote.finance.mapper.FinanceIncomeStatementConfigMapper;
import com.kakarote.finance.service.IFinanceIncomeStatementConfigService;
import com.kakarote.finance.service.IFinanceInitService;
import com.kakarote.finance.service.IFinanceReportTemplateService;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service("incomeStatementTemplateService")
public class FinanceIncomeStatementConfigServiceImpl extends BaseServiceImpl<FinanceIncomeStatementConfigMapper, FinanceIncomeStatementConfig>
        implements IFinanceIncomeStatementConfigService, IFinanceInitService {

    @Autowired
    private IFinanceReportTemplateService reportTemplateService;

    @Autowired
    private IFinanceIncomeStatementConfigService incomeStatementConfigService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importTemplate(Sheet sheet) {
        List<String> headers = Arrays.asList("项目", "行次", "所包含科目编码", "取数规则", "等级");
        Row headerRow = sheet.getRow(0);
        Map<String, Integer> headIndexMap = getHeadIndexMap(headers, headerRow);
        List<FinanceReportTemplate> templates = new ArrayList<>();
        // 先删除原有配置
        reportTemplateService.lambdaUpdate()
                .eq(FinanceReportTemplate::getType, FinanceTemplateType.INCOME_STATEMENT.getType())
                .remove();
        int index = 0;
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            String name = row.getCell(MapUtil.getInt(headIndexMap, "项目")).getStringCellValue()
                    .replaceAll(" ", "").trim();
            double sort = row.getCell(MapUtil.getInt(headIndexMap, "行次")).getNumericCellValue();
            String number = row.getCell(MapUtil.getInt(headIndexMap, "所包含科目编码")).getStringCellValue().trim();
            String rule = row.getCell(MapUtil.getInt(headIndexMap, "取数规则")).getStringCellValue().trim();
            double grade = row.getCell(MapUtil.getInt(headIndexMap, "等级")).getNumericCellValue();
            number = SeparatorUtil.replaceChinese(number, "")
                    .replaceAll("、", ",")
                    .replaceAll("：", "")
                    .replaceAll("\\s+", ",");
            FinanceReportTemplate template = new FinanceReportTemplate();
            template.setName(name);
            template.setSort(new Double(sort).intValue());
            template.setSortIndex(index++);
            template.setCreateTime(DateUtil.date());
            template.setType(FinanceTemplateType.INCOME_STATEMENT.getType());
            template.setGrade(new Double(grade).intValue());
            if (StrUtil.isNotEmpty(number)) {
                if (number.contains("L")) {
                    template.setFormula(JSON.toJSONString(Arrays.asList(number)));
                } else {
                    List<FinanceFormulaBO> formulaBOS = this.initFormulaBO(number, FinanceRuleEnum.parseName(rule));
                    template.setFormula(JSON.toJSONString(formulaBOS));
                    template.setEditable(true);
                }
            } else {
                template.setFormula(JSON.toJSONString(new ArrayList<>()));
                template.setEditable(false);
            }
            templates.add(template);
        }
        reportTemplateService.saveBatch(templates);
    }

    @Override
    public void init() {
        List<FinanceReportTemplate> templates = reportTemplateService.listByType(FinanceTemplateType.INCOME_STATEMENT.getType());
        if (CollUtil.isEmpty(templates)) {
            return;
        }
        incomeStatementConfigService.lambdaUpdate()
                .eq(FinanceIncomeStatementConfig::getAccountId, AccountSet.getAccountSetId())
                .remove();
        Map<String, FinanceSubject> subjectMap = CashFlowStatementReportHolder.getSubjects().stream().collect(Collectors.toMap(FinanceSubject::getNumber, Function.identity(), (k1, k2) -> k1));

        List<FinanceIncomeStatementConfig> configs = JSON.parseArray(JSON.toJSONString(templates),
                FinanceIncomeStatementConfig.class);
        for (FinanceIncomeStatementConfig config : configs) {
            config.setId(null);
            config.setAccountId(AccountSet.getAccountSetId());
            config.setCreateUserId(UserUtil.getUserId());
            config.setCreateTime(LocalDateTime.now());
            String formula = config.getFormula();
            if (!formula.contains("L")) {
                List<FinanceFormulaBO> formulaBOS = JSON.parseArray(formula, FinanceFormulaBO.class);
                for (FinanceFormulaBO formulaBO : formulaBOS) {
                    String subjectNumber = formulaBO.getSubjectNumber();
                    formulaBO.setSubjectId(Optional.ofNullable(subjectMap.get(subjectNumber).getSubjectId()).map(String::valueOf).orElse(""));
                }
                config.setFormula(JSON.toJSONString(formulaBOS));
            }
        }
        incomeStatementConfigService.saveBatch(configs);
    }
}
