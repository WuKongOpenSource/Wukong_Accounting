package com.kakarote.finance.controller;

import com.alibaba.fastjson.JSONObject;
import com.kakarote.core.common.Result;
import com.kakarote.finance.entity.PO.FinanceDashboardConfig;
import com.kakarote.finance.entity.VO.FinanceIndicatorVO;
import com.kakarote.finance.service.IFinanceDashboardConfigService;
import com.kakarote.finance.service.IFinanceDashboardService;
import com.kakarote.finance.service.IFinanceIndicatorService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: zjj
 * @date: 2021-09-02 11:12
 */
@RestController
@RequestMapping("/financeDashboard")
@Api(tags = "仪表盘")
public class FinanceDashboardController {

    @Autowired
    private IFinanceDashboardService dashboardService;

    @Autowired
    private IFinanceDashboardConfigService configService;

    @Autowired
    private IFinanceIndicatorService indicatorService;

    @PostMapping("/incomeStatement")
    @ApiOperation("利润表统计")
    public Result<List<JSONObject>> incomeStatement(@RequestParam(value = "strDate", required = false) String strDate) {
        List<JSONObject> result = dashboardService.incomeStatement(strDate);
        return Result.ok(result);
    }


    @PostMapping("/config")
    @ApiOperation("查询仪表盘品配置")
    public Result<FinanceDashboardConfig> getDashboardConfig() {
        FinanceDashboardConfig result = configService.getDashboardConfig();
        return Result.ok(result);
    }


    @PostMapping("/updateConfig")
    @ApiOperation("修改仪表盘品配置")
    public Result updateConfig(@RequestBody FinanceDashboardConfig config) {
        configService.updateConfig(config);
        return Result.ok();
    }

    @PostMapping("/indicator")
    @ApiOperation("指标列表")
    public Result<List<FinanceIndicatorVO>> indicatorList() {
        //{
        //  "name_resourceKey": "finaceField.superiorCustomerName"
        //}languageKeyMap  利润总额
        List<FinanceIndicatorVO> result = indicatorService.indicatorList();
        return Result.ok(result);
    }

    @PostMapping("/statistics")
    @ApiOperation("指标统计")
    public Result<List<JSONObject>> indicatorList(@RequestParam(value = "id") Long id) {
        List<JSONObject> result = indicatorService.statistics(id);
        return Result.ok(result);
    }

    @ApiOperation("查询所有字段语言包key信息")
    @PostMapping(value = "/getAllFieldLanguageRel")
    public Result<List<Map<String, Object>>> getAllFieldLanguageRel() {
        return Result.ok(indicatorService.getAllFieldLanguageRel());
    }

}
