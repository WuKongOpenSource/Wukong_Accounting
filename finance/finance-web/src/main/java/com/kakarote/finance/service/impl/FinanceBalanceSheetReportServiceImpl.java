package com.kakarote.finance.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.googlecode.aviator.AviatorEvaluator;
import com.kakarote.core.servlet.BaseServiceImpl;
import com.kakarote.core.utils.ExcelParseUtil;
import com.kakarote.core.utils.SeparatorUtil;
import com.kakarote.finance.common.*;
import com.kakarote.finance.constant.FinanceBalanceDirectionEnum;
import com.kakarote.finance.constant.FinanceRuleEnum;
import com.kakarote.finance.entity.BO.*;
import com.kakarote.finance.entity.PO.*;
import com.kakarote.finance.mapper.FinanceBalanceSheetReportMapper;
import com.kakarote.finance.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author zjj
 * @description: 资产负债表
 * @date 2021/8/26 15:01
 */
@Service
public class FinanceBalanceSheetReportServiceImpl extends BaseServiceImpl<FinanceBalanceSheetReportMapper, FinanceBalanceSheetReport> implements IFinanceBalanceSheetReportService {

    @Autowired
    private IFinanceSubjectService subjectService;

    @Autowired
    private IFinanceBalanceSheetConfigService balanceSheetConfigService;

    @Autowired
    private IFinanceCertificateService certificateService;

    @Autowired
    private IFinanceStatementCertificateService statementCertificateService;

    @Autowired
    private IFinanceParameterService parameterService;

    @Autowired
    IFinanceCurrencyService currencyService;

