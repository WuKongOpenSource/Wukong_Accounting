package com.kakarote.finance.controller;


import com.alibaba.fastjson.JSONObject;
import com.kakarote.core.common.Result;
import com.kakarote.finance.entity.PO.FinanceTemplate;
import com.kakarote.finance.service.IFinanceTemplateService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author zhangzhiwei
 * @since 2021-08-24
 */
@RestController
@RequestMapping("/financeTemplate")
@Api(tags = "凭证模板模块")
public class FinanceTemplateController {

    @Autowired
    private IFinanceTemplateService templateService;

    @PostMapping("/add")
    @ApiOperation("保存数据")
    public Result add(@RequestBody FinanceTemplate template) {
        templateService.saveAndUpdate(template);
        return Result.ok();
    }

    @PostMapping("/update")
    @ApiOperation("编辑数据")
    public Result update(@RequestBody FinanceTemplate template) {
        templateService.saveAndUpdate(template);
        return Result.ok();
    }

    @PostMapping("/queryList")
    @ApiOperation("查询凭证模板列表")
    public Result<List<JSONObject>> queryList() {
        List<JSONObject> list = templateService.queryList();
        return Result.ok(list);
    }

    @PostMapping("/deleteById")
    @ApiOperation("删除")
    public Result deleteById(@RequestParam("templateId") Long templateId) {
        templateService.deleteById(templateId);
        return Result.ok();
    }

}

