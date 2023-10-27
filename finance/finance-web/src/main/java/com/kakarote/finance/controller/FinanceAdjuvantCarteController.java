package com.kakarote.finance.controller;


import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.extra.servlet.ServletUtil;
import com.alibaba.fastjson.JSONObject;
import com.kakarote.common.log.annotation.OperateLog;
import com.kakarote.common.log.entity.OperationLog;
import com.kakarote.common.log.entity.OperationResult;
import com.kakarote.common.log.enums.ApplyEnum;
import com.kakarote.common.log.enums.BehaviorEnum;
import com.kakarote.common.log.enums.OperateObjectEnum;
import com.kakarote.common.log.enums.OperateTypeEnum;
import com.kakarote.core.common.Result;
import com.kakarote.core.entity.BasePage;
import com.kakarote.core.entity.UploadEntity;
import com.kakarote.core.feign.admin.service.AdminFileService;
import com.kakarote.core.utils.BaseUtil;
import com.kakarote.finance.entity.BO.FinanceAdjuvantCarteBO;
import com.kakarote.finance.entity.PO.FinanceAdjuvant;
import com.kakarote.finance.entity.PO.FinanceAdjuvantCarte;
import com.kakarote.finance.service.IFinanceAdjuvantCarteService;
import com.kakarote.finance.service.IFinanceAdjuvantService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.BufferedInputStream;
import java.io.File;
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
@RequestMapping("/financeAdjuvantCarte")
@Api(tags = "辅助核算关联信息")
public class FinanceAdjuvantCarteController {

    @Autowired
    private IFinanceAdjuvantCarteService financeAdjuvantCarteService;
    @Autowired
    private IFinanceAdjuvantService financeAdjuvantService;

    @Autowired
    private AdminFileService adminFileService;

    @PostMapping("/add")
    @ApiOperation("保存数据")
    @OperateLog(apply = ApplyEnum.FS, behavior = BehaviorEnum.SAVE, object = OperateObjectEnum.FS_ADJUVANT_CARD)
    public Result add(@RequestBody FinanceAdjuvantCarte adjuvantCarte) {
        OperationLog operationLog = financeAdjuvantCarteService.saveAndUpdate(adjuvantCarte);
        return OperationResult.ok(ListUtil.toList(operationLog));
    }

    @PostMapping("/update")
    @ApiOperation("编辑数据")
    @OperateLog(apply = ApplyEnum.FS, behavior = BehaviorEnum.SAVE, object = OperateObjectEnum.FS_ADJUVANT_CARD)
    public Result update(@RequestBody FinanceAdjuvantCarte adjuvantCarte) {
        OperationLog operationLog = financeAdjuvantCarteService.saveAndUpdate(adjuvantCarte);
        return OperationResult.ok(ListUtil.toList(operationLog));
    }

    @PostMapping("/queryByAdjuvantId")
    @ApiOperation("根据辅助核算查询关联信息")
    public Result<BasePage<FinanceAdjuvantCarte>> queryByAdjuvantId(@RequestBody FinanceAdjuvantCarteBO carteBO) {
        BasePage<FinanceAdjuvantCarte> list = financeAdjuvantCarteService.queryByAdjuvantId(carteBO);
        return Result.ok(list);
    }

    @PostMapping("/updateStatusById")
    @ApiOperation("启用禁用")
    @OperateLog(apply = ApplyEnum.FS, object = OperateObjectEnum.FS_ADJUVANT_CARD)
    public Result updateStatusById(@RequestBody FinanceAdjuvantCarte adjuvantCarte) {
        OperationLog operationLog = financeAdjuvantCarteService.updateStatusById(adjuvantCarte);
        return OperationResult.ok(ListUtil.toList(operationLog));
    }

    @PostMapping("/deleteById")
    @ApiOperation("删除")
    @OperateLog(apply = ApplyEnum.FS, object = OperateObjectEnum.FS_ADJUVANT_CARD, behavior = BehaviorEnum.DELETE)
    public Result deleteById(@RequestBody List<Long> carteId) {
        List<OperationLog> operationLogList = financeAdjuvantCarteService.deleteById(carteId);
        return OperationResult.ok(operationLogList);
    }

    @PostMapping("/downloadExcel")
    @ApiOperation("下载导入模板")
    public void downloadExcel(HttpServletResponse response, @RequestParam("label") Integer label) {
        financeAdjuvantCarteService.downloadExcel(response, label);
    }

    @PostMapping("/excelImport")
    @ApiOperation("excel导入")
    @OperateLog(apply = ApplyEnum.FS, type = OperateTypeEnum.IMPORT, behavior = BehaviorEnum.EXCEL_IMPORT, object = OperateObjectEnum.FS_ADJUVANT_CARD)
    public Result<JSONObject> excelImport(@RequestParam("file") MultipartFile file, @RequestParam("label") Integer label, @RequestParam("adjuvantId") Long adjuvantId) {
        JSONObject object = financeAdjuvantCarteService.excelImport(file, label, adjuvantId);

        FinanceAdjuvant adjuvant = financeAdjuvantService.getById(adjuvantId);


        UploadEntity uploadEntity = adminFileService.uploadTempFile(file, null).getData();

        OperationLog operationLog = new OperationLog();
        operationLog.setOperationObject("uploadData", uploadEntity.getFileId());
        operationLog.setOperationObject("name", "辅助核算(" + adjuvant.getAdjuvantName() + ")");

        JSONObject info = new JSONObject();
        info.put("count", object.getInteger("totalSize"));
        info.put("errorSize", object.getInteger("errSize"));
        info.put("errorFile", object.getString("token"));
        operationLog.setOperationInfo(info.toJSONString());

        return OperationResult.ok(object, ListUtil.toList(operationLog));
    }

    @PostMapping("/synchronizeData")
    @ApiOperation("crm数据同步")
    public Result synchronizeData(@RequestBody List<FinanceAdjuvantCarteBO> carteBOS) {
        financeAdjuvantCarteService.synchronizeData(carteBOS);
        return Result.ok();
    }

    @PostMapping("/downExcel")
    @ApiOperation("excel下载错误数据")
    public void downExcel(@RequestParam("token") String token, HttpServletResponse response) {
        String path = FileUtil.getTmpDirPath() + "/" + token;
        if (FileUtil.exist(path)) {
            File file = FileUtil.file(path);
            final String fileName = file.getName();
            final String contentType = ObjectUtil.defaultIfNull(FileUtil.getMimeType(fileName), "application/octet-stream");
            BufferedInputStream in = null;
            try {
                in = FileUtil.getInputStream(file);
                ServletUtil.write(response, in, contentType, "import_error.xls");
            } finally {
                IoUtil.close(in);
            }
            FileUtil.del(path);
        }
    }

    @PostMapping("/exportExcel")
    @ApiOperation("导出数据")
    @OperateLog(apply = ApplyEnum.FS, type = OperateTypeEnum.EXPORT, behavior = BehaviorEnum.EXCEL_EXPORT, object = OperateObjectEnum.FS_ADJUVANT_CARD)
    public void allExportExcel(@RequestBody @Valid FinanceAdjuvantCarteBO carteBO, HttpServletResponse response) {
        FinanceAdjuvant adjuvant = financeAdjuvantService.getById(carteBO.getAdjuvantId());
        financeAdjuvantCarteService.exportExcel(carteBO, response);

        BaseUtil.getResponse().setHeader("name", "辅助核算(" + adjuvant.getAdjuvantName() + ")");

    }
}