    @Override
    public List<JSONObject> report(FinanceReportRequestBO requestBO) {
        List<JSONObject> report = new ArrayList<>();
        List<FinanceBalanceSheetReport> balanceSheetReports = lambdaQuery()
                .eq(FinanceBalanceSheetReport::getAccountId, AccountSet.getAccountSetId())
                .eq(FinanceBalanceSheetReport::getType, requestBO.getType())
                .eq(FinanceBalanceSheetReport::getFromPeriod, requestBO.getFromPeriod())
                .eq(FinanceBalanceSheetReport::getToPeriod, requestBO.getToPeriod())
                .orderByAsc(FinanceBalanceSheetReport::getSortIndex)
                .list();
        if (CollUtil.isEmpty(balanceSheetReports)) {
            // 获取资产负债表配置
            List<FinanceBalanceSheetConfig> balanceSheetConfigs = balanceSheetConfigService.lambdaQuery()
                    .eq(FinanceBalanceSheetConfig::getAccountId, AccountSet.getAccountSetId())
                    .orderByAsc(FinanceBalanceSheetConfig::getSortIndex)
                    .list();
            balanceSheetConfigs.forEach(c -> {
                FinanceBalanceSheetReport r = BeanUtil.copyProperties(c, FinanceBalanceSheetReport.class);
                r.setId(null);
                r.setCreateTime(LocalDateTime.now());
                r.setType(requestBO.getType());
                r.setFromPeriod(requestBO.getFromPeriod());
                r.setToPeriod(requestBO.getToPeriod());
                save(r);
                balanceSheetReports.add(r);
            });
        }

        // 所有未删除的科目
        List<FinanceSubject> subjects = subjectService.lambdaQuery()
                .eq(FinanceSubject::getAccountId, AccountSet.getAccountSetId())
                .ne(FinanceSubject::getStatus, 3)
                .list();
        if (CollUtil.isEmpty(subjects)) {
            return report;
        }
        try {
            CashFlowStatementReportHolder.setSubjects(subjects);
            // 本期科目余额
            List<JSONObject> subjectBalance = certificateService.querySubjectBalance(AccountSet.getAccountSetId(), requestBO.getFromPeriod(), requestBO.getToPeriod());
            Map<Long, JSONObject> subjectBalanceMap = subjectBalance.stream()
                    .collect(Collectors.toMap(o -> o.getLong("subjectId"), Function.identity()));
            Boolean isFirstMonth = false;
            // 获取初始账期
            String initPeriod = parameterService.getStartPeriod();
            if (StrUtil.equals(requestBO.getFromPeriod(), initPeriod)) {
                isFirstMonth = true;
            }

            for (FinanceBalanceSheetReport config : balanceSheetReports) {
                Integer sort0 = config.getSort();
                String formula = config.getFormula();
                Map<String, String> keyMap = new HashMap<>();
                keyMap.put("name_resourceKey", "finance.config.statement.balance." + config.getName());
                config.setLanguageKeyMap(keyMap);
                BigDecimal initialPeriod = new BigDecimal(0);
                BigDecimal endPeriod = new BigDecimal(0);
                BigDecimal startPeriod = new BigDecimal(0);
                if (formula.contains("L")) {
                    formula = JSON.parseArray(formula, String.class).get(0);
                    String[] formulas = formula.split(SeparatorUtil.REGEX_OPERATOR);
                    Map<String, Object> initialEnv = new HashMap<>();
                    Map<String, Object> endEnv = new HashMap<>();
                    Map<String, Object> startEnv = new HashMap<>();
                    for (String f : formulas) {
                        Integer sort = Integer.valueOf(f.replaceAll("L", ""));
                        JSONObject valueObject = filterReport(report, sort);
                        initialEnv.put(f, valueObject.getBigDecimal("initialPeriod"));
                        endEnv.put(f, valueObject.getBigDecimal("endPeriod"));
                        startEnv.put(f, valueObject.getBigDecimal("startPeriod"));
                    }
                    initialPeriod = initialPeriod.add(new BigDecimal(AviatorEvaluator.execute(formula, initialEnv).toString()));
                    endPeriod = endPeriod.add(new BigDecimal(AviatorEvaluator.execute(formula, endEnv).toString()));
                    startPeriod = startPeriod.add(new BigDecimal(AviatorEvaluator.execute(formula, startEnv).toString()));
                } else {
                    List<FinanceFormulaBO> formulaBOS = JSON.parseArray(formula, FinanceFormulaBO.class);
                    for (FinanceFormulaBO formulaBO : formulaBOS) {
                        BigDecimal i = this.calculateBeginBValue(formulaBO, subjectBalanceMap);

                        BigDecimal e = this.calculateEndValue(formulaBO, subjectBalanceMap);
                        BigDecimal s = BigDecimal.ZERO;
                        if (isFirstMonth) {
                            s = this.calculateInitialValue(formulaBO, subjectBalanceMap);
                        }
                        formulaBO.setStartPeriod(s);
                        formulaBO.setInitialPeriod(i);
                        formulaBO.setEndPeriod(e);
                        if (StrUtil.equals("+", formulaBO.getOperator())) {
                            initialPeriod = initialPeriod.add(i);
                            endPeriod = endPeriod.add(e);
                            startPeriod = startPeriod.add(s);
                        } else {
                            initialPeriod = initialPeriod.subtract(i);
                            endPeriod = endPeriod.subtract(e);
                            startPeriod = startPeriod.subtract(s);
                        }
                        FinanceSubject subject = CashFlowStatementReportHolder.getSubjects(Long.valueOf(formulaBO.getSubjectId()));
                        if (ObjectUtil.isEmpty(subject)) {
                            continue;
                        }
                        formulaBO.setSubjectName(subject.getSubjectName());
                        formulaBO.setSubjectNumber(subject.getNumber());
                    }
                    config.setFormula(JSON.toJSONString(formulaBOS));
                }
                config.setInitialPeriod(initialPeriod);
                config.setEndPeriod(endPeriod);
                config.setStartPeriod(startPeriod);
                JSONObject jsonObject = new JSONObject(BeanUtil.beanToMap(config));
                report.add(jsonObject);
            }
            return report;
        } finally {
            CashFlowStatementReportHolder.removeSubjects();
        }
    }

