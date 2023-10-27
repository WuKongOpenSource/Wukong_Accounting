package com.kakarote.finance.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.googlecode.aviator.AviatorEvaluator;
import com.kakarote.core.servlet.BaseServiceImpl;
import com.kakarote.core.utils.SeparatorUtil;
import com.kakarote.finance.common.AccountSet;
import com.kakarote.finance.common.CashFlowStatementReportHolder;
import com.kakarote.finance.constant.FinanceBalanceDirectionEnum;
import com.kakarote.finance.constant.FinanceRuleEnum;
import com.kakarote.finance.entity.BO.FinanceCashFlowStatementExtendDataUpdateBO;
import com.kakarote.finance.entity.BO.FinanceFormulaBO;
import com.kakarote.finance.entity.BO.FinanceReportRequestBO;
import com.kakarote.finance.entity.BO.FinanceUpdateReportConfigBO;
import com.kakarote.finance.entity.PO.FinanceCashFlowStatementExtendConfig;
import com.kakarote.finance.entity.PO.FinanceCashFlowStatementExtendData;
import com.kakarote.finance.entity.PO.FinanceCashFlowStatementReport;
import com.kakarote.finance.entity.PO.FinanceSubject;
import com.kakarote.finance.mapper.FinanceCashFlowStatementExtendDataMapper;
import com.kakarote.finance.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: zjj
 * @date: 2021-08-30 09:32
 */
