package com.kakarote.finance.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.googlecode.aviator.AviatorEvaluator;
import com.kakarote.core.servlet.BaseServiceImpl;
import com.kakarote.core.utils.ExcelParseUtil;
import com.kakarote.core.utils.SeparatorUtil;
import com.kakarote.finance.common.AccountSet;
import com.kakarote.finance.common.CashFlowStatementReportHolder;
import com.kakarote.finance.common.FinanceExcelParseUtil;
import com.kakarote.finance.constant.FinanceRuleEnum;
import com.kakarote.finance.entity.BO.FinanceDetailAccountBO;
import com.kakarote.finance.entity.BO.FinanceFormulaBO;
import com.kakarote.finance.entity.BO.FinanceReportRequestBO;
import com.kakarote.finance.entity.BO.FinanceUpdateReportConfigBO;
import com.kakarote.finance.entity.PO.FinanceAccountSet;
import com.kakarote.finance.entity.PO.FinanceIncomeStatementConfig;
import com.kakarote.finance.entity.PO.FinanceIncomeStatementReport;
import com.kakarote.finance.entity.PO.FinanceSubject;
import com.kakarote.finance.mapper.FinanceIncomeStatementReportMapper;
import com.kakarote.finance.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author zjj
 * @description: 利润表
 * @date 2021/8/26 15:02
 */
@Service
public class FinanceIncomeStatementReportServiceImpl extends BaseServiceImpl<FinanceIncomeStatementReportMapper, FinanceIncomeStatementReport> implements IFinanceIncomeStatementReportService {

    @Autowired
    private IFinanceSubjectService subjectService;

    @Autowired
    private IFinanceIncomeStatementConfigService incomeStatementConfigService;

    @Autowired
    private IFinanceCertificateService certificateService;

    @Autowired
    private IFinanceBalanceSheetReportService balanceSheetService;

    @Autowired
    IFinanceCurrencyService currencyService;