    /**
     * 计算期初-当前月是第一个月才会计算
     *
     * @return
     */
    private BigDecimal calculateInitialValue(FinanceFormulaBO formulaBO,
                                             Map<Long, JSONObject> subjectBalanceMap) {
        BigDecimal bigDecimal = new BigDecimal(0);
        String operatorJ = "+";
        Long subjectId = Long.valueOf(formulaBO.getSubjectId());
        if (ObjectUtil.isNull(subjectId)) {
            FinanceSubject subject = CashFlowStatementReportHolder.getSubjectByNumber(formulaBO.getSubjectNumber());
            if (ObjectUtil.isNotNull(subject)) {
                subjectId = subject.getSubjectId();
            }
        } else {
            FinanceSubject subject = CashFlowStatementReportHolder.getSubjects(subjectId);
            if (ObjectUtil.isNull(subject)) {
                FinanceSubject subjectByNumber = CashFlowStatementReportHolder.getSubjectByNumber(formulaBO.getSubjectNumber());
                if (ObjectUtil.isNotNull(subjectByNumber)) {
                    subjectId = subjectByNumber.getSubjectId();
                }
            }
        }
        FinanceSubject subject = CashFlowStatementReportHolder.getSubjects(subjectId);
        if (ObjectUtil.isEmpty(subject)) {
            return BigDecimal.ZERO;
        }
        Integer balanceDirection = subject.getBalanceDirection();
        String operator = formulaBO.getOperator();
        FinanceRuleEnum ruleEnum = FinanceRuleEnum.parseType(formulaBO.getRules());

        JSONObject balanceObject = subjectBalanceMap.get(subjectId);
        // 借方期初
        BigDecimal debtorInitialBalance = balanceObject.getBigDecimal("debtorInitialBalance");
        // 贷方期初
        BigDecimal creditInitialBalance = balanceObject.getBigDecimal("creditInitialBalance");
        // 余额
        if (ObjectUtil.equal(FinanceRuleEnum.BALANCE, ruleEnum)) {
            // 子科目的科目方向与父科目的方向相同
            if (ObjectUtil.equal(1, balanceDirection)) {
                bigDecimal = bigDecimal.add(debtorInitialBalance);
            } else {
                bigDecimal = bigDecimal.add(creditInitialBalance);
            }
        }
        // 借方余额
        else if (ObjectUtil.equal(FinanceRuleEnum.LEND_BALANCE, ruleEnum)) {
            if (StrUtil.equals(operatorJ, operator)) {
                bigDecimal = bigDecimal.add(debtorInitialBalance);
            } else {
                bigDecimal = bigDecimal.subtract(debtorInitialBalance);
            }
        }
        // 贷方余额
        else if (ObjectUtil.equal(FinanceRuleEnum.LOAN_BALANCE, ruleEnum)) {
            if (StrUtil.equals(operatorJ, operator)) {
                bigDecimal = bigDecimal.add(creditInitialBalance);
            } else {
                bigDecimal = bigDecimal.subtract(creditInitialBalance);
            }
        }
        // 科目借方余额
        else if (ObjectUtil.equal(FinanceRuleEnum.SUBJECT_LEND_BALANCE, ruleEnum)) {
            if (debtorInitialBalance.compareTo(BigDecimal.ZERO) == 0 && creditInitialBalance.compareTo(BigDecimal.ZERO) == -1) {
                debtorInitialBalance = creditInitialBalance.negate();
            }
            // 科目借方余额小于0，则不取
            if (debtorInitialBalance.compareTo(BigDecimal.ZERO) < 0) {
                debtorInitialBalance = BigDecimal.ZERO;
            }
            if (StrUtil.equals(operatorJ, operator)) {
                bigDecimal = bigDecimal.add(debtorInitialBalance);
            } else {
                bigDecimal = bigDecimal.subtract(debtorInitialBalance);
            }
        }
        // 科目贷方余额
        else if (ObjectUtil.equal(FinanceRuleEnum.SUBJECT_LOAN_BALANCE, ruleEnum)) {
            if (creditInitialBalance.compareTo(BigDecimal.ZERO) == 0 && debtorInitialBalance.compareTo(BigDecimal.ZERO) == -1) {
                creditInitialBalance = debtorInitialBalance.negate();
            }
            // 科目贷方余额小于0，则不取
            if (creditInitialBalance.compareTo(BigDecimal.ZERO) < 0) {
                creditInitialBalance = BigDecimal.ZERO;
            }
            if (StrUtil.equals(operatorJ, operator)) {
                bigDecimal = bigDecimal.add(creditInitialBalance);
            } else {
                bigDecimal = bigDecimal.subtract(creditInitialBalance);
            }
        }
        return bigDecimal;
    }

