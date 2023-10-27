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
import com.kakarote.finance.entity.PO.FinanceParameter;
import com.kakarote.finance.service.IFinanceParameterService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 系统参数设置 前端控制器
 * </p>
 *
 * @author zhangzhiwei
 * @since 2021-08-25
 */
@RestController
@RequestMapping("/financeParameter")
@Api(tags = "系统参数设置模块")
public class FinanceParameterController {

    @Autowired
    private IFinanceParameterService financeParameterService;

    @PostMapping("/queryParameter")
    @ApiOperation("获取当前登录人系统参数")
    public Result<JSONObject> queryParameter() {
        JSONObject jsonObject = financeParameterService.queryParameter();
        return Result.ok(jsonObject);
    }

    @PostMapping("/updateParameter")
    @ApiOperation("编辑系统参数")
    @OperateLog(apply = ApplyEnum.FS, behavior = BehaviorEnum.UPDATE, object = OperateObjectEnum.FS_SYSTEM_PARAM)
    public Result updateParameter(@RequestBody FinanceParameter parameter) {
        OperationLog operationLog = financeParameterService.updateParameter(parameter);
        return OperationResult.ok(ListUtil.toList(operationLog));
    }


}

