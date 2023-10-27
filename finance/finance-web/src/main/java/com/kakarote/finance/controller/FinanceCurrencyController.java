package com.kakarote.finance.controller;


import cn.hutool.core.collection.ListUtil;
import com.kakarote.common.log.annotation.OperateLog;
import com.kakarote.common.log.entity.OperationLog;
import com.kakarote.common.log.entity.OperationResult;
import com.kakarote.common.log.enums.ApplyEnum;
import com.kakarote.common.log.enums.BehaviorEnum;
import com.kakarote.common.log.enums.OperateObjectEnum;
import com.kakarote.core.common.Result;
import com.kakarote.finance.entity.PO.FinanceCurrency;
import com.kakarote.finance.service.IFinanceCurrencyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 币种 前端控制器
 * </p>
 *
 * @author zhangzhiwei
 * @since 2021-08-24
 */
@RestController
@RequestMapping("/financeCurrency")
@Api(tags = "币种模块")
public class FinanceCurrencyController {

    @Autowired
    private IFinanceCurrencyService financeCurrencyService;

    @PostMapping("/add")
    @ApiOperation("保存数据")
    @OperateLog(apply = ApplyEnum.FS, behavior = BehaviorEnum.SAVE, object = OperateObjectEnum.FS_CURRENCY)
    public Result add(@RequestBody FinanceCurrency currency) {
        OperationLog operationLog = financeCurrencyService.saveAndUpdate(currency);
        return OperationResult.ok(ListUtil.toList(operationLog));
    }

    @PostMapping("/update")
    @ApiOperation("编辑数据")
    @OperateLog(apply = ApplyEnum.FS, behavior = BehaviorEnum.UPDATE, object = OperateObjectEnum.FS_CURRENCY)
    public Result update(@RequestBody FinanceCurrency currency) {
        OperationLog operationLog = financeCurrencyService.saveAndUpdate(currency);
        return OperationResult.ok(ListUtil.toList(operationLog));
    }

    @PostMapping("/deleteById")
    @ApiOperation("删除")
    @OperateLog(apply = ApplyEnum.FS, behavior = BehaviorEnum.DELETE, object = OperateObjectEnum.FS_CURRENCY)
    public Result deleteById(@RequestParam("currencyId") Long currencyId) {
        OperationLog operationLog = financeCurrencyService.deleteById(currencyId);
        return OperationResult.ok(ListUtil.toList(operationLog));
    }

    @PostMapping("/queryAllList")
    @ApiOperation("列表查询币种")
    public Result<List<FinanceCurrency>> queryAllList() {
        List<FinanceCurrency> list = financeCurrencyService.queryList();
        return Result.ok(list);
    }

    @PostMapping("/queryListByAccountId")
    @ApiOperation("列表查询币种根据账套id")
    public Result<List<FinanceCurrency>> queryListByAccountId(@RequestParam("accountId") Long accountId) {
        List<FinanceCurrency> list = financeCurrencyService.queryListByAccountId(accountId);
        return Result.ok(list);
    }

}