    /**
     * 年初数
     *
     * @param formulaBO
     * @param subjectBalanceMap
     * @return
     */
    private BigDecimal calculateBeginBValue(FinanceFormulaBO formulaBO,
                                            Map<Long, JSONObject> subjectBalanceMap) {
        Long subjectId = StrUtil.isEmpty(formulaBO.getSubjectId()) ? null : Long.valueOf(formulaBO.getSubjectId());
        if (ObjectUtil.isNull(subjectId)) {
            FinanceSubject subject = CashFlowStatementReportHolder.getSubjectByNumber(formulaBO.getSubjectNumber());
            if (ObjectUtil.isNotNull(subject)) {
                subjectId = subject.getSubjectId();
            }
        } else {
            FinanceSubject subject = CashFlowStatementReportHolder.getSubjects(subjectId);
            if (ObjectUtil.isNull(subject)) {
                FinanceSubject subjectByNumber = CashFlowStatementReportHolder.getSubjectByNumber(formulaBO.getSubjectNumber());
                if (ObjectUtil.isNotNull(subjectByNumber)) {
                    subjectId = subjectByNumber.getSubjectId();
                }
            }
        }
        // 父科目
        FinanceSubject subject = CashFlowStatementReportHolder.getSubjects(subjectId);
        if (ObjectUtil.isEmpty(subject)) {
            return BigDecimal.ZERO;
        }
        Integer balanceDirection = subject.getBalanceDirection();
//		String operator = formulaBO.getOperator();
        FinanceRuleEnum ruleEnum = FinanceRuleEnum.parseType(formulaBO.getRules());
        List<Long> childSubjectIds = CashFlowStatementReportHolder.getFirstChildSubjectIds(subjectId);
        if (CollUtil.isEmpty(childSubjectIds)) {
            childSubjectIds.add(subjectId);
        }
        BigDecimal bigDecimal = new BigDecimal(0);
        for (Long childSubjectId : childSubjectIds) {
            JSONObject balanceObject = subjectBalanceMap.get(childSubjectId);
            if (ObjectUtil.isNotNull(balanceObject)) {
                // 子科目
                FinanceSubject subSubject = CashFlowStatementReportHolder.getSubjects(childSubjectId);
                if (ObjectUtil.isEmpty(subSubject)) {
                    continue;
                }
                Integer subBalanceDirection = subSubject.getBalanceDirection();

                // 年初余额
                BigDecimal beginningBalance = balanceObject.getBigDecimal("beginningBalance");
                // 借方余额
                BigDecimal debtorBeginningBalance = BigDecimal.ZERO;
                // 贷方余额
                BigDecimal creditBeginningBalance = BigDecimal.ZERO;
                if (ObjectUtil.equal(1, subBalanceDirection)) {
                    debtorBeginningBalance = beginningBalance;
                } else {
                    creditBeginningBalance = beginningBalance;
                }
                // 余额
                if (ObjectUtil.equal(FinanceRuleEnum.BALANCE, ruleEnum)) {
                    // 子科目的科目方向与父科目的方向相同
                    BigDecimal value;
                    if (ObjectUtil.equal(balanceDirection, subBalanceDirection)) {
                        value = beginningBalance;
                    } else {
                        value = beginningBalance.negate();
                    }
                    bigDecimal = bigDecimal.add(value);
                }
                // 借方余额
                else if (ObjectUtil.equal(FinanceRuleEnum.LEND_BALANCE, ruleEnum)) {
                    bigDecimal = bigDecimal.add(debtorBeginningBalance);
                }
                // 贷方余额
                else if (ObjectUtil.equal(FinanceRuleEnum.LOAN_BALANCE, ruleEnum)) {
                    bigDecimal = bigDecimal.add(creditBeginningBalance);
                }
                // 科目借方余额
                else if (ObjectUtil.equal(FinanceRuleEnum.SUBJECT_LEND_BALANCE, ruleEnum)) {
                    if (debtorBeginningBalance.compareTo(BigDecimal.ZERO) == 0 && creditBeginningBalance.compareTo(BigDecimal.ZERO) == -1) {
                        debtorBeginningBalance = creditBeginningBalance.negate();
                    }
                    // 科目借方余额小于0，则不取
                    if (debtorBeginningBalance.compareTo(BigDecimal.ZERO) < 0) {
                        debtorBeginningBalance = BigDecimal.ZERO;
                    }
                    bigDecimal = bigDecimal.add(debtorBeginningBalance);
                }
                // 科目贷方余额
                else if (ObjectUtil.equal(FinanceRuleEnum.SUBJECT_LOAN_BALANCE, ruleEnum)) {
                    if (creditBeginningBalance.compareTo(BigDecimal.ZERO) == 0 && debtorBeginningBalance.compareTo(BigDecimal.ZERO) == -1) {
                        creditBeginningBalance = debtorBeginningBalance.negate();
                    }
                    // 科目贷方余额小于0，则不取
                    if (creditBeginningBalance.compareTo(BigDecimal.ZERO) < 0) {
                        creditBeginningBalance = BigDecimal.ZERO;
                    }
                    bigDecimal = bigDecimal.add(creditBeginningBalance);
                }
            }
        }
        return bigDecimal;
    }

