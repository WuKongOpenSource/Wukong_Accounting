package com.kakarote.finance.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.kakarote.core.servlet.BaseServiceImpl;
import com.kakarote.core.utils.AdminLanguageUtil;
import com.kakarote.finance.common.AccountSet;
import com.kakarote.finance.common.CashFlowStatementReportHolder;
import com.kakarote.finance.common.PeriodUtils;
import com.kakarote.finance.common.RuleUtils;
import com.kakarote.finance.constant.FinanceCodeEnum;
import com.kakarote.finance.entity.BO.FinanceFormulaBO;
import com.kakarote.finance.entity.BO.FinanceReportRequestBO;
import com.kakarote.finance.entity.PO.*;
import com.kakarote.finance.entity.VO.FinanceIndicatorVO;
import com.kakarote.finance.mapper.FinanceIndicatorMapper;
import com.kakarote.finance.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * @author zjj
 * @ClassName FinanceIndicatorServiceImpl.java
 * @Description 财务指标
 * @createTime 2021-10-08
 */
@Service
public class FinanceIndicatorServiceImpl extends BaseServiceImpl<FinanceIndicatorMapper, FinanceIndicator> implements IFinanceIndicatorService {

    @Autowired
    private IFinanceBalanceSheetReportService balanceSheetReportService;

    @Autowired
    private IFinanceIncomeStatementReportService incomeStatementReportService;

    @Autowired
    private IFinanceCertificateService certificateService;

    @Autowired
    private IFinanceSubjectService subjectService;

    @Autowired
    private IFinanceCashFlowStatementExtendDataService cashFlowStatementExtendDataService;

    @Autowired
    private IFinanceStatementSettleService settleService;

    @Autowired
    private IFinanceParameterService financeParameterService;

    @Autowired
    private FinanceVoucherServiceImpl financeVoucherServiceImpl;

    @Autowired
    private IFinanceIncomeStatementConfigService financeIncomeStatementConfigService;

    @Autowired
    private IFinanceBalanceSheetConfigService financeBalanceSheetConfigService;

    @Autowired
    private IFinanceCashFlowStatementConfigService financeCashFlowStatementConfigService;

    @Autowired
    private IFinanceStatementTemplateService financeStatementTemplateService;

    @Autowired
    private IFinanceStatementService financeStatementService;

    @Autowired
    private IFinanceAdjuvantService financeAdjuvantServiceImpl;

    @Autowired
    private IFinanceSubjectService financeSubjectServiceImpl;

    @Autowired
    private IFinanceStatementSubjectService financeStatementSubjectServiceImpl;

    /**
     * 特殊处理的行次，如果公式内的值小于0，返回0
     */
    private static final List<String> SORT_LIST = Arrays.asList("应收账款", "应付账款", "应交税费");