    @Override
    public List<JSONObject> report(FinanceReportRequestBO requestBO) {
        List<JSONObject> report = new ArrayList<>();
        try {
            List<FinanceIncomeStatementReport> incomeStatementReports = lambdaQuery()
                    .eq(FinanceIncomeStatementReport::getAccountId, AccountSet.getAccountSetId())
                    .eq(FinanceIncomeStatementReport::getType, requestBO.getType())
                    .eq(FinanceIncomeStatementReport::getFromPeriod, requestBO.getFromPeriod())
                    .eq(FinanceIncomeStatementReport::getToPeriod, requestBO.getToPeriod())
                    .orderByAsc(FinanceIncomeStatementReport::getSortIndex)
                    .list();
            if (CollUtil.isEmpty(incomeStatementReports)) {
                // 获取利润表配置
                List<FinanceIncomeStatementConfig> incomeStatementConfigs = incomeStatementConfigService.lambdaQuery()
                        .eq(FinanceIncomeStatementConfig::getAccountId, AccountSet.getAccountSetId())
                        .orderByAsc(FinanceIncomeStatementConfig::getSortIndex)
                        .list();
                incomeStatementConfigs.forEach(c -> {
                    FinanceIncomeStatementReport r = BeanUtil.copyProperties(c, FinanceIncomeStatementReport.class);
                    r.setId(null);
                    r.setCreateTime(LocalDateTime.now());
                    r.setType(requestBO.getType());
                    r.setFromPeriod(requestBO.getFromPeriod());
                    r.setToPeriod(requestBO.getToPeriod());
                    save(r);
                    incomeStatementReports.add(r);
                });
            }
            if (CollUtil.isEmpty(incomeStatementReports)) {
                return report;
            }
            // 本期科目余额
            FinanceDetailAccountBO detailAccountBO = new FinanceDetailAccountBO();
            detailAccountBO.setAccountId(AccountSet.getAccountSetId());
            detailAccountBO.setStartTime(requestBO.getFromPeriod());
            detailAccountBO.setEndTime(requestBO.getToPeriod());
            List<JSONObject> subjectBalance = certificateService.querySubjectBalance(AccountSet.getAccountSetId(), requestBO.getFromPeriod(), requestBO.getToPeriod());
            subjectBalance = subjectBalance.stream().filter(o -> !o.getBoolean("isAssist")).collect(Collectors.toList());
            Map<Long, JSONObject> subjectBalanceMap = subjectBalance.stream()
                    .collect(Collectors.toMap(o -> o.getLong("subjectId"), Function.identity()));

            for (FinanceIncomeStatementReport config : incomeStatementReports) {
                String formula = config.getFormula();
                BigDecimal monthValue = new BigDecimal(0);
                BigDecimal yearValue = new BigDecimal(0);
                Map<String, String> keyMap = new HashMap<>();
                keyMap.put("name_resourceKey", "finance.config.statement.income." + config.getName());
                config.setLanguageKeyMap(keyMap);
                if (formula.contains("L")) {
                    formula = JSON.parseArray(config.getFormula(), String.class).get(0);
                    String[] formulas = formula.split(SeparatorUtil.REGEX_OPERATOR);
                    Map<String, Object> monthEnv = new HashMap<>();
                    Map<String, Object> yearEnv = new HashMap<>();
                    for (String f : formulas) {
                        Integer sort = Integer.valueOf(f.replaceAll("L", ""));
                        JSONObject valueObject = filterReport(report, sort);
                        monthEnv.put(f, valueObject.getBigDecimal("monthValue"));
                        yearEnv.put(f, valueObject.getBigDecimal("yearValue"));
                    }
                    monthValue = monthValue.add(new BigDecimal(AviatorEvaluator.execute(formula, monthEnv).toString()));
                    yearValue = yearValue.add(new BigDecimal(AviatorEvaluator.execute(formula, yearEnv).toString()));
                } else {
                    List<FinanceFormulaBO> formulaBOS = JSON.parseArray(config.getFormula(), FinanceFormulaBO.class);
                    for (FinanceFormulaBO formulaBO : formulaBOS) {
                        BigDecimal m = this.calculateCurrentValue(formulaBO, subjectBalanceMap);
                        BigDecimal y = this.calculateYearValue(formulaBO, subjectBalanceMap);
                        formulaBO.setMonthValue(m);
                        formulaBO.setYearValue(y);
                        monthValue = monthValue.add(m);
                        yearValue = yearValue.add(y);
                        FinanceSubject subject = CashFlowStatementReportHolder.getSubjects(Long.valueOf(formulaBO.getSubjectId()));
                        if (ObjectUtil.isEmpty(subject)) {
                            continue;
                        }
                        formulaBO.setSubjectName(subject.getSubjectName());
                        formulaBO.setSubjectNumber(subject.getNumber());
                    }
                    config.setFormula(JSON.toJSONString(formulaBOS));
                }
                config.setMonthValue(monthValue);
                config.setYearValue(yearValue);
                JSONObject jsonObject = new JSONObject(BeanUtil.beanToMap(config));
                report.add(jsonObject);
            }
        } finally {
            CashFlowStatementReportHolder.remove();
        }
        return report;
    }