    /**
     * 期末数
     *
     * @param formulaBO
     * @param subjectBalanceMap
     * @return
     */
    @Override
    public BigDecimal calculateEndValue(FinanceFormulaBO formulaBO,
                                        Map<Long, JSONObject> subjectBalanceMap) {
        Long subjectId = StrUtil.isEmpty(formulaBO.getSubjectId()) ? null : Long.valueOf(formulaBO.getSubjectId());
        if (ObjectUtil.isNull(subjectId)) {
            FinanceSubject subject = CashFlowStatementReportHolder.getSubjectByNumber(formulaBO.getSubjectNumber());
            if (ObjectUtil.isNotNull(subject)) {
                subjectId = subject.getSubjectId();
            }
        } else {
            FinanceSubject subject = CashFlowStatementReportHolder.getSubjects(subjectId);
            if (ObjectUtil.isNull(subject)) {
                FinanceSubject subjectByNumber = CashFlowStatementReportHolder.getSubjectByNumber(formulaBO.getSubjectNumber());
                if (ObjectUtil.isNotNull(subjectByNumber)) {
                    subjectId = subjectByNumber.getSubjectId();
                }
            }
        }
        FinanceSubject parentSubject = CashFlowStatementReportHolder.getSubjects(subjectId);
        if (ObjectUtil.isEmpty(parentSubject)) {
            return BigDecimal.ZERO;
        }
//		String operator = formulaBO.getOperator();
        FinanceRuleEnum ruleEnum = FinanceRuleEnum.parseType(formulaBO.getRules());
        List<Long> childSubjectIds = CashFlowStatementReportHolder.getFirstChildSubjectIds(subjectId);
        if (CollUtil.isEmpty(childSubjectIds)) {
            childSubjectIds.add(subjectId);
        }
        BigDecimal bigDecimal = new BigDecimal(0);
        for (Long childSubjectId : childSubjectIds) {
            JSONObject balanceObject = subjectBalanceMap.get(childSubjectId);
            if (ObjectUtil.isNotNull(balanceObject)) {
                FinanceSubject subject = CashFlowStatementReportHolder.getSubjects(childSubjectId);
                if (ObjectUtil.isEmpty(subject)) {
                    continue;
                }
                // 期末借方余额
                BigDecimal debtorEndBalance = Optional.ofNullable(
                        balanceObject.getBigDecimal("debtorEndBalance")).orElse(BigDecimal.ZERO);
                // 期末贷方余额
                BigDecimal creditEndBalance = Optional.ofNullable(
                        balanceObject.getBigDecimal("creditEndBalance")).orElse(BigDecimal.ZERO);
                // 余额
                if (ObjectUtil.equal(FinanceRuleEnum.BALANCE, ruleEnum)) {
                    BigDecimal value;
                    if (ObjectUtil.equal(FinanceBalanceDirectionEnum.LEND.getType(), parentSubject.getBalanceDirection())) {
                        value = debtorEndBalance.subtract(creditEndBalance);
                    } else {
                        value = creditEndBalance.subtract(debtorEndBalance);
                    }
                    bigDecimal = bigDecimal.add(value);
                }
                // 借方余额
                else if (ObjectUtil.equal(FinanceRuleEnum.LEND_BALANCE, ruleEnum)) {
                    if (debtorEndBalance.compareTo(BigDecimal.ZERO) == 1) {
                        bigDecimal = bigDecimal.add(debtorEndBalance);
                    }
                    if (debtorEndBalance.compareTo(BigDecimal.ZERO) == 0 && creditEndBalance.compareTo(BigDecimal.ZERO) < 0) {
                        bigDecimal = bigDecimal.add(creditEndBalance.negate());
                    }
                }
                // 贷方余额
                else if (ObjectUtil.equal(FinanceRuleEnum.LOAN_BALANCE, ruleEnum)) {
                    if (creditEndBalance.compareTo(BigDecimal.ZERO) == 1) {
                        bigDecimal = bigDecimal.add(creditEndBalance);
                    }
                    if (creditEndBalance.compareTo(BigDecimal.ZERO) == 0 && debtorEndBalance.compareTo(BigDecimal.ZERO) < 0) {
                        bigDecimal = bigDecimal.add(debtorEndBalance.negate());
                    }
                }
                // 科目借方余额
                else if (ObjectUtil.equal(FinanceRuleEnum.SUBJECT_LEND_BALANCE, ruleEnum)) {
                    if (debtorEndBalance.compareTo(BigDecimal.ZERO) == 0 && creditEndBalance.compareTo(BigDecimal.ZERO) == -1) {
                        debtorEndBalance = creditEndBalance.negate();
                    }
                    // 科目借方余额小于0，则不取
                    if (debtorEndBalance.compareTo(BigDecimal.ZERO) < 0) {
                        debtorEndBalance = BigDecimal.ZERO;
                    }
                    bigDecimal = bigDecimal.add(debtorEndBalance);
                }
                // 科目贷方余额
                else if (ObjectUtil.equal(FinanceRuleEnum.SUBJECT_LOAN_BALANCE, ruleEnum)) {
                    if (creditEndBalance.compareTo(BigDecimal.ZERO) == 0 && debtorEndBalance.compareTo(BigDecimal.ZERO) == -1) {
                        creditEndBalance = debtorEndBalance.negate();
                    }
                    // 科目贷方余额小于0，则不取
                    if (creditEndBalance.compareTo(BigDecimal.ZERO) < 0) {
                        creditEndBalance = BigDecimal.ZERO;
                    }
                    bigDecimal = bigDecimal.add(creditEndBalance);
                }
            }
        }
        return bigDecimal;
    }

