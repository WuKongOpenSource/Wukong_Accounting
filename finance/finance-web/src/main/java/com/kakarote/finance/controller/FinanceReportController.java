package com.kakarote.finance.controller;

import cn.hutool.core.collection.ListUtil;
import com.alibaba.fastjson.JSONObject;
import com.kakarote.common.log.annotation.OperateLog;
import com.kakarote.common.log.entity.OperationLog;
import com.kakarote.common.log.entity.OperationResult;
import com.kakarote.common.log.enums.ApplyEnum;
import com.kakarote.common.log.enums.BehaviorEnum;
import com.kakarote.common.log.enums.OperateObjectEnum;
import com.kakarote.common.log.enums.OperateTypeEnum;
import com.kakarote.core.common.Result;
import com.kakarote.core.utils.BaseUtil;
import com.kakarote.finance.entity.BO.FinanceCashFlowStatementExtendDataUpdateBO;
import com.kakarote.finance.entity.BO.FinanceCashFlowStatementUpdateBO;
import com.kakarote.finance.entity.BO.FinanceReportRequestBO;
import com.kakarote.finance.entity.BO.FinanceUpdateReportConfigBO;
import com.kakarote.finance.entity.PO.FinanceCashFlowStatementExtendData;
import com.kakarote.finance.service.IFinanceBalanceSheetReportService;
import com.kakarote.finance.service.IFinanceCashFlowStatementExtendDataService;
import com.kakarote.finance.service.IFinanceCashFlowStatementReportService;
import com.kakarote.finance.service.IFinanceIncomeStatementReportService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author 10323
 * @description: 报表
 * @date 2021/8/2811:50
 */
@RestController
@RequestMapping("/financeReport")
@Api(tags = "报表模块")
public class FinanceReportController {

    @Autowired
    private IFinanceIncomeStatementReportService incomeStatementService;

    @Autowired
    private IFinanceBalanceSheetReportService balanceSheetService;

    @Autowired
    private IFinanceCashFlowStatementReportService statementReportService;

    @Autowired
    private IFinanceCashFlowStatementExtendDataService extendDataService;

    @PostMapping("/incomeStatementReport")
    @ApiOperation("利润表")
    public Result<List<JSONObject>> incomeStatementReport(@RequestBody FinanceReportRequestBO requestBO) {
        List<JSONObject> result = incomeStatementService.report(requestBO);
        return Result.ok(result);
    }

    @PostMapping("/incomeStatementReport/balanceCheck")
    @ApiOperation("利润表平衡检查")
    public Result<JSONObject> incomeStatementReportBalanceCheck(@RequestBody FinanceReportRequestBO requestBO) {
        JSONObject result = incomeStatementService.balanceCheck(requestBO);
        return Result.ok(result);
    }

    @PostMapping("/balanceSheetReport")
    @ApiOperation("资产负债表")
    public Result<List<JSONObject>> balanceSheetReport(@RequestBody FinanceReportRequestBO requestBO) {
        List<JSONObject> result = balanceSheetService.report(requestBO);
        return Result.ok(result);
    }

    @PostMapping("/balanceSheetReport/balanceCheck")
    @ApiOperation("资产负债表平衡检查")
    public Result<JSONObject> balanceSheetReportBalanceCheck(@RequestBody FinanceReportRequestBO requestBO) {
        JSONObject result = balanceSheetService.balanceCheck(requestBO);
        return Result.ok(result);
    }

    @PostMapping("/cashFlowStatementReport")
    @ApiOperation("现金流量表")
    public Result<List<JSONObject>> cashFlowStatementReport(@RequestBody FinanceReportRequestBO requestBO) {
        List<JSONObject> result = statementReportService.report(requestBO);
        return Result.ok(result);
    }

    @PostMapping("/cashFlowStatementReport/balanceCheck")
    @ApiOperation("现金流量表平衡检查")
    public Result<JSONObject> cashFlowStatementReportBalanceCheck(@RequestBody FinanceReportRequestBO requestBO) {
        JSONObject result = statementReportService.balanceCheck(requestBO);
        return Result.ok(result);
    }

