package com.kakarote.finance.controller;


import cn.hutool.core.collection.ListUtil;
import com.alibaba.fastjson.JSONObject;
import com.kakarote.common.log.annotation.OperateLog;
import com.kakarote.common.log.entity.OperationLog;
import com.kakarote.common.log.entity.OperationResult;
import com.kakarote.common.log.enums.ApplyEnum;
import com.kakarote.common.log.enums.BehaviorEnum;
import com.kakarote.common.log.enums.OperateObjectEnum;
import com.kakarote.core.common.Result;
import com.kakarote.finance.entity.BO.FinanceStatementCertificateBO;
import com.kakarote.finance.entity.BO.FinanceStatementSaveBO;
import com.kakarote.finance.entity.BO.FinanceStatementTemplateBO;
import com.kakarote.finance.entity.PO.FinanceStatementTemplate;
import com.kakarote.finance.entity.VO.FinanceStatementVO;
import com.kakarote.finance.service.IFinanceStatementService;
import com.kakarote.finance.service.IFinanceStatementTemplateService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 结账表 前端控制器
 * </p>
 *
 * @author zhangzhiwei
 * @since 2021-08-27
 */
@RestController
@RequestMapping("/financeStatement")
@Api(tags = "结账模块")
public class FinanceStatementController {

    @Autowired
    private IFinanceStatementService financeStatementService;

    @Autowired
    private IFinanceStatementTemplateService templateService;

    @PostMapping("/add")
    @ApiOperation("保存数据")
    @OperateLog(apply = ApplyEnum.FS, behavior = BehaviorEnum.SAVE, object = OperateObjectEnum.FS_CLOSING)
    public Result add(@RequestBody FinanceStatementSaveBO statementSaveBO) {
        OperationLog operationLog = financeStatementService.saveAndUpdate(statementSaveBO);
        return OperationResult.ok(ListUtil.toList(operationLog));
    }

    @PostMapping("/update")
    @ApiOperation("编辑数据")
    @OperateLog(apply = ApplyEnum.FS, behavior = BehaviorEnum.UPDATE, object = OperateObjectEnum.FS_CLOSING)
    public Result update(@RequestBody FinanceStatementSaveBO statementSaveBO) {
        OperationLog operationLog = financeStatementService.saveAndUpdate(statementSaveBO);
        return OperationResult.ok(ListUtil.toList(operationLog));
    }

    @PostMapping("/queryStatement")
    @ApiOperation("查询列表页数据")
    public Result<FinanceStatementVO> queryStatement() {
        FinanceStatementVO mapBasePage = financeStatementService.queryStatement();
        return Result.ok(mapBasePage);
    }

    @PostMapping("/statementCertificate")
    @ApiOperation("结账生成凭证")
    @OperateLog(apply = ApplyEnum.FS, behavior = BehaviorEnum.GENERATE, object = OperateObjectEnum.FS_CLOSING)
    public Result statementCertificate(@RequestBody FinanceStatementCertificateBO statementCertificateBO) {
        List<OperationLog> operationLogList = financeStatementService.statementCertificate(statementCertificateBO);
        return OperationResult.ok(operationLogList);
    }

    @PostMapping("/statement")
    @ApiOperation("结账反结账，根据type判断结账反结账 时间格式 例 2021-08-01")
    public Result statement(@RequestBody FinanceStatementCertificateBO statementCertificateBO) {
        financeStatementService.statement(statementCertificateBO);
        return Result.ok();
    }

    @PostMapping("/addTemplate")
    @ApiOperation("保存模板数据")
    @OperateLog(apply = ApplyEnum.FS, behavior = BehaviorEnum.SAVE, object = OperateObjectEnum.FS_CLOSING_VOUCHER_TEMPLATE)
    public Result addTemplate(@RequestBody FinanceStatementTemplateBO templateBO) {
        OperationLog operationLog = templateService.saveAndUpdate(templateBO);
        return OperationResult.ok(ListUtil.toList(operationLog));
    }

    @PostMapping("/updateTemplate")
    @ApiOperation("编辑模板数据")
    @OperateLog(apply = ApplyEnum.FS, behavior = BehaviorEnum.UPDATE, object = OperateObjectEnum.FS_CLOSING_VOUCHER_TEMPLATE)
    public Result updateTemplate(@RequestBody FinanceStatementTemplateBO templateBO) {
        OperationLog operationLog = templateService.saveAndUpdate(templateBO);
        return OperationResult.ok(ListUtil.toList(operationLog));
    }

    @PostMapping("/queryListByType")
    @ApiOperation("根据type获取结账模板")
    public Result<List<FinanceStatementTemplate>> queryListByType(@RequestParam("type") Integer type) {
        List<FinanceStatementTemplate> certificateVO = templateService.queryListByType(type);
        return Result.ok(certificateVO);
    }

    @PostMapping("/querySubjectList")
    @ApiOperation("根据模板id获取科目信息")
    public Result<List<JSONObject>> querySubjectList(@RequestParam("templateId") Long templateId) {
        List<JSONObject> certificateVO = templateService.querySubjectList(templateId);
        return Result.ok(certificateVO);
    }

}

