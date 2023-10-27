package com.kakarote.finance.controller;


import com.kakarote.core.common.Result;
import com.kakarote.finance.entity.PO.FinanceDigest;
import com.kakarote.finance.service.IFinanceDigestService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 凭证摘要 前端控制器
 * </p>
 *
 * @author zhangzhiwei
 * @since 2021-08-25
 */
@RestController
@RequestMapping("/financeDigest")
@Api(tags = "摘要模块")
public class FinanceDigestController {

    @Autowired
    private IFinanceDigestService financeDigestService;

    @PostMapping("/add")
    @ApiOperation("保存数据")
    public Result add(@RequestBody FinanceDigest digest) {
        financeDigestService.saveAndUpdate(digest);
        return Result.ok();
    }

    @PostMapping("/update")
    @ApiOperation("编辑数据")
    public Result update(@RequestBody FinanceDigest digest) {
        financeDigestService.saveAndUpdate(digest);
        return Result.ok();
    }

    @PostMapping("/queryList")
    @ApiOperation("列表查询摘要")
    public Result<List<FinanceDigest>> queryList() {
        List<FinanceDigest> list = financeDigestService.queryList();
        return Result.ok(list);
    }

    @PostMapping("/deleteById")
    @ApiOperation("删除")
    public Result deleteById(@RequestParam("digestId") Long digestId) {
        financeDigestService.deleteById(digestId);
        return Result.ok();
    }

}

