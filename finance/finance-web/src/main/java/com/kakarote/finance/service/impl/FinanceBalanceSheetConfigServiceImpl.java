package com.kakarote.finance.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
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
import com.kakarote.finance.entity.PO.FinanceBalanceSheetConfig;
import com.kakarote.finance.entity.PO.FinanceReportTemplate;
import com.kakarote.finance.entity.PO.FinanceSubject;
import com.kakarote.finance.mapper.FinanceBalanceSheetConfigMapper;
import com.kakarote.finance.service.IFinanceBalanceSheetConfigService;
import com.kakarote.finance.service.IFinanceInitService;
import com.kakarote.finance.service.IFinanceReportTemplateService;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author zjj
 * @description: 资产负债表配置
 * @date 2021/8/26 18:49
 */
@Service("balanceSheetTemplateService")
public class FinanceBalanceSheetConfigServiceImpl extends BaseServiceImpl<FinanceBalanceSheetConfigMapper, FinanceBalanceSheetConfig> implements IFinanceBalanceSheetConfigService, IFinanceInitService {

    @Autowired
    private IFinanceReportTemplateService reportTemplateService;

    @Autowired
    private IFinanceBalanceSheetConfigService balanceSheetConfigService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importTemplate(Sheet sheet) {
        List<String> headers = Arrays.asList("资产", "计算所包含科目编码", "行次", "取数规则", "等级",
                "负债和所有者权益", "计算所包含科目", "行次", "取数规则", "等级");
        Row headerRow = sheet.getRow(0);
        Map<String, Integer> headIndexMap = getHeadIndexMap(headers, headerRow);
        AtomicInteger atomicInteger = new AtomicInteger(0);
        List<FinanceReportTemplate> templates = new ArrayList<>();
        // 先删除原有配置
        reportTemplateService.lambdaUpdate()
                .eq(FinanceReportTemplate::getType, FinanceTemplateType.BALANCE_SHEET.getType())
                .remove();
        int index = 0;
        int index_1 = sheet.getLastRowNum();

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            String name_0 = row.getCell(MapUtil.getInt(headIndexMap, "资产")).getStringCellValue().trim();
            String number_0 = row.getCell(MapUtil.getInt(headIndexMap, "计算所包含科目编码")).getStringCellValue().trim();
            double sort_0 = row.getCell(MapUtil.getInt(headIndexMap, "行次")).getNumericCellValue();
            String rule_0 = row.getCell(MapUtil.getInt(headIndexMap, "取数规则")).getStringCellValue().trim();
            double grade_0 = row.getCell(MapUtil.getInt(headIndexMap, "等级")).getNumericCellValue();


            Cell name_1_cell = row.getCell(MapUtil.getInt(headIndexMap, "负债和所有者权益"));
            String name_1 = ObjectUtil.isNull(name_1_cell) ? "" : name_1_cell.getStringCellValue().trim();
            Cell number_1_cell = row.getCell(MapUtil.getInt(headIndexMap, "计算所包含科目"));
            String number_1 = ObjectUtil.isNull(number_1_cell) ? "" : number_1_cell.getStringCellValue().trim();
            Cell sort_1_cell = row.getCell(MapUtil.getInt(headIndexMap, "行次_1"));
            double sort_1 = ObjectUtil.isNull(sort_1_cell) ? 0 : sort_1_cell.getNumericCellValue();
            Cell rule_1_cell = row.getCell(MapUtil.getInt(headIndexMap, "取数规则_1"));
            String rule_1 = ObjectUtil.isNull(rule_1_cell) ? "" : rule_1_cell.getStringCellValue().trim();
            Cell grade_1_cell = row.getCell(MapUtil.getInt(headIndexMap, "等级_1"));
            double grade_1 = ObjectUtil.isNull(grade_1_cell) ? 0 : grade_1_cell.getNumericCellValue();

            Integer rowId = atomicInteger.getAndIncrement();

            number_0 = SeparatorUtil.replaceChinese(number_0, "")
                    .replaceAll("、", ",")
                    .replaceAll("：", "")
                    .replaceAll("\\s+", ",");
            number_1 = SeparatorUtil.replaceChinese(number_1, "")
                    .replaceAll("、", ",")
                    .replaceAll("：", "")
                    .replaceAll("\\s+", ",");

            FinanceReportTemplate template = new FinanceReportTemplate();
            template.setType(FinanceTemplateType.BALANCE_SHEET.getType());
            template.setSortIndex(index++);
            template.setRowId(rowId);
            template.setCreateTime(DateUtil.date());
            // 资产
            template.setName(name_0);
            template.setSort(new Double(sort_0).intValue());
            template.setGrade(new Double(grade_0).intValue());
            template.setEditable(false);
            if (StrUtil.isNotEmpty(number_0)) {
                if (number_0.contains("L")) {
                    template.setFormula(JSON.toJSONString(Arrays.asList(number_0)));
                } else {
                    Map<String, FinanceRuleEnum> ruleMap = parseRuleCell(rule_0);
                    List<FinanceFormulaBO> formulaBOS = this.initFormulaBO(number_0, FinanceRuleEnum.BALANCE);
                    ruleMap.forEach((k, v) -> {
                        FinanceFormulaBO formulaBO = formulaBOS.stream()
                                .filter(financeFormulaBO -> ObjectUtil.equal(k, financeFormulaBO.getSubjectNumber()))
                                .findFirst().orElse(null);
                        formulaBO.setRules(v.getType());
                    });
                    template.setEditable(true);
                    template.setFormula(JSON.toJSONString(formulaBOS));
                }
            } else {
                template.setFormula(JSON.toJSONString(new ArrayList<>()));
            }
            templates.add(template);

            if (StrUtil.isNotEmpty(name_1)) {
                FinanceReportTemplate template_1 = new FinanceReportTemplate();
                template_1.setType(FinanceTemplateType.BALANCE_SHEET.getType());
                template_1.setSortIndex(index_1++);
                template_1.setRowId(rowId);
                template_1.setCreateTime(DateUtil.date());
                template_1.setName(name_1);
                template_1.setSort(new Double(sort_1).intValue());
                template_1.setGrade(new Double(grade_1).intValue());
                template_1.setEditable(false);

                if (StrUtil.isNotEmpty(number_1)) {
                    if (number_1.contains("L")) {
                        template_1.setFormula(JSON.toJSONString(Arrays.asList(number_1)));
                    } else {
                        Map<String, FinanceRuleEnum> ruleMap = parseRuleCell(rule_1);
                        List<FinanceFormulaBO> formulaBOS = this.initFormulaBO(number_1, FinanceRuleEnum.BALANCE);
                        ruleMap.forEach((k, v) -> {
                            FinanceFormulaBO formulaBO = formulaBOS.stream()
                                    .filter(financeFormulaBO -> ObjectUtil.equal(k, financeFormulaBO.getSubjectNumber()))
                                    .findFirst().orElse(null);
                            formulaBO.setRules(v.getType());
                        });
                        template_1.setEditable(true);
                        template_1.setFormula(JSON.toJSONString(formulaBOS));
                    }

                } else {
                    template_1.setFormula(JSON.toJSONString(new ArrayList<>()));
                }
                templates.add(template_1);
            }
        }
        reportTemplateService.saveBatch(templates);
    }

    @Override
    public void init() {
        List<FinanceReportTemplate> templates = reportTemplateService.listByType(FinanceTemplateType.BALANCE_SHEET.getType());
        if (CollUtil.isEmpty(templates)) {
            return;
        }
        balanceSheetConfigService.lambdaUpdate()
                .eq(FinanceBalanceSheetConfig::getAccountId, AccountSet.getAccountSetId())
                .remove();
        Map<String, FinanceSubject> subjectMap = CashFlowStatementReportHolder.getSubjects().stream().collect(Collectors.toMap(FinanceSubject::getNumber, Function.identity(), (k1, k2) -> k1));
        List<FinanceBalanceSheetConfig> configs = JSON.parseArray(JSON.toJSONString(templates),
                FinanceBalanceSheetConfig.class);

        for (FinanceBalanceSheetConfig config : configs) {
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
        balanceSheetConfigService.saveBatch(configs);
    }
}