    @Override
    public JSONObject filterByExpression(FinanceReportRequestBO requestBO) {
        String sort = SeparatorUtil.parseStrBetweenBracket(requestBO.getExpression()).get(0).split(",")[0];
        if (requestBO.isQueryLastPeriod()) {
            List<JSONObject> lastReport = CashFlowStatementReportHolder.getLastBalanceSheet();
            if (ObjectUtil.isNull(lastReport) || CollUtil.isEmpty(lastReport)) {
                FinanceBalanceRequestBO lastRequestBO = BeanUtil.copyProperties(requestBO, FinanceBalanceRequestBO.class);
                String initialPeriod = parameterService.getStartPeriod();
                if (StrUtil.equals(requestBO.getFromPeriod(), initialPeriod)) {
                    return null;
                }
                String lastPeriod = PeriodUtils.previousPeriod(requestBO.getFromPeriod(), -1);
                if (StrUtil.isNotEmpty(lastPeriod)) {
                    // 月报
                    if (ObjectUtil.equal(1, requestBO.getType())) {
                        lastRequestBO.setFromPeriod(lastPeriod);
                        lastRequestBO.setToPeriod(lastPeriod);
                    } else { // 季报
                        lastRequestBO.setFromPeriod(PeriodUtils.previousPeriod(requestBO.getFromPeriod(), -3));
                        lastRequestBO.setToPeriod(PeriodUtils.previousPeriod(requestBO.getToPeriod(), -3));
                    }
                    lastReport = this.report(lastRequestBO);
                    CashFlowStatementReportHolder.setLastBalanceSheet(lastReport);
                } else {
                    return null;
                }
            }
            return this.filterReport(lastReport, Integer.valueOf(sort));
        } else {
            List<JSONObject> report = CashFlowStatementReportHolder.getBalanceSheet();
            if (ObjectUtil.isNull(report) || CollUtil.isEmpty(report)) {
                report = this.report(requestBO);
                CashFlowStatementReportHolder.setBalanceSheet(report);
            }
            return this.filterReport(report, Integer.valueOf(sort));
        }
    }

