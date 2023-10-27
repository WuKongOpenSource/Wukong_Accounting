package com.kakarote.finance.controller;


import com.kakarote.core.common.Result;
import com.kakarote.finance.entity.PO.FinanceFlows;
import com.kakarote.finance.service.IFinanceFlowsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 现金流量初始余额 前端控制器
 * </p>
 *
 * @author zhangzhiwei
 * @since 2021-08-24
 */
@RestController
@RequestMapping("/financeFlows")
@Api(tags = "现金流量初始余额模块")
public class FinanceFlowsController {

    @Autowired
    private IFinanceFlowsService financeFlowsService;

    @PostMapping("/add")
    @ApiOperation("保存数据")
    public Result add(@RequestBody List<FinanceFlows> flows) {
        financeFlowsService.updateByIds(flows);
        return Result.ok();
    }

    @PostMapping("/queryAllList")
    @ApiOperation("查询现金流量初始余额")
    public Result<List<FinanceFlows>> queryAllList() {
        List<FinanceFlows> list = financeFlowsService.queryList();
        return Result.ok(list);
    }


}