    @Override
    public List<FinanceIndicatorVO> indicatorList() {
        List<FinanceIndicatorVO> result = new ArrayList<>();
        if (ObjectUtil.isNull(AccountSet.getAccountSetId())) {
            return result;
        }
        String currentPeriod = settleService.getCurrentPeriod();
        // 系统参数
        FinanceParameter parameter = financeParameterService.getParameter();
        if (ObjectUtil.isEmpty(parameter)) {
            return result;
        }
        List<FinanceIndicator> indicators = lambdaQuery().list();

        FinanceReportRequestBO requestBO = new FinanceReportRequestBO();
        requestBO.setType(1);
        requestBO.setFromPeriod(currentPeriod);
        requestBO.setToPeriod(currentPeriod);
        requestBO.setAccountId(AccountSet.getAccountSetId());
        List<JSONObject> balanceSheetReports = balanceSheetReportService.report(requestBO);
        List<JSONObject> incomeStatementReports = incomeStatementReportService.report(requestBO);

        // 所有未删除的科目
        List<FinanceSubject> subjects = subjectService.lambdaQuery()
                .eq(FinanceSubject::getAccountId, AccountSet.getAccountSetId())
                .ne(FinanceSubject::getStatus, 3)
                .list();
        try {
            CashFlowStatementReportHolder.setSubjects(subjects);
            // 本期科目余额
            List<JSONObject> subjectBalance = certificateService.querySubjectBalance(AccountSet.getAccountSetId(), requestBO.getFromPeriod(), requestBO.getToPeriod());
            Map<Long, JSONObject> subjectBalanceMap = subjectBalance.stream()
                    .collect(Collectors.toMap(o -> o.getLong("subjectId"), Function.identity()));
            for (FinanceIndicator indicator : indicators) {
                BigDecimal value = new BigDecimal(0);
                FinanceIndicatorVO indicatorVO = BeanUtil.copyProperties(indicator, FinanceIndicatorVO.class);
                List<FinanceFormulaBO> formulaBOS = JSON.parseArray(indicator.getFormula(), FinanceFormulaBO.class);

                // 资产负债表
                if (ObjectUtil.equal(1, indicator.getType())) {
                    for (FinanceFormulaBO formulaBO : formulaBOS) {
                        if (ObjectUtil.isNotNull(formulaBO.getLine())) {
                            JSONObject jsonObject = balanceSheetReportService.filterReport(balanceSheetReports, formulaBO.getLine());
                            if (ObjectUtil.isNotEmpty(jsonObject)) {
                                value = value.add(jsonObject.getBigDecimal("endPeriod"));
                            } else {
                                value = BigDecimal.ZERO;
                            }

                        } else {
                            String number = formulaBO.getSubjectNumber();
                            number = RuleUtils.transNumber(number, "4-2-2-2", parameter.getRule());
                            FinanceSubject subject = CashFlowStatementReportHolder.getSubjectByNumber(number);
                            if (ObjectUtil.isNotNull(subject)) {
                                formulaBO.setSubjectId(String.valueOf(subject.getSubjectId()));
                            }
                            value = value.add(balanceSheetReportService.calculateEndValue(formulaBO, subjectBalanceMap));
                        }
                    }
                } else if (ObjectUtil.equal(2, indicator.getType())) {
                    // 利润表
                    for (FinanceFormulaBO formulaBO : formulaBOS) {
                        if (ObjectUtil.isNotNull(formulaBO.getLine())) {
                            JSONObject jsonObject = incomeStatementReportService.filterReport(incomeStatementReports, formulaBO.getLine());
                            if (ObjectUtil.isNotEmpty(jsonObject)) {
                                value = value.add(jsonObject.getBigDecimal("monthValue"));
                            } else {
                                value = BigDecimal.ZERO;
                            }
                        } else {
                            String number = formulaBO.getSubjectNumber();
                            number = RuleUtils.transNumber(number, "4-2-2-2", parameter.getRule());
                            FinanceSubject subject = CashFlowStatementReportHolder.getSubjectByNumber(number);
                            formulaBO.setSubjectId(String.valueOf(subject.getSubjectId()));
                            formulaBO.setSubjectNumber(number);
                            value = value.add(cashFlowStatementExtendDataService.calculateCurrentValue(formulaBO, subjectBalanceMap));
                        }
                    }
                }
                if (SORT_LIST.contains(indicator.getName())) {
                    if (value.compareTo(BigDecimal.ZERO) < 0) {
                        value = BigDecimal.ZERO;
                    }
                }


                indicatorVO.setValue(value);
                //添加语言包key
                Map<String, String> keyMap = new HashMap<>();
                keyMap.put("name_resourceKey", "finance.indicator." + indicatorVO.getName());
                indicatorVO.setLanguageKeyMap(keyMap);

                result.add(indicatorVO);
            }
            return result;
        } finally {
            CashFlowStatementReportHolder.removeSubjects();
        }
    }

