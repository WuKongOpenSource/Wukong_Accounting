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
import com.kakarote.core.entity.UploadEntity;
import com.kakarote.core.feign.admin.service.AdminFileService;
import com.kakarote.finance.entity.BO.FinanceExportBO;
import com.kakarote.finance.entity.BO.FinanceSubjectBO;
import com.kakarote.finance.entity.BO.FinanceSubjectUpdateBO;
import com.kakarote.finance.entity.VO.FinanceSubjectVO;
import com.kakarote.finance.service.IFinanceSubjectService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * <p>
 * 科目 前端控制器
 * </p>
 *
 * @author zhangzhiwei
 * @since 2021-08-24
 */
@RestController
@RequestMapping("/financeSubject")
@Api(tags = "科目模块")
public class FinanceSubjectController {

    @Autowired
    private IFinanceSubjectService financeSubjectService;

    @Autowired
    private AdminFileService adminFileService;

    @PostMapping("/add")
    @ApiOperation("保存数据")
    @OperateLog(apply = ApplyEnum.FS, behavior = BehaviorEnum.SAVE, object = OperateObjectEnum.FS_SUBJECT)
    public Result add(@RequestBody FinanceSubjectBO subjectBO) {
        OperationLog operationLog = financeSubjectService.saveAndUpdate(subjectBO);
        return OperationResult.ok(ListUtil.toList(operationLog));
    }

    @PostMapping("/update")
    @ApiOperation("编辑数据")
    @OperateLog(apply = ApplyEnum.FS, behavior = BehaviorEnum.SAVE, object = OperateObjectEnum.FS_SUBJECT)
    public Result update(@RequestBody FinanceSubjectBO subjectBO) {
        OperationLog operationLog = financeSubjectService.saveAndUpdate(subjectBO);
        return OperationResult.ok(ListUtil.toList(operationLog));
    }

    @PostMapping("/queryListByType")
    @ApiOperation("列表查询科目")
    public Result<List<FinanceSubjectVO>> queryListByType(@RequestParam(value = "type", required = false) Integer type,
                                                          @RequestParam("isTree") Integer isTree,
                                                          @RequestParam(value = "returnType", required = false) Integer returnType,
                                                          @RequestParam(value = "certificateTime", required = false) String certificateTime) {
        List<FinanceSubjectVO> list = financeSubjectService.queryListByType(type, isTree, returnType, certificateTime);
        return Result.ok(list);
    }

    @PostMapping("/deleteByIds")
    @ApiOperation("批量删除科目")
    @OperateLog(apply = ApplyEnum.FS, behavior = BehaviorEnum.DELETE, object = OperateObjectEnum.FS_SUBJECT)
    public Result deleteByIds(@RequestBody FinanceSubjectUpdateBO subjectUpdateBO) {
        List<OperationLog> operationLogList = financeSubjectService.deleteByIds(subjectUpdateBO.getIds());
        return OperationResult.ok(operationLogList);
    }

    @PostMapping("/updateStatus")
    @ApiOperation("批量启用禁用科目")
    @OperateLog(apply = ApplyEnum.FS, object = OperateObjectEnum.FS_SUBJECT)
    public Result updateStatus(@RequestBody FinanceSubjectUpdateBO subjectUpdateBO) {
        List<OperationLog> operationLogList = financeSubjectService.updateStatus(subjectUpdateBO.getIds(), subjectUpdateBO.getStatus());
        return OperationResult.ok(operationLogList);
    }

    @PostMapping("/getAdjuvantList")
    @ApiOperation("根据科目id获取辅助核算信息")
    public Result<List<Integer>> getAdjuvantList(@RequestParam("subjectId") Long subjectId) {
        return Result.ok(financeSubjectService.getAdjuvantList(subjectId));
    }

    @PostMapping("/downloadExcel")
    @ApiOperation("下载导入模板")
    public void downloadExcel(HttpServletResponse response) throws IOException {
        financeSubjectService.downloadExcel(response);
    }

    @PostMapping("/excelImport")
    @ApiOperation("excel导入科目")
    @OperateLog(apply = ApplyEnum.FS, behavior = BehaviorEnum.EXCEL_IMPORT, type = OperateTypeEnum.IMPORT, object = OperateObjectEnum.FS_SUBJECT)
    public Result<JSONObject> excelImport(@RequestParam("file") MultipartFile file) {
        JSONObject object = financeSubjectService.excelImport(file);

        UploadEntity uploadEntity = adminFileService.uploadTempFile(file, null).getData();

        OperationLog operationLog = new OperationLog();
        operationLog.setOperationObject("uploadData", uploadEntity.getFileId());
        operationLog.setOperationObject("name", "科目");

        JSONObject info = new JSONObject();
        info.put("count", object.getInteger("totalSize"));
        info.put("errorSize", object.getInteger("errSize"));
        info.put("errorFile", object.getString("token"));
        operationLog.setOperationInfo(info.toJSONString());

        return OperationResult.ok(object, ListUtil.toList(operationLog));
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

    @PostMapping("/exportListByType")
    @ApiOperation("导出科目")
    @OperateLog(apply = ApplyEnum.FS, behavior = BehaviorEnum.EXCEL_EXPORT, type = OperateTypeEnum.EXPORT, object = OperateObjectEnum.FS_SUBJECT)
    public void exportListByType(@RequestBody FinanceExportBO exportBO) {
        financeSubjectService.exportListByType(exportBO);
    }
}

