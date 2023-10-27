package com.kakarote.finance.controller;


import com.kakarote.common.log.annotation.OperateLog;
import com.kakarote.common.log.entity.OperationLog;
import com.kakarote.common.log.entity.OperationResult;
import com.kakarote.common.log.enums.ApplyEnum;
import com.kakarote.common.log.enums.BehaviorEnum;
import com.kakarote.common.log.enums.OperateObjectEnum;
import com.kakarote.common.log.enums.OperateTypeEnum;
import com.kakarote.core.common.Result;
import com.kakarote.finance.entity.BO.FinanceAccountAuthSaveBO;
import com.kakarote.finance.entity.BO.FinanceAccountSetBO;
import com.kakarote.finance.entity.BO.FinanceNewAccountSetBO;
import com.kakarote.finance.entity.PO.AdminRole;
import com.kakarote.finance.entity.PO.FinanceAccountSet;
import com.kakarote.finance.entity.VO.FinanceAccountListVO;
import com.kakarote.finance.entity.VO.FinanceAccountVO;
import com.kakarote.finance.service.IFinanceAccountSetService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * <p>
 * 账套表 前端控制器
 * </p>
 *
 * @author dsc
 * @since 2021-08-28
 */
@RestController
@RequestMapping("/financeAccountSet")
@Api(tags = "账套管理")
public class FinanceAccountSetController {

    @Autowired
    private IFinanceAccountSetService accountSetService;

    @PostMapping("/queryPageList")
    @ApiOperation("查询账套管理列表页数据")
    public Result<List<FinanceAccountSet>> queryPageList() {
        return Result.ok(accountSetService.queryPageList());
    }

    @PostMapping("/getAccountSetById")
    @ApiOperation("查询账单详情根据id")
    public Result<FinanceAccountSet> getAccountSetById(@RequestParam("accountId") Long accountId) {
        return Result.ok(accountSetService.getAccountSetById(accountId));
    }

    @PostMapping("/addAccount")
    @ApiOperation("保存数据")
    @OperateLog(apply = ApplyEnum.FS_MANAGEMENT, type = OperateTypeEnum.SETTING
            , object = OperateObjectEnum.FS_PACKAGE_MANAGEMENT, behavior = BehaviorEnum.SAVE)
    public Result add(@RequestBody FinanceAccountSet accountSet) {
        OperationLog operationLog = accountSetService.saveAndUpdate(accountSet);
        return OperationResult.ok(operationLog);
    }

    @PostMapping("/updateAccount")
    @ApiOperation("编辑数据")
    @OperateLog(apply = ApplyEnum.FS_MANAGEMENT, type = OperateTypeEnum.SETTING
            , object = OperateObjectEnum.FS_PACKAGE_MANAGEMENT, behavior = BehaviorEnum.UPDATE)
    public Result update(@RequestBody FinanceAccountSet accountSet) {
        OperationLog operationLog = accountSetService.saveAndUpdate(accountSet);
        return OperationResult.ok(operationLog);
    }

    @PostMapping("/getUserByAccountId")
    @ApiOperation("查询账单授权员工根据id")
    public Result<FinanceAccountVO> getUserByAccountId(@RequestParam("accountId") Long accountId) {
        return Result.ok(accountSetService.getUserByAccountId(accountId));
    }

    @PostMapping("/saveAccountAuth")
    @ApiOperation("保存账套授权员工")
    @OperateLog(apply = ApplyEnum.FS_MANAGEMENT, type = OperateTypeEnum.SETTING
            , object = OperateObjectEnum.FS_PACKAGE_MANAGEMENT)
    public Result saveAccountAuth(@RequestBody FinanceAccountAuthSaveBO authSaveBO) {
        List<OperationLog> operationLogList = accountSetService.saveAccountAuth(authSaveBO);
        return OperationResult.ok(operationLogList);
    }

    @PostMapping("/deleteAccountUser")
    @ApiOperation("删除账套授权员工")
    public Result deleteAccountUser(@RequestBody FinanceAccountSetBO accountSetBO) {
        accountSetService.deleteAccountUser(accountSetBO);
        return Result.ok();
    }

    @PostMapping("/saveAccountSet")
    @ApiOperation("创建账套")
    public Result saveAccountSet(@RequestBody FinanceNewAccountSetBO accountSet) {
        accountSetService.saveAccountSet(accountSet);
        return Result.ok();
    }

    @PostMapping("/getAccountSetList")
    @ApiOperation("获取账套切换列表")
    public Result<List<FinanceAccountListVO>> getAccountSetList() {
        return Result.ok(accountSetService.getAccountSetList());
    }

    @PostMapping("/switchAccountSet")
    @ApiOperation("切换账套")
    public Result switchAccountSet(@RequestParam("accountId") Long accountId) {
        accountSetService.switchAccountSet(accountId);
        return Result.ok();
    }

    @PostMapping("/getFinanceRoleByType/{type}")
    @ApiOperation("查询财务管理角色")
    public Result<List<AdminRole>> getFinanceRoleByType(@PathVariable("type") Integer type) {
        return Result.ok(accountSetService.getFinanceRoleByType(type));
    }

    @PostMapping("/initFinanceData")
    @ApiOperation("初始化财务数据")
    public Result initFinanceData() {
        accountSetService.initFinanceData();
        return Result.ok();
    }

}

