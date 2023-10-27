package com.kakarote.finance.controller;

import com.kakarote.core.common.ParamAspect;
import com.kakarote.core.common.Result;
import com.kakarote.finance.service.IFinanceCommonService;
import com.kakarote.finance.service.IFinanceReportTemplateService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/financeCommon")
@Api(tags = "通用模块")
public class FinanceCommonController {

    @Autowired
    private IFinanceCommonService commonService;

    @Autowired
    private IFinanceReportTemplateService templateService;

    @PostMapping("/init")
    @ApiOperation("初始化数据")
    public Result init() throws IOException {
        commonService.init();
        return Result.ok();
    }

    @PostMapping("/import")
    @ApiOperation("导入模板")
    public Result importTemplate() throws IOException {
        templateService.importTemplate();
        return Result.ok();
    }

    @PostMapping("/initBalanceSheet")
    @ApiOperation("初始化资产负债表")
    @ParamAspect
    public Result initBalanceSheet() {
        commonService.initBalanceSheet();
        return Result.ok();
    }
}
