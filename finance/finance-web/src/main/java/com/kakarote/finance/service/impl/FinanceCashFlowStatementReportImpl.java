package com.kakarote.finance.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.googlecode.aviator.AviatorEvaluator;
import com.kakarote.common.utils.UserUtil;
import com.kakarote.core.servlet.BaseServiceImpl;
import com.kakarote.core.utils.ExcelParseUtil;
import com.kakarote.core.utils.SeparatorUtil;
import com.kakarote.finance.common.AccountSet;
import com.kakarote.finance.common.CashFlowStatementReportHolder;
import com.kakarote.finance.common.FinanceExcelParseUtil;
import com.kakarote.finance.entity.BO.FinanceCashFlowStatementUpdateBO;
import com.kakarote.finance.entity.BO.FinanceDetailAccountBO;
import com.kakarote.finance.entity.BO.FinanceReportRequestBO;
import com.kakarote.finance.entity.PO.FinanceAccountSet;
import com.kakarote.finance.entity.PO.FinanceCashFlowStatementConfig;
import com.kakarote.finance.entity.PO.FinanceCashFlowStatementReport;
import com.kakarote.finance.mapper.FinanceCashFlowStatementReportMapper;
import com.kakarote.finance.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 10323
 * @description: 现金流量表数据
 * @date 2021/8/2918:31
 */