    @Override
    public List<JSONObject> statistics(Long id) {
        List<JSONObject> result = new ArrayList<>();
        FinanceIndicator indicator = getById(id);
        // 系统参数
        FinanceParameter parameter = financeParameterService.getParameter();
        List<FinanceFormulaBO> formulaBOS = JSON.parseArray(indicator.getFormula(), FinanceFormulaBO.class);
        String currentPeriod = settleService.getCurrentPeriod();
        List<String> periods = PeriodUtils.getOneYearPeriod(currentPeriod);
        FinanceReportRequestBO requestBO = new FinanceReportRequestBO();
        requestBO.setType(1);
        requestBO.setAccountId(AccountSet.getAccountSetId());
        // 所有未删除的科目
        List<FinanceSubject> subjects = subjectService.lambdaQuery()
                .eq(FinanceSubject::getAccountId, AccountSet.getAccountSetId())
                .ne(FinanceSubject::getStatus, 3)
                .list();

        LocalDateTime startTime = parameter.getStartTime();
        String startPeriod = LocalDateTimeUtil.format(startTime, PeriodUtils.PERIOD_FORMAT);

        try {
            CashFlowStatementReportHolder.setSubjects(subjects);
            for (FinanceFormulaBO formulaBO : formulaBOS) {
                if (ObjectUtil.isNull(formulaBO.getLine())) {
                    String number = formulaBO.getSubjectNumber();
                    number = RuleUtils.transNumber(number, "4-2-2-2", parameter.getRule());
                    FinanceSubject subject = CashFlowStatementReportHolder.getSubjectByNumber(number);
                    formulaBO.setSubjectId(String.valueOf(subject.getSubjectId()));
                    formulaBO.setSubjectNumber(number);
                }
            }
            for (String period : periods) {
                JSONObject object = new JSONObject();
                BigDecimal value = new BigDecimal(0);
                requestBO.setFromPeriod(period);
                requestBO.setToPeriod(period);
                // 资产负债表
                List<JSONObject> list = new ArrayList<>();
                if (Integer.parseInt(startPeriod) <= Integer.parseInt(period)) {
                    // 本期科目余额
                    List<JSONObject> subjectBalance = certificateService.querySubjectBalance(AccountSet.getAccountSetId(), requestBO.getFromPeriod(), requestBO.getToPeriod());
                    Map<Long, JSONObject> subjectBalanceMap = subjectBalance.stream()
                            .collect(Collectors.toMap(o -> o.getLong("subjectId"), Function.identity()));
                    if (ObjectUtil.equal(1, indicator.getType())) {
                        List<JSONObject> balanceSheetReports = balanceSheetReportService.report(requestBO);
                        for (FinanceFormulaBO formulaBO : formulaBOS) {
                            if (ObjectUtil.isNotNull(formulaBO.getLine())) {
                                JSONObject jsonObject = balanceSheetReportService.filterReport(balanceSheetReports, formulaBO.getLine());
                                value = value.add(jsonObject.getBigDecimal("endPeriod"));
                            } else {
                                CashFlowStatementReportHolder.setSubjects(subjects);
                                List<FinanceSubject> subjectList = new ArrayList<>();
                                String number = formulaBO.getSubjectNumber();
                                FinanceSubject subject = CashFlowStatementReportHolder.getSubjectByNumber(number);
                                CashFlowStatementReportHolder.getNextSubjectIds(subjectList, subject.getSubjectId());
                                for (FinanceSubject financeSubject : subjectList) {
                                    if (subjectList.size() > 1 && subject.getSubjectId().equals(financeSubject.getSubjectId())) {
                                        continue;
                                    }
                                    JSONObject sub = new JSONObject();
                                    formulaBO.setSubjectId(Optional.ofNullable(financeSubject.getSubjectId())
                                            .map(i -> String.valueOf(i)).orElse(""));
                                    BigDecimal endValue = balanceSheetReportService.calculateEndValue(formulaBO, subjectBalanceMap);
                                    value = value.add(endValue);
                                    sub.put("value", endValue);
                                    sub.put("subjectNumber", financeSubject.getNumber());
                                    sub.put("subjectName", financeSubject.getSubjectName());
                                    list.add(sub);
                                }
                            }
                        }
                    } else if (ObjectUtil.equal(2, indicator.getType())) {
                        // 利润表
                        for (FinanceFormulaBO formulaBO : formulaBOS) {
                            if (ObjectUtil.isNotNull(formulaBO.getLine())) {
                                List<JSONObject> incomeStatementReports = incomeStatementReportService.report(requestBO);
                                JSONObject jsonObject = incomeStatementReportService.filterReport(incomeStatementReports, formulaBO.getLine());
                                value = value.add(jsonObject.getBigDecimal("monthValue"));
                            } else {
                                value = value.add(cashFlowStatementExtendDataService.calculateCurrentValue(formulaBO, subjectBalanceMap));
                            }
                        }
                    }
                }

                object.put("value", value);
                object.put("period", period);
                object.put("list", list);
                result.add(object);
            }
            return result;
        } finally {
            CashFlowStatementReportHolder.removeSubjects();
        }
    }