    @PostMapping("/cashFlowStatementReport/update")
    @ApiOperation("编辑现金流量表")
    @OperateLog(apply = ApplyEnum.FS, object = OperateObjectEnum.FS_CASH_FLOW_STATEMENT, behavior = BehaviorEnum.UPDATE)
    public Result cashFlowStatementReport(@RequestBody FinanceCashFlowStatementUpdateBO updateBO) {
        statementReportService.updateReports(updateBO);
        OperationLog operationLog = new OperationLog();
        operationLog.setOperationObject("财务现金流量表");
        operationLog.setOperationInfo("调整财务现金流量表");
        return OperationResult.ok(ListUtil.toList(operationLog));
    }

    @PostMapping("/cashFlowStatementExtend/list")
    @ApiOperation("查询扩展")
    public Result<List<FinanceCashFlowStatementExtendData>> queryCashFlowStatementExtend(@RequestBody FinanceReportRequestBO requestBO) {
        List<FinanceCashFlowStatementExtendData> result = extendDataService.queryList(requestBO);
        return Result.ok(result);
    }

    @PostMapping("/cashFlowStatementExtend/update")
    @ApiOperation("编辑扩展数据")
    public Result updateCashFlowStatementExtendConfig(@RequestBody FinanceCashFlowStatementExtendDataUpdateBO updateBO) {
        extendDataService.updateData(updateBO);
        return Result.ok();
    }

    @PostMapping("/incomeStatementConfig/update")
    @ApiOperation("编辑利润表公式")
    public Result updateIncomeStatementConfig(@RequestBody FinanceUpdateReportConfigBO updateBO) {
        incomeStatementService.updateIncomeStatementConfig(updateBO);
        return Result.ok();
    }

    @PostMapping("/balanceSheetConfig/update")
    @ApiOperation("编辑资产负债表公式")
    public Result updateBalanceSheetConfig(@RequestBody FinanceUpdateReportConfigBO updateBO) {
        balanceSheetService.updateBalanceSheetConfig(updateBO);
        return Result.ok();
    }

    @PostMapping("/cashFlowStatementExtendConfig/update")
    @ApiOperation("编辑扩展数据公式")
    public Result cashFlowStatementExtendConfig(@RequestBody FinanceUpdateReportConfigBO updateBO) {
        extendDataService.updateCashFlowStatementExtendConfig(updateBO);
        return Result.ok();
    }

    @PostMapping("/exportBalanceSheetReport")
    @ApiOperation("导出资产负债表")
    @OperateLog(apply = ApplyEnum.FS, object = OperateObjectEnum.FS_BALANCE_SHEET, type = OperateTypeEnum.EXPORT, behavior = BehaviorEnum.EXCEL_EXPORT)
    public void exportBalanceSheetReport(@RequestBody FinanceReportRequestBO requestBO) {
        balanceSheetService.exportBalanceSheetReport(requestBO);
        HttpServletResponse response = BaseUtil.getResponse();
        response.setHeader("exportSize", "-1");
    }

    @PostMapping("/exportIncomeStatementReport")
    @ApiOperation("导出利润表")
    @OperateLog(apply = ApplyEnum.FS, object = OperateObjectEnum.FS_PROFIT_STATEMENT, type = OperateTypeEnum.EXPORT, behavior = BehaviorEnum.EXCEL_EXPORT)
    public void exportIncomeStatementReport(@RequestBody FinanceReportRequestBO requestBO) {
        incomeStatementService.exportIncomeStatementReport(requestBO);
        HttpServletResponse response = BaseUtil.getResponse();
        response.setHeader("exportSize", "-1");
    }

    @PostMapping("/exportCashFlowStatementReport")
    @ApiOperation("导出现金流量表")
    @OperateLog(apply = ApplyEnum.FS, object = OperateObjectEnum.FS_CASH_FLOW_STATEMENT, type = OperateTypeEnum.EXPORT, behavior = BehaviorEnum.EXCEL_EXPORT)
    public void exportCashFlowStatementReport(@RequestBody FinanceReportRequestBO requestBO) {
        statementReportService.exportCashFlowStatementReport(requestBO);
        HttpServletResponse response = BaseUtil.getResponse();
        response.setHeader("exportSize", "-1");
    }

}
