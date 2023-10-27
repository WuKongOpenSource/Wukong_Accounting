package com.kakarote.finance.controller;


import cn.hutool.core.collection.ListUtil;
import com.kakarote.common.log.annotation.OperateLog;
import com.kakarote.common.log.entity.OperationLog;
import com.kakarote.common.log.entity.OperationResult;
import com.kakarote.common.log.enums.ApplyEnum;
import com.kakarote.common.log.enums.BehaviorEnum;
import com.kakarote.common.log.enums.OperateObjectEnum;
import com.kakarote.core.common.Result;
import com.kakarote.finance.entity.PO.FinanceVoucher;
import com.kakarote.finance.service.IFinanceVoucherService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 凭证字 前端控制器
 * </p>
 *
 * @author zhangzhiwei
 * @since 2021-08-24
 */
@RestController
@RequestMapping("/financeVoucher")
@Api(tags = "凭证字模块")
public class FinanceVoucherController {

    @Autowired
    private IFinanceVoucherService financeVoucherService;

    @PostMapping("/add")
    @ApiOperation("保存数据")
    @OperateLog(apply = ApplyEnum.FS, behavior = BehaviorEnum.SAVE, object = OperateObjectEnum.FS_VOUCHER_WORD)
    public Result add(@RequestBody FinanceVoucher voucher) {
        OperationLog operationLog = financeVoucherService.saveAndUpdate(voucher);
        return OperationResult.ok(ListUtil.toList(operationLog));
    }

    @PostMapping("/update")
    @ApiOperation("编辑数据")
    @OperateLog(apply = ApplyEnum.FS, behavior = BehaviorEnum.UPDATE, object = OperateObjectEnum.FS_VOUCHER_WORD)
    public Result update(@RequestBody FinanceVoucher voucher) {
        OperationLog operationLog = financeVoucherService.saveAndUpdate(voucher);
        return OperationResult.ok(ListUtil.toList(operationLog));
    }

    @PostMapping("/queryList")
    @ApiOperation("列表查询凭证字")
    public Result<List<FinanceVoucher>> queryList() {
        List<FinanceVoucher> list = financeVoucherService.queryList();
        return Result.ok(list);
    }

    @PostMapping("/deleteById")
    @ApiOperation("删除")
    @OperateLog(apply = ApplyEnum.FS, behavior = BehaviorEnum.DELETE, object = OperateObjectEnum.FS_VOUCHER_WORD)
    public Result deleteById(@RequestParam("voucherId") Long voucherId) {
        OperationLog operationLog = financeVoucherService.deleteById(voucherId);
        return OperationResult.ok(ListUtil.toList(operationLog));
    }

    @PostMapping("/sort")
    @ApiOperation("排序")
    public Result sort(@RequestParam("voucherStartId") Long voucherStartId, @RequestParam("voucherEndId") Long voucherEndId) {
        financeVoucherService.sort(voucherStartId, voucherEndId);
        return Result.ok();
    }


}

