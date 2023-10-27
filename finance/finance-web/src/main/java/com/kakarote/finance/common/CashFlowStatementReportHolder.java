package com.kakarote.finance.common;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.kakarote.core.servlet.ApplicationContextHolder;
import com.kakarote.finance.entity.BO.FinanceDetailAccountBO;
import com.kakarote.finance.entity.PO.FinanceCashFlowStatementExtendData;
import com.kakarote.finance.entity.PO.FinanceSubject;
import com.kakarote.finance.service.IFinanceCertificateService;
import com.kakarote.finance.service.IFinanceSubjectService;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @description: 现金流量表临时数据
 * @author: zjj
 * @date: 2021-09-01 13:37
 */
public class CashFlowStatementReportHolder {
    /**
     * 科目
     */
    private final static ThreadLocal<List<FinanceSubject>> subjects = new ThreadLocal<>();

    /**
     * 利润表
     */
    private final static ThreadLocal<List<JSONObject>> incomeStatement = new ThreadLocal<>();
    /**
     * 资产负债表
     */
    private final static ThreadLocal<List<JSONObject>> balanceSheet = new ThreadLocal<>();
    /**
     * 资产负债表-上期
     */
    private final static ThreadLocal<List<JSONObject>> lastBalanceSheet = new ThreadLocal<>();
    /**
     * 扩展表
     */
    private final static ThreadLocal<List<FinanceCashFlowStatementExtendData>> extendDatas = new ThreadLocal<>();

    /**
     * 科目余额表
     */
    private final static ThreadLocal<List<JSONObject>> detailBalanceSheet = new ThreadLocal<>();


    public static void setSubjects(List<FinanceSubject> subjectList) {
        subjects.set(subjectList);
    }

    public static void setDetailBalance(List<JSONObject> detailBalanceList) {
        detailBalanceSheet.set(detailBalanceList);
    }

    public static FinanceSubject getSubjectByNumber(String number) {
        List<FinanceSubject> subjects = getSubjects();
        return subjects.stream().filter(s -> StrUtil.equals(number, s.getNumber())).findFirst().orElse(null);
    }

    public static List<FinanceSubject> getSubjects() {
        List<FinanceSubject> subjectList = subjects.get();
        if (CollUtil.isEmpty(subjectList)) {
            subjectList = ApplicationContextHolder.getBean(IFinanceSubjectService.class).queryAll();
            setSubjects(subjectList);
        }
        return subjectList;
    }

    public static List<JSONObject> getDetailBalance(String certificateTime) {
        if (CollUtil.isEmpty(detailBalanceSheet.get())) {
            FinanceDetailAccountBO accountBO = new FinanceDetailAccountBO();
            Date date = DateUtil.parse(certificateTime, "yyyyMMdd");
            accountBO.setStartTime(DateUtil.format(date, "yyyyMM"));
            accountBO.setEndTime(DateUtil.format(date, "yyyyMM"));
            accountBO.setAccountId(AccountSet.getAccountSetId());
            accountBO.setMinLevel(1);
            accountBO.setMaxLevel(9);
            accountBO.setIsSubject(1);
            List<JSONObject> subjects = ApplicationContextHolder.getBean(IFinanceCertificateService.class).queryOldDetailBalanceAccount(accountBO);
            setDetailBalance(subjects);
        }
        return detailBalanceSheet.get();
    }

    public static JSONObject getDetailBalance(Long subjectId, String certificateTime) {
        return getDetailBalance(certificateTime).stream().filter(s -> ObjectUtil.equal(subjectId, s.getLong("subjectId"))).findFirst().orElse(null);
    }


    public static FinanceSubject getSubjects(Long subjectId) {
        return getSubjects().stream().filter(s -> ObjectUtil.equal(subjectId, s.getSubjectId())).findFirst().orElse(null);
    }

    public static List<Long> getChildSubjectIds(List<Long> childSubjectIds, Long subjectId) {
        getSubjects().forEach(s -> {
            if (ObjectUtil.equal(subjectId, s.getParentId())) {
                childSubjectIds.add(s.getSubjectId());
                getChildSubjectIds(childSubjectIds, s.getSubjectId());
            }
        });
        return childSubjectIds;
    }

    public static List<Long> getFirstChildSubjectIds(Long subjectId) {
        return getSubjects().stream().filter(s -> ObjectUtil.equal(subjectId, s.getParentId())).map(FinanceSubject::getSubjectId).collect(Collectors.toList());
    }

    public static List<FinanceSubject> getNextSubjectIds(List<FinanceSubject> childSubjectIds, Long subjectId) {
        FinanceSubject subject = getSubjects(subjectId);
        getSubjects().forEach(s -> {
            if (ObjectUtil.equal(subjectId, s.getParentId()) && !childSubjectIds.contains(s)) {
                childSubjectIds.add(s);
            } else if (!childSubjectIds.contains(subject)) {
                childSubjectIds.add(subject);
            }
        });
        return childSubjectIds;
    }


    public static void setIncomeStatement(List<JSONObject> jsonObjects) {
        incomeStatement.set(jsonObjects);
    }

    public static List<JSONObject> getIncomeStatement() {
        return incomeStatement.get();
    }

    public static void setBalanceSheet(List<JSONObject> jsonObjects) {
        balanceSheet.set(jsonObjects);
    }

    public static List<JSONObject> getBalanceSheet() {
        return balanceSheet.get();
    }

    public static void setLastBalanceSheet(List<JSONObject> jsonObjects) {
        lastBalanceSheet.set(jsonObjects);
    }

    public static List<JSONObject> getLastBalanceSheet() {
        return lastBalanceSheet.get();
    }

    public static void setExtendData(List<FinanceCashFlowStatementExtendData> extendData) {
        extendDatas.set(extendData);
    }

    public static List<FinanceCashFlowStatementExtendData> getExtendData() {
        return extendDatas.get();
    }

    public static void remove() {
        subjects.remove();
        incomeStatement.remove();
        balanceSheet.remove();
        lastBalanceSheet.remove();
        extendDatas.remove();
        detailBalanceSheet.remove();
    }

    public static void removeSubjects() {
        subjects.remove();
    }


    public static void removeDetailBalanceSheet() {
        detailBalanceSheet.remove();
    }
}