    private BigDecimal calculateCurrentValue(FinanceFormulaBO formulaBO, Map<Long, JSONObject> subjectBalanceMap) {
        Long subjectId = StrUtil.isEmpty(formulaBO.getSubjectId()) ? null : Long.valueOf(formulaBO.getSubjectId());
        String operator = formulaBO.getOperator();
        FinanceRuleEnum ruleEnum = FinanceRuleEnum.parseType(formulaBO.getRules());
        JSONObject balanceObject = subjectBalanceMap.get(subjectId);
        BigDecimal bigDecimal = new BigDecimal(0);
        if (ObjectUtil.isNull(balanceObject)) {
            return bigDecimal;
        }
        BigDecimal debtorCurrentBalance = balanceObject.getBigDecimal("debtorCurrentBalance");
        BigDecimal creditCurrentBalance = balanceObject.getBigDecimal("creditCurrentBalance");
        // 贷方发生额
        if (ObjectUtil.equal(FinanceRuleEnum.CREDIT_AMOUNT, ruleEnum)) {
            if (StrUtil.equals("+", operator)) {
                bigDecimal = creditCurrentBalance.subtract(debtorCurrentBalance);
            } else {
                bigDecimal = bigDecimal.subtract(creditCurrentBalance);
            }
        }
        // 借方发生额
        else if (ObjectUtil.equal(FinanceRuleEnum.DEBIT_AMOUNT, ruleEnum)) {
            if (StrUtil.equals("+", operator)) {
                bigDecimal = debtorCurrentBalance.subtract(creditCurrentBalance);
            } else {
                bigDecimal = bigDecimal.subtract(debtorCurrentBalance);
            }
        }
        return bigDecimal;
    }

    private BigDecimal calculateYearValue(FinanceFormulaBO formulaBO, Map<Long, JSONObject> subjectBalanceMap) {
        Long subjectId = StrUtil.isEmpty(formulaBO.getSubjectId()) ? null : Long.valueOf(formulaBO.getSubjectId());
        String operator = formulaBO.getOperator();
        FinanceRuleEnum ruleEnum = FinanceRuleEnum.parseType(formulaBO.getRules());
        JSONObject balanceObject = subjectBalanceMap.get(subjectId);
        BigDecimal bigDecimal = new BigDecimal(0);
        if (ObjectUtil.isNull(balanceObject)) {
            return bigDecimal;
        }
        BigDecimal debtorYearBalance = balanceObject.getBigDecimal("debtorYearBalance");
        BigDecimal creditYearBalance = balanceObject.getBigDecimal("creditYearBalance");
        // 贷方发生额
        if (ObjectUtil.equal(FinanceRuleEnum.CREDIT_AMOUNT, ruleEnum)) {
            if (StrUtil.equals("+", operator)) {
                bigDecimal = creditYearBalance.subtract(debtorYearBalance);
            } else {
                bigDecimal = bigDecimal.subtract(creditYearBalance);
            }
        }
        // 借方发生额
        else if (ObjectUtil.equal(FinanceRuleEnum.DEBIT_AMOUNT, ruleEnum)) {
            if (StrUtil.equals("+", operator)) {
                bigDecimal = debtorYearBalance.subtract(creditYearBalance);
            } else {
                bigDecimal = bigDecimal.subtract(debtorYearBalance);
            }
        }

        return bigDecimal;
    }

    @Override
    public JSONObject filterByExpression(FinanceReportRequestBO requestBO) {
        List<JSONObject> report = CashFlowStatementReportHolder.getIncomeStatement();
        if (ObjectUtil.isNull(report) || CollUtil.isEmpty(report)) {
            report = this.report(requestBO);
            CashFlowStatementReportHolder.setIncomeStatement(report);
        }
        String expression = requestBO.getExpression();
        String sort = expression.replaceAll("[A-Z]{2}", "");
        return this.filterReport(report, Integer.valueOf(sort));
    }

