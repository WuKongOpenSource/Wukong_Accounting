package com.kakarote.finance.controller;


import com.alibaba.fastjson.JSONObject;
import com.kakarote.core.common.Result;
import com.kakarote.finance.service.IFinanceAccountUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 账套员工对应关系表 前端控制器
 * </p>
 *
 * @author dsc
 * @since 2021-08-29
 */
@RestController
@RequestMapping("/financeAccountUser")
@Api(tags = "账套授权员工")
public class FinanceAccountUserController {


    @Autowired
    private IFinanceAccountUserService accountUserService;

    @PostMapping("/financeAuth")
    @ApiOperation("获取财务管理角色权限")
    public Result<JSONObject> financeAuth() {
        JSONObject object = accountUserService.financeAuth();
        return Result.ok(object);
    }
}

