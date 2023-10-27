package com.kakarote.finance.controller;


import cn.hutool.core.collection.ListUtil;
import com.kakarote.common.log.annotation.OperateLog;
import com.kakarote.common.log.entity.OperationLog;
import com.kakarote.common.log.entity.OperationResult;
import com.kakarote.common.log.enums.ApplyEnum;
import com.kakarote.common.log.enums.BehaviorEnum;
import com.kakarote.common.log.enums.OperateObjectEnum;
import com.kakarote.core.common.Result;
import com.kakarote.finance.entity.PO.FinanceAdjuvant;
import com.kakarote.finance.service.IFinanceAdjuvantService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 辅助核算表 前端控制器
 * </p>
 *
 * @author zhangzhiwei
 * @since 2021-08-24
 */
@RestController
@RequestMapping("/financeAdjuvant")
@Api(tags = "辅助核算")
public class FinanceAdjuvantController {

    @Autowired
    private IFinanceAdjuvantService financeAdjuvantService;

    @PostMapping("/add")
    @ApiOperation("保存数据")
    @OperateLog(apply = ApplyEnum.FS, behavior = BehaviorEnum.SAVE, object = OperateObjectEnum.FS_ADJUVANT_CARD)
    public Result add(@RequestBody FinanceAdjuvant adjuvant) {
        OperationLog operationLog = financeAdjuvantService.saveAndUpdate(adjuvant);
        return OperationResult.ok(ListUtil.toList(operationLog));
    }

    @PostMapping("/update")
    @ApiOperation("编辑数据")
    @OperateLog(apply = ApplyEnum.FS, behavior = BehaviorEnum.UPDATE, object = OperateObjectEnum.FS_ADJUVANT_CARD)
    public Result update(@RequestBody FinanceAdjuvant adjuvant) {
        OperationLog operationLog = financeAdjuvantService.saveAndUpdate(adjuvant);
        return OperationResult.ok(ListUtil.toList(operationLog));
    }

    @PostMapping("/deleteById")
    @ApiOperation("删除")
    @OperateLog(apply = ApplyEnum.FS, behavior = BehaviorEnum.DELETE, object = OperateObjectEnum.FS_ADJUVANT_CARD)
    public Result deleteById(@RequestParam("adjuvantId") Long adjuvantId) {
        OperationLog operationLog = financeAdjuvantService.deleteById(adjuvantId);
        return OperationResult.ok(ListUtil.toList(operationLog));
    }

    @PostMapping("/queryAllList")
    @ApiOperation("列表查询辅助核算")
    public Result<List<FinanceAdjuvant>> queryAllList() {
        List<FinanceAdjuvant> list = financeAdjuvantService.queryAllList();
        return Result.ok(list);
    }

    @PostMapping("/queryCustomList")
    @ApiOperation("查询所有自定义辅助核算")
    public Result<List<FinanceAdjuvant>> queryCustomList() {
        List<FinanceAdjuvant> list = financeAdjuvantService.queryCustomList();
        return Result.ok(list);
    }

}