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
import com.kakarote.finance.entity.PO.FinanceCashFlowStatementExtendConfig;
import com.kakarote.finance.entity.PO.FinanceCashFlowStatementExtendData;
import com.kakarote.finance.entity.PO.FinanceReportTemplate;
import com.kakarote.finance.entity.PO.FinanceSubject;
import com.kakarote.finance.mapper.FinanceCashFlowStatementExtendConfigMapper;
import com.kakarote.finance.service.IFinanceCashFlowStatementExtendConfigService;
import com.kakarote.finance.service.IFinanceCashFlowStatementExtendDataService;
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
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author 10323
 * @description: 现金流量扩展表
 * @date 2021/8/2910:36
 */
@Service("cashFlowStatementExtendTemplateService")
public class FinanceCashFlowStatementExtendConfigServiceImpl extends BaseServiceImpl<FinanceCashFlowStatementExtendConfigMapper, FinanceCashFlowStatementExtendConfig>
        implements IFinanceCashFlowStatementExtendConfigService, IFinanceInitService {

    @Autowired
    private IFinanceReportTemplateService reportTemplateService;

    @Autowired
    private IFinanceCashFlowStatementExtendDataService extendDataService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importTemplate(Sheet sheet) {
        List<String> headers = Arrays.asList("项目", "行次", "公式", "取数规则", "说明", "类型", "表格标识", "等级");
        Row headerRow = sheet.getRow(0);
        Map<String, Integer> headIndexMap = getHeadIndexMap(headers, headerRow);
        List<FinanceReportTemplate> templates = new ArrayList<>();
        // 先删除原有配置
        reportTemplateService.lambdaUpdate()
                .eq(FinanceReportTemplate::getType, FinanceTemplateType.CASH_FLOW_STATEMENT_EXTEND.getType())
                .remove();
        int index = 0;
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            String name = row.getCell(MapUtil.getInt(headIndexMap, "项目")).getStringCellValue()
                    .replaceAll(" ", "").trim();
            double sort = row.getCell(MapUtil.getInt(headIndexMap, "行次")).getNumericCellValue();
            double grade = row.getCell(MapUtil.getInt(headIndexMap, "等级")).getNumericCellValue();
            Cell formula_cell = row.getCell(MapUtil.getInt(headIndexMap, "公式"));
            String formula = ObjectUtil.isNull(formula_cell) ? "" : formula_cell.getStringCellValue().trim();
            Cell rule_cell = row.getCell(MapUtil.getInt(headIndexMap, "取数规则"));
            String rule = ObjectUtil.isNull(rule_cell) ? "" : rule_cell.getStringCellValue().trim();
            Cell remark_cell = row.getCell(MapUtil.getInt(headIndexMap, "说明"));
            String remark = ObjectUtil.isNull(remark_cell) ? "" : remark_cell.getStringCellValue().trim();
            double tableRemark = row.getCell(MapUtil.getInt(headIndexMap, "表格标识")).getNumericCellValue();
            double type = row.getCell(MapUtil.getInt(headIndexMap, "类型")).getNumericCellValue();

            formula = SeparatorUtil.replaceChinese(formula, "")
                    .replaceAll("、", ",")
                    .replaceAll("：", "")
                    .replaceAll("\\s+", ",");

            FinanceReportTemplate template = new FinanceReportTemplate();
            template.setName(name);
            template.setSort(new Double(sort).intValue());
            template.setGrade(new Double(grade).intValue());
            template.setSortIndex(index++);
            template.setRemark(remark);
            template.setCategory(new Double(tableRemark).intValue());
            template.setType(FinanceTemplateType.CASH_FLOW_STATEMENT_EXTEND.getType());
            template.setEditable(ObjectUtil.equal(1, new Double(type).intValue()) ? false : true);
            template.setCreateTime(DateUtil.date());
            if (StrUtil.isNotEmpty(formula)) {
                if (formula.contains("L")) {
                    template.setFormula(JSON.toJSONString(Arrays.asList(formula)));
                } else {
                    Map<String, FinanceRuleEnum> ruleMap = parseRuleCell(rule);
                    List<FinanceFormulaBO> formulaBOS = this.initFormulaBO(formula, FinanceRuleEnum.BALANCE);
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
        }
        reportTemplateService.saveBatch(templates);
    }

    @Override
    public void init() {
        List<FinanceReportTemplate> templates = reportTemplateService.listByType(FinanceTemplateType.CASH_FLOW_STATEMENT_EXTEND.getType());
        if (CollUtil.isEmpty(templates)) {
            return;
        }
        lambdaUpdate()
                .eq(FinanceCashFlowStatementExtendConfig::getAccountId, AccountSet.getAccountSetId())
                .remove();
        extendDataService.lambdaUpdate()
                .eq(FinanceCashFlowStatementExtendData::getAccountId, AccountSet.getAccountSetId())
                .remove();
        Map<String, FinanceSubject> subjectMap = CashFlowStatementReportHolder.getSubjects().stream().collect(Collectors.toMap(FinanceSubject::getNumber, Function.identity(), (k1, k2) -> k1));
        List<FinanceCashFlowStatementExtendConfig> configs = JSON.parseArray(JSON.toJSONString(templates),
                FinanceCashFlowStatementExtendConfig.class);
        for (FinanceCashFlowStatementExtendConfig config : configs) {
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
        saveBatch(configs);
    }
}
