package com.kakarote.finance.controller;


import com.alibaba.fastjson.JSONObject;
import com.kakarote.common.log.annotation.OperateLog;
import com.kakarote.common.log.entity.OperationLog;
import com.kakarote.common.log.entity.OperationResult;
import com.kakarote.common.log.enums.ApplyEnum;
import com.kakarote.common.log.enums.BehaviorEnum;
import com.kakarote.common.log.enums.OperateObjectEnum;
import com.kakarote.core.common.Result;
import com.kakarote.core.entity.BasePage;
import com.kakarote.finance.entity.BO.FinanceAddInitialBO;
import com.kakarote.finance.entity.BO.FinanceInitialPageBO;
import com.kakarote.finance.entity.PO.FinanceInitial;
import com.kakarote.finance.entity.PO.FinanceInitialAssist;
import com.kakarote.finance.service.IFinanceInitialService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 财务初始余额 前端控制器
 * </p>
 *
 * @author zhangzhiwei
 * @since 2021-08-25
 */
@RestController
@RequestMapping("/financeInitial")
@Api(tags = "财务初始余额模块")
public class FinanceInitialController {

    @Autowired
    private IFinanceInitialService financeInitialService;

    @PostMapping("/add")
    @ApiOperation("添加辅助核算初始余额")
    public Result add(@RequestBody List<FinanceAddInitialBO> bos) {
        financeInitialService.addInitial(bos);
        return Result.ok();
    }

    @PostMapping("/update")
    @ApiOperation("更新期初余额")
    @OperateLog(apply = ApplyEnum.FS, behavior = BehaviorEnum.UPDATE, object = OperateObjectEnum.FS_INITIAL_BALANCE)
    public Result updateInitialValue(@RequestBody List<FinanceInitial> initials) {
        financeInitialService.updateInitialValue(initials);
        OperationLog operationLog = new OperationLog();
        operationLog.setOperationObject("财务初始余额");
        operationLog.setOperationInfo("编辑财务初始余额");
        return OperationResult.ok(operationLog);
    }

    @PostMapping("/saveAndUpdate")
    @ApiOperation("保存数据")
    public Result saveAndUpdate(@RequestBody FinanceInitial initial) {
//        financeInitialService.saveAndUpdate(initial);
        return Result.ok();
    }

    @PostMapping("/queryListBySubjectType")
    @ApiOperation("查询现金流量初始余额")
    public Result<List<JSONObject>> queryListBySubjectType(@RequestParam("subjectType") Integer subjectType) {
        List<JSONObject> list = financeInitialService.queryListBySubjectType(subjectType);
        return Result.ok(list);
    }

    @PostMapping("/queryPageBySubjectType")
    @ApiOperation("查询现金流量初始余额")
    public Result<BasePage<JSONObject>> queryPageBySubjectType(@RequestBody FinanceInitialPageBO bo) {
        BasePage<JSONObject> list = financeInitialService.queryListBySubjectType(bo);
        return Result.ok(list);
    }

    @PostMapping("/queryTrialBalance")
    @ApiOperation("试算平衡")
    public Result<JSONObject> queryTrialBalance() {
        JSONObject list = financeInitialService.queryTrialBalance();
        return Result.ok(list);
    }

    @PostMapping("/deleteById")
    @ApiOperation("删除辅助核算")
    public Result deleteById(@RequestBody FinanceInitial initial) {
        financeInitialService.deleteById(initial.getInitialId());
        return Result.ok();
    }

    @PostMapping("/deleteByAssistId")
    @ApiOperation("删除辅助核算")
    public Result deleteByAssistId(@RequestParam("assistId") Long assistId) {
        financeInitialService.deleteByAssistId(assistId);
        return Result.ok();
    }

    @PostMapping("/queryByAdjuvant")
    @ApiOperation("根据科目id，辅助核算，查询余额")
    public Result<FinanceInitial> queryByAdjuvant(@RequestBody FinanceInitialAssist assist) {
        FinanceInitial initial = financeInitialService.queryByAdjuvant(assist);
        return Result.ok(initial);
    }

}