@Service
public class FinanceCashFlowStatementReportImpl extends BaseServiceImpl<FinanceCashFlowStatementReportMapper, FinanceCashFlowStatementReport>
        implements IFinanceCashFlowStatementReportService {

    @Autowired
    private IFinanceCashFlowStatementConfigService configService;

    @Autowired
    private IFinanceIncomeStatementReportService incomeStatementService;

    @Autowired
    private IFinanceBalanceSheetReportService balanceSheetService;

    @Autowired
    private FinanceCashFlowStatementExtendDataServiceImpl extendDataService;

    @Autowired
    IFinanceCurrencyService currencyService;

    @Autowired
    private IFinanceCertificateService certificateService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<JSONObject> report(FinanceReportRequestBO requestBO) {
        List<JSONObject> report = new ArrayList<>();
        List<FinanceCashFlowStatementReport> flowStatementReports = lambdaQuery()
                .eq(FinanceCashFlowStatementReport::getAccountId, AccountSet.getAccountSetId())
                .eq(FinanceCashFlowStatementReport::getType, requestBO.getType())
                .eq(FinanceCashFlowStatementReport::getFromPeriod, requestBO.getFromPeriod())
                .eq(FinanceCashFlowStatementReport::getToPeriod, requestBO.getToPeriod())
                .orderByAsc(FinanceCashFlowStatementReport::getSortIndex)
                .list();
        if (CollUtil.isEmpty(flowStatementReports)) {
            // 获取现金流量表配置
            List<FinanceCashFlowStatementConfig> configs = configService.lambdaQuery()
                    .eq(FinanceCashFlowStatementConfig::getAccountId, AccountSet.getAccountSetId())
                    .orderByAsc(FinanceCashFlowStatementConfig::getSortIndex)
                    .list();
            configs.forEach(c -> {
                FinanceCashFlowStatementReport r = BeanUtil.copyProperties(c, FinanceCashFlowStatementReport.class);
                r.setId(null);
                r.setCreateTime(LocalDateTime.now());
                r.setType(requestBO.getType());
                r.setFromPeriod(requestBO.getFromPeriod());
                r.setToPeriod(requestBO.getToPeriod());
                save(r);
                flowStatementReports.add(r);
            });
        }
        if (CollUtil.isEmpty(flowStatementReports)) {
            return report;
        }

        try {
            for (FinanceCashFlowStatementReport config : flowStatementReports) {
                // 公式
                List<String> formulas = JSON.parseArray(config.getFormula(), String.class);
                requestBO.setCategory(config.getCategory());
                if (CollUtil.isNotEmpty(formulas)) {
                    this.calculate(report, requestBO, config, false);
                }
                Map<String, String> keyMap = new HashMap<>();
                keyMap.put("name_resourceKey", "finance.config.statement." + config.getName());
                config.setLanguageKeyMap(keyMap);
                JSONObject jsonObject = JSON.parseObject(JSON.toJSONStringWithDateFormat(config, DatePattern.NORM_DATETIME_PATTERN, SerializerFeature.WriteDateUseDateFormat));
                report.add(jsonObject);
            }
            JSONObject l22 = this.filterReport(report, 22);
            List<JSONObject> balanceReports = CashFlowStatementReportHolder.getBalanceSheet();
            JSONObject balanceSort1 = balanceSheetService.filterReport(balanceReports, 1);
            String yearValue = "yearValue";
            String endPeriod = "endPeriod";
            if (l22.getBigDecimal(yearValue).compareTo(balanceSort1.getBigDecimal(endPeriod)) != 0) {
                JSONObject l6 = this.filterReport(report, 6);
                BigDecimal l6YearPeriod = l6.getBigDecimal("yearValue");
                BigDecimal l6MonthValue = l6.getBigDecimal("monthValue");
                l6YearPeriod = l6YearPeriod.add(l22.getBigDecimal("yearValue")).subtract(balanceSort1.getBigDecimal("endPeriod"));
                l6MonthValue = l6MonthValue.add(l22.getBigDecimal("monthValue")).subtract(balanceSort1.getBigDecimal("endPeriod"));
                if (l6YearPeriod.compareTo(BigDecimal.ZERO) > 0) {
                    l6.put("yearValue", l6YearPeriod);
                }
                if (l6MonthValue.compareTo(BigDecimal.ZERO) > 0) {
                    l6.put("monthValue", l6MonthValue);
                }
                this.recalculate(report, requestBO, 6);
            }
            return report;
        } finally {
            CashFlowStatementReportHolder.remove();
        }
    }

    private void recalculate(List<JSONObject> report, FinanceReportRequestBO requestBO, int sort) {
        String line = "L" + sort;
        for (JSONObject r : report) {
            FinanceCashFlowStatementReport config = JSON.parseObject(r.toJSONString(), FinanceCashFlowStatementReport.class);
            if (config.getFormula().contains(line)) {
                this.calculate(report, requestBO, config, true);
                r.put("monthValue", config.getMonthValue());
                r.put("yearValue", config.getYearValue());
                recalculate(report, requestBO, config.getSort());
            }
        }
    }

    private void calculate(List<JSONObject> report, FinanceReportRequestBO requestBO, FinanceCashFlowStatementReport config, Boolean isRecalculate) {
        BigDecimal currentPeriod = BigDecimal.ZERO;
        BigDecimal yearPeriod = BigDecimal.ZERO;
        if (isRecalculate) {
            currentPeriod = config.getMonthValue();
            yearPeriod = config.getYearValue();
        }
        String formula = JSON.parseArray(config.getFormula(), String.class).get(0);
        String L = "L";
        if (!formula.contains(L)) {
            List<String> params = SeparatorUtil.parseParams(formula);
            Map<String, Object> monthEnv = new HashMap<>();
            Map<String, Object> yearEnv = new HashMap<>();
            for (String p : params) {
                requestBO.setExpression(p);
                // 利润表
                if (p.contains("IN")) {
                    JSONObject valueObject = incomeStatementService.filterByExpression(requestBO);
                    monthEnv.put(p, valueObject.getBigDecimal("monthValue"));
                    yearEnv.put(p, valueObject.getBigDecimal("yearValue"));
                }
                // 资产负债表
                else if (p.contains("BA")) {
                    String sort = SeparatorUtil.parseStrBetweenBracket(p).get(0).split(",")[0];
                    // 本期
                    JSONObject valueObject = balanceSheetService.filterByExpression(requestBO);
                    // 上期
                    FinanceReportRequestBO lastRequestBO = BeanUtil.copyProperties(requestBO,
                            FinanceReportRequestBO.class);
                    lastRequestBO.setQueryLastPeriod(true);
                    JSONObject lastValueObject = balanceSheetService.filterByExpression(lastRequestBO);
                    String[] strs = SeparatorUtil.parseStrBetweenBracket(p).get(0).split(",");
                    p = p.replaceAll("\\[|\\,|\\]", "");
                    if (ObjectUtil.equal(Integer.valueOf(sort), valueObject.getInteger("sort"))) {
                        // 期初
                        if (StrUtil.equals("1", strs[1])) {
                            // 本月： 本月期末 - 上月期末(如果当前月为初始月，则上月期末为年初数)
                            if (ObjectUtil.isNull(lastValueObject)) {
                                monthEnv.put(p, valueObject.getBigDecimal("startPeriod"));
                            } else {
                                monthEnv.put(p, lastValueObject.getBigDecimal("endPeriod"));
                            }
                            // 本年： 期末 - 年初
                            yearEnv.put(p, valueObject.getBigDecimal("initialPeriod"));
                        } else { // 期末
                            // 本月： 本月： 本月期末 - 上月期末
                            monthEnv.put(p, valueObject.getBigDecimal("endPeriod"));
                            // 本年： 期末 - 年初
                            yearEnv.put(p, valueObject.getBigDecimal("endPeriod"));
                        }
                    }
                }
                // 扩展表
                else if (p.contains("EX")) {
                    JSONObject valueObject = extendDataService.filterByExpression(requestBO);
                    monthEnv.put(p, valueObject.getBigDecimal("monthValue"));
                    yearEnv.put(p, valueObject.getBigDecimal("yearValue"));
                }
            }
            if (StrUtil.isNotEmpty(formula)) {
                formula = formula.replaceAll("\\[|\\,|\\]", "");
                if (ObjectUtil.isNotNull(currentPeriod) && currentPeriod.compareTo(BigDecimal.ZERO) == 0) {
                    currentPeriod = new BigDecimal(AviatorEvaluator.execute(formula, monthEnv).toString());
                }
                if (ObjectUtil.isNotNull(yearPeriod) && yearPeriod.compareTo(BigDecimal.ZERO) == 0) {
                    yearPeriod = new BigDecimal(AviatorEvaluator.execute(formula, yearEnv).toString());
                }
            }
        } else {
            Map<String, BigDecimal> result = this.calculateFixedFormula(formula, report);
            currentPeriod = result.get("monthValue");
            yearPeriod = result.get("yearValue");
        }
        config.setMonthValue(currentPeriod);
        config.setYearValue(yearPeriod);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateReports(FinanceCashFlowStatementUpdateBO updateBO) {
        if (CollUtil.isEmpty(updateBO.getDataList())) {
            return;
        }
        lambdaUpdate()
                .eq(FinanceCashFlowStatementReport::getAccountId, AccountSet.getAccountSetId())
                .eq(FinanceCashFlowStatementReport::getType, updateBO.getType())
                .eq(FinanceCashFlowStatementReport::getFromPeriod, updateBO.getFromPeriod())
                .eq(FinanceCashFlowStatementReport::getToPeriod, updateBO.getToPeriod())
                .remove();
        for (FinanceCashFlowStatementReport data : updateBO.getDataList()) {
            data.setId(null);
            data.setCreateUserId(UserUtil.getUserId());
            data.setCreateTime(LocalDateTime.now());
            data.setAccountId(AccountSet.getAccountSetId());
            data.setFromPeriod(updateBO.getFromPeriod());
            data.setToPeriod(updateBO.getToPeriod());
            data.setType(updateBO.getType());
            save(data);
        }
    }

    private Map<String, BigDecimal> calculateFixedFormula(String formula, List<JSONObject> reports) {
        Map<String, BigDecimal> result = new HashMap<>();
        BigDecimal currentPeriod = new BigDecimal(0);
        BigDecimal yearPeriod = new BigDecimal(0);
        String L = "L";
        if (formula.contains(L)) {
            String[] formulas = formula.split(SeparatorUtil.REGEX_OPERATOR);
            Map<String, Object> monthEnv = new HashMap<>();
            Map<String, Object> yearEnv = new HashMap<>();
            for (String f : formulas) {
                Integer sort = Integer.valueOf(f.replaceAll("L", ""));
                JSONObject object = this.filterReport(reports, sort);
                monthEnv.put(f, object.getBigDecimal("monthValue"));
                yearEnv.put(f, object.getBigDecimal("yearValue"));
            }
            currentPeriod = new BigDecimal(AviatorEvaluator.execute(formula, monthEnv).toString());
            yearPeriod = new BigDecimal(AviatorEvaluator.execute(formula, yearEnv).toString());
        }
        result.put("monthValue", currentPeriod);
        result.put("yearValue", yearPeriod);
        return result;
    }

    @Override
    public JSONObject balanceCheck(FinanceReportRequestBO requestBO) {
        JSONObject balanceSheetBalanceResult = balanceSheetService.balanceCheck(requestBO);
        JSONObject object = new JSONObject();
        String notContains = "notContains";
        String settled = "settled";
        if (!balanceSheetBalanceResult.getJSONArray(notContains).isEmpty() || !balanceSheetBalanceResult.getBoolean(settled)
                || !balanceSheetBalanceResult.getBoolean("balanced")) {
            object.put("balanceSheet", false);
        } else {
            object.put("balanceSheet", true);
        }
        return object;
    }

    /**
     * 导出现金流量表
     *
     * @param requestBO
     */
    @Override
    public void exportCashFlowStatementReport(FinanceReportRequestBO requestBO) {
        FinanceAccountSet accountSet = AccountSet.getAccountSet();
        //查询现金流量表
        List<JSONObject> mapList = report(requestBO);
        List<ExcelParseUtil.ExcelDataEntity> dataList = new ArrayList<>();
        dataList.add(ExcelParseUtil.toEntity("name", "项目"));
        dataList.add(ExcelParseUtil.toEntity("sort", "行次"));
        dataList.add(ExcelParseUtil.toEntity("yearValue", "本年累计金额"));
        dataList.add(ExcelParseUtil.toEntity("monthValue", "本月金额"));
        FinanceExcelParseUtil.exportExcelFinance(1, mapList, new FinanceExcelParseUtil.FinanceExcelParseService() {
            @Override
            public String getExcelName() {
                return "现金流量表";
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