    @Override
    public JSONObject balanceCheck(FinanceReportRequestBO requestBO) {
        FinanceParameter parameter = parameterService.getParameter();
        List<FinanceSubject> allSubjects = subjectService.queryAll();
        if (CollUtil.isEmpty(allSubjects)) {
            return new JSONObject();
        }
        // 获取所有非损益类科目
        List<FinanceSubject> firstGradeSubjects = allSubjects.stream()
                .filter(o -> ObjectUtil.notEqual(5, o.getType()))
                .filter(o -> (ObjectUtil.equal(0L, o.getParentId()) || ObjectUtil.isNull(o.getParentId())))
                .collect(Collectors.toList());

        // 获取可编辑的资产负债表配置
        List<FinanceBalanceSheetReport> balanceSheetReports = lambdaQuery()
                .eq(FinanceBalanceSheetReport::getAccountId, AccountSet.getAccountSetId())
                .eq(FinanceBalanceSheetReport::getType, requestBO.getType())
                .eq(FinanceBalanceSheetReport::getFromPeriod, requestBO.getFromPeriod())
                .eq(FinanceBalanceSheetReport::getToPeriod, requestBO.getToPeriod())
                .eq(FinanceBalanceSheetReport::getEditable, 1)
                .orderByAsc(FinanceBalanceSheetReport::getSortIndex)
                .list();
        // 获取科目余额表
        FinanceSubjectBalanceQueryBO detailAccountBO = new FinanceSubjectBalanceQueryBO();
        detailAccountBO.setStartTime(requestBO.getFromPeriod());
        detailAccountBO.setEndTime(requestBO.getToPeriod());
        List<JSONObject> detailBalance = certificateService.querySubjectBalance(detailAccountBO);
        // 检查科目是否都已经在资产负债表中设置科目公式
        List<FinanceSubject> notContains = new ArrayList<>();
        for (FinanceSubject subject : firstGradeSubjects) {
            boolean notContainsSubject = this.notContainsSubject(subject.getSubjectId(), balanceSheetReports);
            if (notContainsSubject) {
                JSONObject subjectBalance = detailBalance.stream().filter(o -> ObjectUtil.equal(subject.getSubjectId(), o.getLong("subjectId")))
                        .findFirst().orElse(null);
                if (ObjectUtil.isNotNull(subjectBalance)) {
                    if (subjectBalance.getBigDecimal("debtorCurrentBalance").compareTo(BigDecimal.ZERO) != 0
                            || subjectBalance.getBigDecimal("creditCurrentBalance").compareTo(BigDecimal.ZERO) != 0
                            || subjectBalance.getBigDecimal("debtorInitialBalance").compareTo(BigDecimal.ZERO) != 0
                            || subjectBalance.getBigDecimal("creditInitialBalance").compareTo(BigDecimal.ZERO) != 0) {
                        if (ObjectUtil.isNotNull(subjectBalance)) {
                            notContains.add(subject);
                        }
                    }
                }
            }
        }
        // 检查是否结转
        boolean settled = true;
        List<String> periods = PeriodUtils.getPeriodByFromTo(requestBO.getFromPeriod(), requestBO.getToPeriod());
        for (String period : periods) {
            settled = statementCertificateService.lossSubjectSettled(period);
            if (!settled) {
                settled = false;
                break;
            }
        }

        try {
            // 资本化支出 过滤掉
            String number = RuleUtils.transNumber("4404", "4-2-2-2", parameter.getRule());
            FinanceSubject subject = CashFlowStatementReportHolder.getSubjectByNumber(number);
            if (ObjectUtil.isNotNull(subject)) {
                notContains = notContains.stream().filter(o -> ObjectUtil.notEqual(o.getSubjectId(), subject.getSubjectId()))
                        .collect(Collectors.toList());
            }
            List<JSONObject> report = this.report(requestBO);
            JSONObject sort30 = this.filterReport(report, 30);
            JSONObject sort53 = this.filterReport(report, 53);
            JSONObject object = new JSONObject();
            object.put("notContains", notContains);
            object.put("settled", settled);
            String initialPeriod = "initialPeriod";
            if (sort30.getBigDecimal(initialPeriod).compareTo(sort53.getBigDecimal(initialPeriod)) == 0
                    && sort30.getBigDecimal("endPeriod").compareTo(sort53.getBigDecimal("endPeriod")) == 0) {
                object.put("balanced", true);
            } else {
                object.put("balanced", false);
            }
            return object;
        } finally {
            CashFlowStatementReportHolder.removeSubjects();
        }
    }