@Service
public class FinanceCashFlowStatementExtendDataServiceImpl extends BaseServiceImpl<FinanceCashFlowStatementExtendDataMapper, FinanceCashFlowStatementExtendData>
        implements IFinanceCashFlowStatementExtendDataService, IFinanceReportService {

    @Autowired
    private IFinanceCashFlowStatementExtendConfigService extendConfigService;

    @Autowired
    private IFinanceCashFlowStatementReportService statementReportService;

    @Autowired
    private IFinanceCertificateService certificateService;

    @Autowired
    private IFinanceSubjectService subjectService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<FinanceCashFlowStatementExtendData> queryList(FinanceReportRequestBO requestBO) {
        List<FinanceCashFlowStatementExtendData> dataList = lambdaQuery()
                .eq(FinanceCashFlowStatementExtendData::getAccountId, AccountSet.getAccountSetId())
                .eq(FinanceCashFlowStatementExtendData::getType, requestBO.getType())
                .eq(FinanceCashFlowStatementExtendData::getFromPeriod, requestBO.getFromPeriod())
                .eq(FinanceCashFlowStatementExtendData::getToPeriod, requestBO.getToPeriod())
                .eq(FinanceCashFlowStatementExtendData::getCategory, requestBO.getCategory())
                .orderByAsc(FinanceCashFlowStatementExtendData::getSortIndex).list();
        if (CollUtil.isEmpty(dataList)) {
            List<FinanceCashFlowStatementExtendConfig> configs = extendConfigService.lambdaQuery()
                    .eq(FinanceCashFlowStatementExtendConfig::getAccountId, AccountSet.getAccountSetId())
                    .eq(FinanceCashFlowStatementExtendConfig::getCategory, requestBO.getCategory())
                    .orderByAsc(FinanceCashFlowStatementExtendConfig::getSortIndex).list();
            configs.forEach(c -> {
                FinanceCashFlowStatementExtendData extendData = BeanUtil.copyProperties(c, FinanceCashFlowStatementExtendData.class);
                extendData.setId(null);
                extendData.setType(requestBO.getType());
                extendData.setFromPeriod(requestBO.getFromPeriod());
                extendData.setToPeriod(requestBO.getToPeriod());
                extendData.setAccountId(AccountSet.getAccountSetId());
                save(extendData);
                dataList.add(extendData);
            });
        }
        // 所有未删除的科目
        List<FinanceSubject> subjects = subjectService.lambdaQuery()
                .eq(FinanceSubject::getAccountId, AccountSet.getAccountSetId())
                .ne(FinanceSubject::getStatus, 3)
                .list();
        if (CollUtil.isEmpty(dataList)) {
            return dataList;
        }
        try {
            CashFlowStatementReportHolder.setSubjects(subjects);
            // 本期科目余额
            List<JSONObject> subjectBalance = certificateService.querySubjectBalance(AccountSet.getAccountSetId(), requestBO.getFromPeriod(), requestBO.getToPeriod());
            Map<Long, JSONObject> subjectBalanceMap = subjectBalance.stream()
                    .collect(Collectors.toMap(o -> o.getLong("subjectId"), Function.identity()));
            for (FinanceCashFlowStatementExtendData data : dataList) {
                if (!data.getEditable()) {
                    String formulaStr = JSON.parseArray(data.getFormula(), String.class).get(0);
                    formulaStr = SeparatorUtil.replaceChinese(formulaStr, "");
                    String[] formulas = formulaStr.split(SeparatorUtil.REGEX_OPERATOR);
                    Map<String, Object> monthEnv = new HashMap<>();
                    Map<String, Object> yearEnv = new HashMap<>();
                    for (String f : formulas) {
                        Integer sort = Integer.valueOf(f.replaceAll("L", ""));
                        FinanceCashFlowStatementExtendData c = filterData(dataList, sort);
                        monthEnv.put(f, c.getMonthValue());
                        yearEnv.put(f, c.getYearValue());
                    }
                    data.setMonthValue(new BigDecimal(AviatorEvaluator.execute(formulaStr, monthEnv).toString()));
                    data.setYearValue(new BigDecimal(AviatorEvaluator.execute(formulaStr, yearEnv).toString()));
                } else {
                    List<FinanceFormulaBO> formulaBOS = JSON.parseArray(data.getFormula(), FinanceFormulaBO.class);
                    BigDecimal monthValue = new BigDecimal(0);
                    BigDecimal yearValue = new BigDecimal(0);
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
                    data.setFormula(JSON.toJSONString(formulaBOS));
                    if (data.getMonthValue().compareTo(BigDecimal.ZERO) == 0) {
                        data.setMonthValue(monthValue);
                    }
                    if (data.getYearValue().compareTo(BigDecimal.ZERO) == 0) {
                        data.setYearValue(yearValue);
                    }
                }
            }
            return dataList;
        } finally {
            CashFlowStatementReportHolder.removeSubjects();
        }
    }

    @Override
    public void updateData(FinanceCashFlowStatementExtendDataUpdateBO updateBO) {
        for (FinanceCashFlowStatementExtendData data : updateBO.getDataList()) {
            FinanceCashFlowStatementExtendData source = getById(data.getId());
            // 不可编辑
            if (!source.getEditable()) {
                continue;
            }
            source.setMonthValue(data.getMonthValue());
            source.setYearValue(data.getYearValue());
            lambdaUpdate()
                    .set(FinanceCashFlowStatementExtendData::getMonthValue, data.getMonthValue())
                    .set(FinanceCashFlowStatementExtendData::getYearValue, data.getYearValue())
                    .eq(FinanceCashFlowStatementExtendData::getAccountId, AccountSet.getAccountSetId())
                    .eq(FinanceCashFlowStatementExtendData::getId, data.getId())
                    .update();
        }
        statementReportService.lambdaUpdate()
                .eq(FinanceCashFlowStatementReport::getAccountId, AccountSet.getAccountSetId())
                .eq(FinanceCashFlowStatementReport::getType, updateBO.getType())
                .eq(FinanceCashFlowStatementReport::getFromPeriod, updateBO.getFromPeriod())
                .eq(FinanceCashFlowStatementReport::getToPeriod, updateBO.getToPeriod())
                .remove();
    }


    @Override
    public void updateCashFlowStatementExtendConfig(FinanceUpdateReportConfigBO configBO) {
        FinanceCashFlowStatementExtendData config = getById(configBO.getId());
        config.setFormula(JSON.toJSONString(configBO.getFormulaBOList()));
        updateById(config);
    }

    @Override
    public JSONObject filterByExpression(FinanceReportRequestBO requestBO) {
        List<FinanceCashFlowStatementExtendData> extendDatas = CashFlowStatementReportHolder.getExtendData();
        if (ObjectUtil.isNull(extendDatas) || CollUtil.isEmpty(extendDatas)) {
            extendDatas = this.queryList(requestBO);
            CashFlowStatementReportHolder.setExtendData(extendDatas);
        }
        String expression = requestBO.getExpression();
        String sort = expression.replaceAll("[A-Z]{2}", "");
        FinanceCashFlowStatementExtendData extendData = filterData(extendDatas, Integer.valueOf(sort));
        return new JSONObject(BeanUtil.beanToMap(extendData));
    }

    @Override
    public BigDecimal calculateCurrentValue(FinanceFormulaBO formulaBO, Map<Long, JSONObject> subjectBalanceMap) {
        Long subjectId = StrUtil.isEmpty(formulaBO.getSubjectId()) ? null : Long.valueOf(formulaBO.getSubjectId());
        if (subjectId == null) {
            FinanceSubject subject = CashFlowStatementReportHolder.getSubjectByNumber(formulaBO.getSubjectNumber());
            if (subject != null) {
                subjectId = subject.getSubjectId();
            }
        }
        String operator = formulaBO.getOperator();
        FinanceRuleEnum ruleEnum = FinanceRuleEnum.parseType(formulaBO.getRules());
        List<Long> childSubjectIds = CashFlowStatementReportHolder.getFirstChildSubjectIds(subjectId);
        if (CollUtil.isEmpty(childSubjectIds)) {
            childSubjectIds.add(subjectId);
        }
        BigDecimal bigDecimal = new BigDecimal(0);
        // 父级科目
        FinanceSubject parentSubject = CashFlowStatementReportHolder.getSubjects(subjectId);
        if (ObjectUtil.isEmpty(parentSubject)) {
            return BigDecimal.ZERO;
        }
        for (Long childSubjectId : childSubjectIds) {
            JSONObject balanceObject = subjectBalanceMap.get(childSubjectId);
            if (ObjectUtil.isNotNull(balanceObject)) {
                BigDecimal debtorCurrentBalance = balanceObject.getBigDecimal("debtorCurrentBalance");
                BigDecimal creditCurrentBalance = balanceObject.getBigDecimal("creditCurrentBalance");
                // 贷方发生额
                if (ObjectUtil.equal(FinanceRuleEnum.CREDIT_AMOUNT, ruleEnum)) {
                    if (StrUtil.equals("+", operator)) {
                        bigDecimal = bigDecimal.add(creditCurrentBalance);
                    } else {
                        bigDecimal = bigDecimal.subtract(creditCurrentBalance);
                    }
                }
                // 借方发生额
                else if (ObjectUtil.equal(FinanceRuleEnum.DEBIT_AMOUNT, ruleEnum)) {
                    if (StrUtil.equals("+", operator)) {
                        bigDecimal = bigDecimal.add(debtorCurrentBalance);
                    } else {
                        bigDecimal = bigDecimal.subtract(debtorCurrentBalance);
                    }
                }
                // 损益发生额
                else if (ObjectUtil.equal(FinanceRuleEnum.PROFIT_LOSS_AMOUNT, ruleEnum)) {
                    BigDecimal value;
                    if (ObjectUtil.equal(FinanceBalanceDirectionEnum.LEND.getType(), parentSubject.getBalanceDirection())) {
                        value = debtorCurrentBalance.subtract(creditCurrentBalance);
                    } else {
                        value = creditCurrentBalance.subtract(debtorCurrentBalance);
                    }
                    if (StrUtil.equals("+", operator)) {
                        bigDecimal = bigDecimal.add(value);
                    } else {
                        bigDecimal = bigDecimal.subtract(value);
                    }
                }
            }
        }
        return bigDecimal;
    }

    private BigDecimal calculateYearValue(FinanceFormulaBO formulaBO, Map<Long, JSONObject> subjectBalanceMap) {
        Long subjectId = StrUtil.isEmpty(formulaBO.getSubjectId()) ? null : Long.valueOf(formulaBO.getSubjectId());
        String operator = formulaBO.getOperator();
        FinanceRuleEnum ruleEnum = FinanceRuleEnum.parseType(formulaBO.getRules());
        List<Long> childSubjectIds = CashFlowStatementReportHolder.getFirstChildSubjectIds(subjectId);
        if (CollUtil.isEmpty(childSubjectIds)) {
            childSubjectIds.add(subjectId);
        }
        BigDecimal bigDecimal = new BigDecimal(0);
        // 父级科目
        FinanceSubject parentSubject = CashFlowStatementReportHolder.getSubjects(subjectId);
        if (ObjectUtil.isEmpty(parentSubject)) {
            return BigDecimal.ZERO;
        }
        for (Long childSubjectId : childSubjectIds) {
            JSONObject balanceObject = subjectBalanceMap.get(childSubjectId);
            if (ObjectUtil.isNotNull(balanceObject)) {
                BigDecimal debtorYearBalance = balanceObject.getBigDecimal("debtorYearBalance");
                BigDecimal creditYearBalance = balanceObject.getBigDecimal("creditYearBalance");
                //年累计借方-初始
                BigDecimal initDebtorYearBalance = balanceObject.getBigDecimal("initDebtorYearBalance");
                //年累计贷方-初始
                BigDecimal initCreditYearBalance = balanceObject.getBigDecimal("initCreditYearBalance");
                // 贷方发生额
                if (ObjectUtil.equal(FinanceRuleEnum.CREDIT_AMOUNT, ruleEnum)) {
                    if (StrUtil.equals("+", operator)) {
                        bigDecimal = bigDecimal.add(creditYearBalance);
                    } else {
                        bigDecimal = bigDecimal.subtract(creditYearBalance);
                    }
                }
                // 借方发生额
                else if (ObjectUtil.equal(FinanceRuleEnum.DEBIT_AMOUNT, ruleEnum)) {
                    if (StrUtil.equals("+", operator)) {
                        bigDecimal = bigDecimal.add(debtorYearBalance);
                    } else {
                        bigDecimal = bigDecimal.subtract(debtorYearBalance);
                    }
                }
                // 损益发生额
                else if (ObjectUtil.equal(FinanceRuleEnum.PROFIT_LOSS_AMOUNT, ruleEnum)) {
                    BigDecimal value;
                    if (ObjectUtil.equal(FinanceBalanceDirectionEnum.LEND.getType(), parentSubject.getBalanceDirection())) {
                        value = debtorYearBalance.subtract(creditYearBalance).add(initDebtorYearBalance);
                    } else {
                        value = creditYearBalance.subtract(debtorYearBalance).add(initCreditYearBalance);
                    }
                    if (StrUtil.equals("+", operator)) {
                        bigDecimal = bigDecimal.add(value);
                    } else {
                        bigDecimal = bigDecimal.subtract(value);
                    }
                }
            }
        }
        return bigDecimal;
    }
}
