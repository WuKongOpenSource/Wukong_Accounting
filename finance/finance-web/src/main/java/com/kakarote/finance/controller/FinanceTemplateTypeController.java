package com.kakarote.finance.controller;


import com.kakarote.core.common.Result;
import com.kakarote.finance.entity.PO.FinanceTemplateType;
import com.kakarote.finance.service.IFinanceTemplateTypeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 凭证模板类型 前端控制器
 * </p>
 *
 * @author zhangzhiwei
 * @since 2021-08-29
 */
@RestController
@RequestMapping("/financeTemplateType")
@Api(tags = "凭证模板类别模块")
public class FinanceTemplateTypeController {

    @Autowired
    private IFinanceTemplateTypeService templateTypeService;

    @PostMapping("/add")
    @ApiOperation("保存数据")
    public Result add(@RequestBody FinanceTemplateType template) {
        templateTypeService.saveAndUpdate(template);
        return Result.ok();
    }

    @PostMapping("/update")
    @ApiOperation("编辑数据")
    public Result update(@RequestBody FinanceTemplateType template) {
        templateTypeService.saveAndUpdate(template);
        return Result.ok();
    }

    @PostMapping("/queryList")
    @ApiOperation("查询凭证模板列表")
    public Result<List<FinanceTemplateType>> queryList() {
        List<FinanceTemplateType> list = templateTypeService.queryList();
        return Result.ok(list);
    }

    @PostMapping("/deleteById")
    @ApiOperation("删除")
    public Result deleteById(@RequestParam("typeId") Long typeId) {
        templateTypeService.deleteById(typeId);
        return Result.ok();
    }

}