    @Override
    public List<Map<String, Object>> getAllFieldLanguageRel() {
        List<Map<String, Object>> listMap = new ArrayList<>();
        //配置项
        List<FinanceIndicator> indicators = list();
        if (CollectionUtil.isNotEmpty(indicators)) {
            for (FinanceIndicator indicator : indicators) {
                Map<String, Object> map = new HashMap<>();
                map.put("fileName", "finance.indicator." + indicator.getName());
                map.put("chinese", indicator.getName());
                map.put("translateName", "");
                listMap.add(map);
            }

        }

        //记账
        List<FinanceVoucher> financeVouchers = financeVoucherServiceImpl.list();
        if (CollectionUtil.isNotEmpty(financeVouchers)) {
            for (FinanceVoucher vouchers : financeVouchers) {
                Map<String, Object> map = new HashMap<>();
                map.put("fileName", "finance.voucher." + vouchers.getVoucherName());
                map.put("chinese", vouchers.getVoucherName());
                map.put("translateName", "");
                listMap.add(map);
            }

        }
        //资产负债表
        List<FinanceIncomeStatementConfig> incomeConfigs = financeIncomeStatementConfigService.list();
        if (CollectionUtil.isNotEmpty(incomeConfigs)) {
            for (FinanceIncomeStatementConfig incomeConfig : incomeConfigs) {
                Map<String, Object> statementReportMap = new HashMap<>();
                statementReportMap.put("fileName", "finance.config.statement.income." + incomeConfig.getName());
                statementReportMap.put("chinese", incomeConfig.getName());
                statementReportMap.put("translateName", "");
                listMap.add(statementReportMap);
            }

        }
        //利润表
        List<FinanceBalanceSheetConfig> balanceSheetConfigs = financeBalanceSheetConfigService.list();
        if (CollectionUtil.isNotEmpty(balanceSheetConfigs)) {
            for (FinanceBalanceSheetConfig balanceSheetConfig : balanceSheetConfigs) {
                Map<String, Object> statementReportMap = new HashMap<>();
                statementReportMap.put("fileName", "finance.config.statement.balance." + balanceSheetConfig.getName());
                statementReportMap.put("chinese", balanceSheetConfig.getName());
                statementReportMap.put("translateName", "");
                listMap.add(statementReportMap);
            }

        }
        //现金流量表
        List<FinanceCashFlowStatementConfig> statementConfigs = financeCashFlowStatementConfigService.list();
        if (CollectionUtil.isNotEmpty(statementConfigs)) {
            for (FinanceCashFlowStatementConfig statementConfig : statementConfigs) {
                Map<String, Object> statementReportMap = new HashMap<>();
                statementReportMap.put("fileName", "finance.config.statement." + statementConfig.getName());
                statementReportMap.put("chinese", statementConfig.getName());
                statementReportMap.put("translateName", "");
                listMap.add(statementReportMap);
            }

        }
        //模板
        List<FinanceStatementTemplate> statementTemplates = financeStatementTemplateService.list();
        if (CollectionUtil.isNotEmpty(statementTemplates)) {
            for (FinanceStatementTemplate statementTemplate : statementTemplates) {
                Map<String, Object> statementReportMap = new HashMap<>();
                statementReportMap.put("fileName", "finance.config.statement.template." + statementTemplate.getDigestContent());
                statementReportMap.put("chinese", statementTemplate.getDigestContent());
                statementReportMap.put("translateName", "");
                listMap.add(statementReportMap);
            }

        }
        //模板
        List<FinanceStatement> statements = financeStatementService.lambdaQuery().groupBy(FinanceStatement::getStatementName).list();
        if (CollectionUtil.isNotEmpty(statements)) {
            for (FinanceStatement statement : statements) {
                Map<String, Object> statementReportMap = new HashMap<>();
                statementReportMap.put("fileName", "finance.statement." + statement.getStatementName());
                statementReportMap.put("chinese", statement.getStatementName());
                statementReportMap.put("translateName", "");
                listMap.add(statementReportMap);
            }

        }
        //科目
        List<FinanceSubject> financeSubjects = financeSubjectServiceImpl.list();
        if (CollectionUtil.isNotEmpty(financeSubjects)) {
            for (FinanceSubject financeSubject : financeSubjects) {
                Map<String, Object> statementReportMap = new HashMap<>();
                statementReportMap.put("fileName", "finance.subject." + financeSubject.getSubjectName());
                statementReportMap.put("chinese", financeSubject.getSubjectName());
                statementReportMap.put("translateName", "");
                listMap.add(statementReportMap);
                //单位
                if (ObjectUtil.isNotEmpty(financeSubject.getAmountUnit())) {
                    Map<String, Object> amountUnitMap = new HashMap<>();
                    amountUnitMap.put("fileName", "finance.subject.amountUnit." + financeSubject.getAmountUnit());
                    amountUnitMap.put("chinese", financeSubject.getAmountUnit());
                    amountUnitMap.put("translateName", "");
                    listMap.add(amountUnitMap);
                }

            }

        }
        //科目
        List<FinanceStatementSubject> statementSubjects = financeStatementSubjectServiceImpl.lambdaQuery().groupBy(FinanceStatementSubject::getDigestContent).list();
        if (CollectionUtil.isNotEmpty(statementSubjects)) {
            for (FinanceStatementSubject statementSubject : statementSubjects) {
                Map<String, Object> statementReportMap = new HashMap<>();
                statementReportMap.put("fileName", "finance.statement.subject." + statementSubject.getDigestContent());
                statementReportMap.put("chinese", statementSubject.getDigestContent());
                statementReportMap.put("translateName", "");
                listMap.add(statementReportMap);
            }

        }
        List<FinanceAdjuvant> financeAdjuvants = financeAdjuvantServiceImpl.list();
        if (CollectionUtil.isNotEmpty(financeAdjuvants)) {
            for (FinanceAdjuvant financeAdjuvant : financeAdjuvants) {
                Map<String, Object> statementReportMap = new HashMap<>();
                statementReportMap.put("fileName", "finance.adjuvant." + financeAdjuvant.getAdjuvantName());
                statementReportMap.put("chinese", financeAdjuvant.getAdjuvantName());
                statementReportMap.put("translateName", "");
                listMap.add(statementReportMap);
            }

        }
        //错误信息
        for (FinanceCodeEnum codeEnum : FinanceCodeEnum.values()) {
            //添加语言包key
            Map<String, Object> map = new HashMap<>();
            map.put("fileName", "error." + codeEnum.getCode());
            map.put("chinese", AdminLanguageUtil.repalceMsg(codeEnum.getMsg()));
            map.put("translateName", "");
            listMap.add(map);
        }
        return listMap;
    }
}