    /**
     * 是否配置该科目
     *
     * @param subjectId
     * @param balanceSheetReports
     * @return
     */
    private boolean notContainsSubject(Long subjectId, List<FinanceBalanceSheetReport> balanceSheetReports) {
        FinanceBalanceSheetReport config = balanceSheetReports.stream()
                .filter(c -> (StrUtil.isNotEmpty(c.getFormula()) && c.getFormula().contains(String.valueOf(subjectId)))).findFirst().orElse(null);
        try {
            if (ObjectUtil.isNull(config)) {
                List<Long> childSubjectIds = new ArrayList<>();
                CashFlowStatementReportHolder.getChildSubjectIds(childSubjectIds, subjectId);
                if (CollUtil.isNotEmpty(childSubjectIds)) {
                    for (Long childSubjectId : childSubjectIds) {
                        if (this.notContainsSubject(childSubjectId, balanceSheetReports)) {
                            continue;
                        } else {
                            return false;
                        }
                    }
                }
                return true;
            }
            return false;
        } finally {
            CashFlowStatementReportHolder.removeSubjects();
        }
    }


    @Override
    public void updateBalanceSheetConfig(FinanceUpdateReportConfigBO configBO) {
        FinanceBalanceSheetReport report = getById(configBO.getId());
        report.setFormula(JSON.toJSONString(configBO.getFormulaBOList()));
        updateById(report);
        Optional<FinanceBalanceSheetConfig> configOptional = balanceSheetConfigService.lambdaQuery()
                .eq(FinanceBalanceSheetConfig::getName, report.getName())
                .eq(FinanceBalanceSheetConfig::getSort, report.getSort())
                .eq(FinanceBalanceSheetConfig::getGrade, report.getGrade())
                .oneOpt();
        configOptional.ifPresent(config -> {
            config.setFormula(report.getFormula());
            balanceSheetConfigService.updateById(config);
        });
    }

    /**
     * 导出资产负债表
     *
     * @param requestBO
     */
    @Override
    public void exportBalanceSheetReport(FinanceReportRequestBO requestBO) {
        FinanceAccountSet accountSet = AccountSet.getAccountSet();
        //查询资产负债表
        List<Map<String, Object>> mapList = new ArrayList<>();
        if (CollUtil.isNotEmpty(requestBO.getExportDataList())) {
            mapList = requestBO.getExportDataList();
            for (Map map : mapList) {
                DecimalFormat df = new DecimalFormat("##0.00");
                if (ObjectUtil.isNotEmpty(map.get("endPeriod"))) {
                    if (map.get("endPeriod").toString().matches("^\\d+$$")) {
                        map.put("endPeriod", Convert.toBigDecimal(df.format(map.get("endPeriod"))));
                    }
                }

                if (ObjectUtil.isNotEmpty(map.get("initialPeriod"))) {
                    if (map.get("initialPeriod").toString().matches("^\\d+$$")) {
                        map.put("initialPeriod", Convert.toBigDecimal(df.format(map.get("initialPeriod"))));
                    }
                }
                if (ObjectUtil.isNotEmpty(map.get("endPeriod0"))) {
                    if (map.get("endPeriod0").toString().matches("^\\d+$$")) {
                        map.put("endPeriod0", Convert.toBigDecimal(df.format(map.get("endPeriod0"))));
                    }
                }
                if (ObjectUtil.isNotEmpty(map.get("initialPeriod0"))) {
                    if (map.get("initialPeriod0").toString().matches("^\\d+$$")) {
                        map.put("initialPeriod0", Convert.toBigDecimal(df.format(map.get("initialPeriod0"))));
                    }
                }
            }
        }
        List<ExcelParseUtil.ExcelDataEntity> dataList = new ArrayList<>();
        dataList.add(ExcelParseUtil.toEntity("name", "资产"));
        dataList.add(ExcelParseUtil.toEntity("sort", "行次"));
        dataList.add(ExcelParseUtil.toEntity("endPeriod", "期末数"));
        dataList.add(ExcelParseUtil.toEntity("initialPeriod", "年初数"));
        dataList.add(ExcelParseUtil.toEntity("name0", "负债和所有者（或股东）"));
        dataList.add(ExcelParseUtil.toEntity("sort0", "行次 "));
        dataList.add(ExcelParseUtil.toEntity("endPeriod0", "期末数 "));
        dataList.add(ExcelParseUtil.toEntity("initialPeriod0", "年初数 "));
        FinanceExcelParseUtil.exportExcelFinance(3, mapList, new FinanceExcelParseUtil.FinanceExcelParseService() {
            @Override
            public String getExcelName() {
                return "资产负债表";
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