    @Override
    public JSONObject balanceCheck(FinanceReportRequestBO requestBO) {
        // 获取所有损益类一级科目
        List<FinanceSubject> firstGradeSubjects = subjectService.queryFirstGradeByType(5);
        // 获取可标记得利润表配置
        List<FinanceIncomeStatementReport> incomeStatementReports = lambdaQuery()
                .eq(FinanceIncomeStatementReport::getAccountId, AccountSet.getAccountSetId())
                .eq(FinanceIncomeStatementReport::getType, requestBO.getType())
                .eq(FinanceIncomeStatementReport::getFromPeriod, requestBO.getFromPeriod())
                .eq(FinanceIncomeStatementReport::getToPeriod, requestBO.getToPeriod())
                .eq(FinanceIncomeStatementReport::getEditable, 1)
                .orderByAsc(FinanceIncomeStatementReport::getSortIndex)
                .list();
        // 检查损益类一级科目是否都已经设置科目公式
        List<FinanceSubject> notContains = new ArrayList<>();
        for (FinanceSubject subject : firstGradeSubjects) {
            // 此损益类科目不检查
            if (subject.getNumber().startsWith("6")) {
                continue;
            }
            boolean notContainsSubject = this.notContainsSubject(subject.getSubjectId(), incomeStatementReports);
            if (notContainsSubject) {
                notContains.add(subject);
            }
        }
        // 资产负债表
        List<JSONObject> balanceSheetReport = balanceSheetService.report(requestBO);
        // 未分配利润
        JSONObject o1 = balanceSheetService.filterReport(balanceSheetReport, 51);
        // 利润表
        List<JSONObject> incomeStatementReport = this.report(requestBO);
        // 净利润
        JSONObject o2 = filterReport(incomeStatementReport, 32);
        JSONObject object = new JSONObject();
        object.put("notContains", notContains);
        String yearValue = "yearValue";
        String endPeriod = "endPeriod";
        String initialPeriod = "initialPeriod";
        if (o2.getBigDecimal(yearValue).compareTo(o1.getBigDecimal(endPeriod).subtract(o1.getBigDecimal(initialPeriod))) == 0) {
            object.put("balanced", true);
        } else {
            object.put("balanced", false);
        }
        return object;
    }

    private boolean notContainsSubject(Long number, List<FinanceIncomeStatementReport> incomeStatementReports) {
        FinanceIncomeStatementReport report = incomeStatementReports.stream()
                .filter(c -> StrUtil.isNotEmpty(c.getFormula()) && c.getFormula().contains(String.valueOf(number))).findFirst().orElse(null);
        if (ObjectUtil.isNull(report)) {
            return true;
        }
        return false;
    }

    @Override
    public void updateIncomeStatementConfig(FinanceUpdateReportConfigBO configBO) {
        FinanceIncomeStatementReport report = getById(configBO.getId());
        report.setFormula(JSON.toJSONString(configBO.getFormulaBOList()));
        updateById(report);
    }


    /**
     * 导出利润表
     *
     * @param requestBO
     */
    @Override
    public void exportIncomeStatementReport(FinanceReportRequestBO requestBO) {
        FinanceAccountSet accountSet = AccountSet.getAccountSet();
        //查询利润表
        List<JSONObject> mapList = report(requestBO);
        List<ExcelParseUtil.ExcelDataEntity> dataList = new ArrayList<>();
        dataList.add(ExcelParseUtil.toEntity("name", "项目"));
        dataList.add(ExcelParseUtil.toEntity("sort", "行次"));
        dataList.add(ExcelParseUtil.toEntity("yearValue", "本年累计金额"));
        dataList.add(ExcelParseUtil.toEntity("monthValue", "本月金额"));
        FinanceExcelParseUtil.exportExcelFinance(1, mapList, new FinanceExcelParseUtil.FinanceExcelParseService() {
            @Override
            public String getExcelName() {
                return "利润表";
            }

            @Override
            public String addCompany() {
                return "编制单位：" + accountSet.getCompanyName();
            }

            @Override
            public String addPeriod() {
                FinanceDetailAccountBO accountBO = new FinanceDetailAccountBO();
                accountBO.setStartTime(requestBO.getFromPeriod());
                accountBO.setEndTime(requestBO.getToPeriod());
                return certificateService.formatFinanceStartTime(accountBO);
            }

            @Override
            public String addUnit() {
                return "币别：" + currencyService.getCurrencyName(accountSet.getCurrencyId());
            }
        }, dataList);
    }

}
