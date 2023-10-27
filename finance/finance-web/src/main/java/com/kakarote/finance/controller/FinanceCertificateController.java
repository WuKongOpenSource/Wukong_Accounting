package com.kakarote.finance.controller;


import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.kakarote.common.log.annotation.OperateLog;
import com.kakarote.common.log.entity.OperationLog;
import com.kakarote.common.log.entity.OperationResult;
import com.kakarote.common.log.enums.ApplyEnum;
import com.kakarote.common.log.enums.BehaviorEnum;
import com.kakarote.common.log.enums.OperateObjectEnum;
import com.kakarote.common.log.enums.OperateTypeEnum;
import com.kakarote.core.common.Result;
import com.kakarote.core.common.cache.CrmCacheKey;
import com.kakarote.core.entity.BasePage;
import com.kakarote.core.entity.UploadEntity;
import com.kakarote.core.feign.admin.service.AdminFileService;
import com.kakarote.core.servlet.LoginFromCookie;
import com.kakarote.core.utils.BaseUtil;
import com.kakarote.finance.entity.BO.*;
import com.kakarote.finance.entity.PO.FinanceCertificate;
import com.kakarote.finance.entity.PO.FinanceVoucher;
import com.kakarote.finance.entity.VO.FinanceCertificateVO;
import com.kakarote.finance.entity.VO.FinanceDiversificationVO;
import com.kakarote.finance.entity.VO.FinanceSubjectVO;
import com.kakarote.finance.service.IFinanceCertificateService;
import com.kakarote.finance.service.IFinanceVoucherService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * <p>
 * 凭证表 前端控制器
 * </p>
 *
 * @author zhangzhiwei
 * @since 2021-08-25
 */
@RestController
@RequestMapping("/financeCertificate")
@Api(tags = "凭证模块")
@Slf4j
public class FinanceCertificateController {

    @Autowired
    private IFinanceCertificateService financeCertificateService;

    @Autowired
    private AdminFileService adminFileService;

    @Autowired
    private IFinanceVoucherService financeVoucherService;

    @PostMapping("/add")
    @ApiOperation("保存数据")
    @OperateLog(apply = ApplyEnum.FS, behavior = BehaviorEnum.SAVE, object = OperateObjectEnum.FS_VOUCHER)
    public Result<FinanceCertificate> add(@RequestBody FinanceCertificateBO financeCertificateBO) {
        FinanceCertificate certificate = financeCertificateService.saveAndUpdate(financeCertificateBO);

        FinanceVoucher voucher = financeVoucherService.getById(certificate.getVoucherId());

        OperationLog operationLog = new OperationLog();
        operationLog.setOperationObject(LocalDateTimeUtil.format(certificate.getCertificateTime(), DatePattern.NORM_DATE_PATTERN) + " " + voucher.getVoucherName() + "_" + certificate.getCertificateNum());
        operationLog.setOperationInfo(StrUtil.format("{}凭证：{}", BehaviorEnum.SAVE.getName(), operationLog.getOperationObject()));

        return OperationResult.ok(certificate, ListUtil.toList(operationLog));
    }

    @PostMapping("/update")
    @ApiOperation("编辑数据")
    @OperateLog(apply = ApplyEnum.FS, behavior = BehaviorEnum.UPDATE, object = OperateObjectEnum.FS_VOUCHER)
    public Result<FinanceCertificate> update(@RequestBody FinanceCertificateBO financeCertificateBO) {
        FinanceCertificate certificate = financeCertificateService.saveAndUpdate(financeCertificateBO);

        FinanceVoucher voucher = financeVoucherService.getById(certificate.getVoucherId());

        OperationLog operationLog = new OperationLog();
        operationLog.setOperationObject(LocalDateTimeUtil.format(certificate.getCertificateTime(), DatePattern.NORM_DATE_PATTERN) + " " + voucher.getVoucherName() + "_" + certificate.getCertificateNum());
        operationLog.setOperationInfo(StrUtil.format("{}凭证：{}", BehaviorEnum.UPDATE.getName(), operationLog.getOperationObject()));

        return OperationResult.ok(certificate, ListUtil.toList(operationLog));
    }

    @PostMapping("/queryPageList")
    @ApiOperation("查询列表页数据")
    public Result<BasePage<FinanceCertificateVO>> queryPageList(@RequestBody FinanceSearchCertificateBO searchCertificateBO) {
        BasePage<FinanceCertificateVO> mapBasePage = financeCertificateService.queryPage(searchCertificateBO);
        return Result.ok(mapBasePage);
    }

    @PostMapping("/updateCheckStatusByIds")
    @ApiOperation("审核凭证")
    @OperateLog(apply = ApplyEnum.FS, object = OperateObjectEnum.FS_VOUCHER)
    public Result updateCheckStatusByIds(@RequestBody FinanceSubjectUpdateBO bo) {
        List<OperationLog> operationLogs = financeCertificateService.updateCheckStatusByIds(bo.getIds(), bo.getStatus());
        return OperationResult.ok(operationLogs);
    }

    @PostMapping("/deleteByIds")
    @ApiOperation("删除凭证")
    @OperateLog(apply = ApplyEnum.FS, behavior = BehaviorEnum.DELETE, object = OperateObjectEnum.FS_VOUCHER)
    public Result deleteByIds(@RequestBody FinanceSubjectUpdateBO bo) {
        List<OperationLog> operationLogs = financeCertificateService.deleteByIds(bo.getIds());
        return OperationResult.ok(operationLogs);
    }

    @PostMapping("/queryListByType")
    @ApiOperation("凭证汇总")
    public Result<List<JSONObject>> queryListCollect(@RequestBody FinanceCollectCertificateBO certificateBO) {
        List<JSONObject> list = financeCertificateService.queryListCollect(certificateBO);
        return Result.ok(list);
    }

    @PostMapping("/queryById")
    @ApiOperation("查询凭证详情")
    public Result<FinanceCertificateVO> queryById(@RequestParam("certificateId") Long certificateId) {
        FinanceCertificateVO certificateVO = financeCertificateService.queryById(certificateId);
        return Result.ok(certificateVO);
    }

    @PostMapping("/queryDetailAccount")
    @ApiOperation("查询明细账")
    public Result<List<JSONObject>> queryDetailAccount(@RequestBody FinanceDetailAccountBO accountBO) {
        List<JSONObject> list = financeCertificateService.queryDetailAccount(accountBO);
        return Result.ok(list);
    }

    @PostMapping("/queryDetailUpAccount")
    @ApiOperation("查询总账")
    public Result<List<JSONObject>> queryDetailUpAccount(@RequestBody FinanceDetailAccountBO accountBO) {
        List<JSONObject> list = financeCertificateService.queryDetailUpAccount(accountBO);
        return Result.ok(list);
    }

    @PostMapping("/queryDetailBalanceAccount")
    @ApiOperation("查询科目余额表")
    public Result<List<JSONObject>> queryDetailBalanceAccount(@RequestBody FinanceSubjectBalanceQueryBO queryBO) {
        List<JSONObject> list = financeCertificateService.querySubjectBalance(queryBO);
        return Result.ok(list);
    }

    @PostMapping("/queryDiversification")
    @ApiOperation("查询多栏账")
    public Result<FinanceDiversificationVO> queryDiversification(@RequestBody FinanceDetailAccountBO accountBO) {
        FinanceDiversificationVO list = financeCertificateService.queryDiversification(accountBO);
        return Result.ok(list);
    }

    @PostMapping("/import/template/download")
    @ApiOperation("下载导入模板")
    public void downloadVoucherImportTemplate(HttpServletResponse response) throws IOException {
        financeCertificateService.downloadVoucherImportTemplate(response);
    }

    @PostMapping("/queryNumByTime")
    @ApiOperation("根据凭证字，凭证时间，返回凭证号")
    public Result<JSONObject> queryNumByTime(@RequestBody FinanceCertificateBO financeCertificateBO) {
        JSONObject json = financeCertificateService.queryNumByTime(financeCertificateBO);
        return Result.ok(json);
    }

    @PostMapping("/import")
    @ApiOperation("导入凭证")
    @OperateLog(apply = ApplyEnum.FS, behavior = BehaviorEnum.EXCEL_IMPORT, type = OperateTypeEnum.IMPORT, object = OperateObjectEnum.FS_VOUCHER)
    public Result<JSONObject> VoucherImport(@RequestParam("file") MultipartFile file) throws IOException, IllegalAccessException {
        JSONObject object = financeCertificateService.voucherImport(file);

        UploadEntity uploadEntity = adminFileService.uploadTempFile(file, null).getData();

        OperationLog operationLog = new OperationLog();
        operationLog.setOperationObject("uploadData", uploadEntity.getFileId());
        operationLog.setOperationObject("name", "凭证");

        JSONObject info = new JSONObject();
        info.put("count", object.getInteger("totalSize"));
        info.put("errorSize", object.getInteger("errSize"));
        info.put("errorFile", object.getString("token"));
        operationLog.setOperationInfo(info.toJSONString());

        return OperationResult.ok(object, ListUtil.toList(operationLog));
    }

    @PostMapping("/insertCertificate")
    @ApiOperation("插入")
    public Result insertCertificate(@RequestBody FinanceCertificateInsertBO insertBO) {
        financeCertificateService.insertCertificate(insertBO);
        return Result.ok();
    }

    @PostMapping("/queryCertificateTime")
    @ApiOperation("获取最大凭证时间和最小凭证时间")
    public Result<JSONObject> queryCertificateTime() {
        JSONObject json = financeCertificateService.queryCertificateTime();
        return Result.ok(json);
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
                ServletUtil.write(response, in, contentType, "import_error.xlsx");
            } finally {
                IoUtil.close(in);
            }
            FileUtil.del(path);
        }
    }

    @PostMapping("/certificateSettle")
    @ApiOperation("凭证整理")
    public Result certificateSettle(@RequestBody FinanceCertificateSettleBO settleBO) {
        financeCertificateService.certificateSettle(settleBO);
        return Result.ok();
    }

    @PostMapping("/queryCertificateNum")
    @ApiOperation("获取当前顺序凭证号")
    public Result<JSONObject> queryCertificateNum(@RequestBody FinanceCertificateSettleBO settleBO) {
        JSONObject json = financeCertificateService.queryCertificateNum(settleBO);
        return Result.ok(json);
    }

    @PostMapping("/queryAmountDetailAccount")
    @ApiOperation("数量金额明细账")
    public Result<List<JSONObject>> queryAmountDetailAccount(@RequestBody FinanceDetailAccountBO accountBO) {
        List<JSONObject> list = financeCertificateService.queryAmountDetailAccount(accountBO);
        return Result.ok(list);
    }

    @PostMapping("/queryAmountDetailUpAccount")
    @ApiOperation("数量总账")
    public Result<List<JSONObject>> queryAmountDetailUpAccount(@RequestBody FinanceDetailAccountBO accountBO) {
        List<JSONObject> list = financeCertificateService.queryAmountDetailUpAccount(accountBO);
        return Result.ok(list);
    }

    @PostMapping("/queryItemsDetailBalanceAccount")
    @ApiOperation("核算项目余额表")
    public Result<List<JSONObject>> queryItemsDetailBalanceAccount(@RequestBody FinanceDetailAccountBO accountBO) {
        List<JSONObject> list = financeCertificateService.queryItemsDetailBalanceAccount(accountBO);
        return Result.ok(list);
    }

    @PostMapping("/queryLabelName")
    @ApiOperation("核算获取辅助项目")
    public Result<List<JSONObject>> queryLabelName(@RequestParam("adjuvantId") Long adjuvantId) {
        List<JSONObject> list = financeCertificateService.queryLabelName(adjuvantId);
        return Result.ok(list);
    }

    @PostMapping("/queryItemsDetailAccount")
    @ApiOperation("核算项目明细账")
    public Result<List<JSONObject>> queryItemsDetailAccount(@RequestBody FinanceDetailAccountBO accountBO) {
        List<JSONObject> list = financeCertificateService.queryItemsDetailAccount(accountBO);
        return Result.ok(list);
    }

    @PostMapping("/queryLabelNameByData")
    @ApiOperation("核算获取存在数据的辅助项目")
    public Result<List<JSONObject>> queryLabelNameByData(@RequestBody FinanceCertificateAssociationBO association) {
        List<JSONObject> objects = financeCertificateService.queryLabelNameByData(association);
        return Result.ok(objects);
    }

    @PostMapping("/itemsDetailTree")
    @ApiOperation("项目明细账树形结构")
    public Result<List<FinanceSubjectVO>> itemsDetailTree(@ApiParam("当前期数") @Param("startTime") String startTime) {
        List<FinanceSubjectVO> objectList = financeCertificateService.itemsDetailTree(startTime);
        return Result.ok(objectList);
    }

    @PostMapping("/exportCertificate")
    @ApiOperation("导出凭证")
    @OperateLog(apply = ApplyEnum.FS, behavior = BehaviorEnum.EXCEL_EXPORT, type = OperateTypeEnum.EXPORT, object = OperateObjectEnum.FS_VOUCHER)
    public void exportCertificate(@RequestBody FinanceSearchCertificateBO searchCertificateBO) {
        financeCertificateService.exportCertificate(searchCertificateBO);
    }

    @PostMapping("/exportListByType")
    @ApiOperation("导出凭证汇总")
    @OperateLog(apply = ApplyEnum.FS, behavior = BehaviorEnum.EXCEL_EXPORT, type = OperateTypeEnum.EXPORT, object = OperateObjectEnum.FS_VOUCHER)
    public void exportListByType(@RequestBody FinanceCollectCertificateBO certificateBO) {
        financeCertificateService.exportListByType(certificateBO);
        BaseUtil.getResponse().setHeader("name", "凭证汇总");
    }

    @PostMapping("/exportDetailAccount")
    @ApiOperation("导出明细账")
    @OperateLog(apply = ApplyEnum.FS, behavior = BehaviorEnum.EXCEL_EXPORT, type = OperateTypeEnum.EXPORT, object = OperateObjectEnum.FS_DETAIL_ACCOUNT)
    public void exportDetailAccount(@RequestBody FinanceDetailAccountBO accountBO) {
        financeCertificateService.exportDetailAccount(accountBO);
    }

    @PostMapping("/exportDetailUpAccount")
    @ApiOperation("导出总账")
    @OperateLog(apply = ApplyEnum.FS, behavior = BehaviorEnum.EXCEL_EXPORT, type = OperateTypeEnum.EXPORT, object = OperateObjectEnum.FS_GENERAL_LEDGER)
    public void exportDetailUpAccount(@RequestBody FinanceDetailAccountBO accountBO) {
        financeCertificateService.exportDetailUpAccount(accountBO);
    }

    @PostMapping("/exportDetailBalanceAccount")
    @ApiOperation("导出科目余额表")
    @OperateLog(apply = ApplyEnum.FS, behavior = BehaviorEnum.EXCEL_EXPORT, type = OperateTypeEnum.EXPORT, object = OperateObjectEnum.FS_SUBJECT_BALANCE)
    public void exportDetailBalanceAccount(@RequestBody FinanceSubjectBalanceQueryBO queryBO) {
        financeCertificateService.exportDetailBalanceAccount(queryBO);
    }

    @PostMapping("/exportDiversification")
    @ApiOperation("导出多栏账")
    @OperateLog(apply = ApplyEnum.FS, behavior = BehaviorEnum.EXCEL_EXPORT, type = OperateTypeEnum.EXPORT, object = OperateObjectEnum.FS_MULTIPLE_BOOKS)
    public void exportDiversification(@RequestBody FinanceDetailAccountBO accountBO) {
        financeCertificateService.exportDiversification(accountBO);
    }

    @PostMapping("/exportItemsDetailAccount")
    @ApiOperation("导出核算项目明细账")
    @OperateLog(apply = ApplyEnum.FS, behavior = BehaviorEnum.EXCEL_EXPORT, type = OperateTypeEnum.EXPORT, object = OperateObjectEnum.FS_PROJECT_DETAIL_ACCOUNT)
    public void exportItemsDetailAccount(@RequestBody FinanceDetailAccountBO accountBO) {
        financeCertificateService.exportItemsDetailAccount(accountBO);
    }

    @PostMapping("/exportItemsDetailBalanceAccount")
    @ApiOperation("导出核算项目余额表")
    @OperateLog(apply = ApplyEnum.FS, behavior = BehaviorEnum.EXCEL_EXPORT, type = OperateTypeEnum.EXPORT, object = OperateObjectEnum.FS_PROJECT_BALANCE)
    public void exportItemsDetailBalanceAccount(@RequestBody FinanceDetailAccountBO accountBO) {
        financeCertificateService.exportItemsDetailBalanceAccount(accountBO);
    }

    @PostMapping("/exportAmountDetailAccount")
    @ApiOperation("导出数量金额明细账")
    @OperateLog(apply = ApplyEnum.FS, behavior = BehaviorEnum.EXCEL_EXPORT, type = OperateTypeEnum.EXPORT, object = OperateObjectEnum.FS_QUANTITY_AMOUNT_DETAIL_ACCOUNT)
    public void exportAmountDetailAccount(@RequestBody FinanceDetailAccountBO accountBO) {
        financeCertificateService.exportAmountDetailAccount(accountBO);
    }

    @PostMapping("/exportAmountDetailUpAccount")
    @ApiOperation("导出数量总账")
    @OperateLog(apply = ApplyEnum.FS, behavior = BehaviorEnum.EXCEL_EXPORT, type = OperateTypeEnum.EXPORT, object = OperateObjectEnum.FS_QUANTITY_AMOUNT_GENERAL_LEDGER)
    public void exportAmountDetailUpAccount(@RequestBody FinanceDetailAccountBO accountBO) {
        financeCertificateService.exportAmountDetailUpAccount(accountBO);
    }


    @PostMapping("/exportPageBySubjectType")
    @ApiOperation("导出财务初始余额")
    @OperateLog(apply = ApplyEnum.FS, behavior = BehaviorEnum.EXCEL_EXPORT, type = OperateTypeEnum.EXPORT, object = OperateObjectEnum.FS_INITIAL_BALANCE)
    public void exportPageBySubjectType(@RequestBody FinanceInitialPageBO bo) {
        financeCertificateService.exportPageBySubjectType(bo);
    }

    @PostMapping("/downloadFinanceInitialExcel")
    @ApiOperation("下载财务初始余额导入模板")
    public void downloadFinanceInitialExcel(@RequestBody FinanceInitialPageBO bo) {
        financeCertificateService.downloadFinanceInitialExcel(bo);
    }

    @PostMapping("/financeInitialImport")
    @ApiOperation("导入财务初始余额")
    @OperateLog(apply = ApplyEnum.FS, behavior = BehaviorEnum.EXCEL_IMPORT, type = OperateTypeEnum.IMPORT, object = OperateObjectEnum.FS_INITIAL_BALANCE)
    public Result<Integer> financeInitialImport(@RequestParam("file") MultipartFile file) {
        Integer size = financeCertificateService.financeInitialImport(file);

        UploadEntity uploadEntity = adminFileService.uploadTempFile(file, null).getData();
        OperationLog operationLog = new OperationLog();
        operationLog.setOperationObject("uploadData", uploadEntity.getFileId());
        operationLog.setOperationObject("name", "财务初始余额");
        JSONObject info = new JSONObject();
        info.put("count", size);
        operationLog.setOperationInfo(info.toJSONString());

        return OperationResult.ok(size, ListUtil.toList(operationLog));
    }

    @ApiOperation("财务凭证预览")
    @PostMapping("/preview")
    public Result<String> preview(@RequestBody CrmPrintPropertiesBO properties) {
        String s = financeCertificateService.preview(properties);
        return Result.ok(s);
    }

    @ApiOperation("财务凭证预览")
    @PostMapping("/previewFinance")
    public Result<String> previewFinance(@RequestParam(value = "content", required = false) String content, @RequestParam(value = "type", required = false) String type, @RequestParam(value = "orientation", required = false) String orientation, @RequestParam(value = "pageSize", required = false) String pageSize) {
        String s = financeCertificateService.previewFinance(content, type, orientation, pageSize);
        return Result.ok(s);
    }

    @ApiOperation("检查科目是否已经录入凭证")
    @PostMapping("/checkSubjectCertificate")
    public Result<Boolean> checkSubjectCertificate(@RequestParam("subjectId") Long subjectId) {
        Boolean aBoolean = financeCertificateService.checkSubjectCertificate(subjectId);
        return Result.ok(aBoolean);
    }

    @ApiOperation(value = "iframe", httpMethod = "GET")
    @RequestMapping("/preview.pdf")
    @LoginFromCookie
    public void preview(String key, HttpServletResponse response) {
        String object = BaseUtil.getRedis().get(CrmCacheKey.CRM_PRINT_TEMPLATE_CACHE_KEY + key);
        if (StrUtil.isNotEmpty(object)) {
            JSONObject parse = JSON.parseObject(object);
            String path = parse.getString("pdf");
            if (FileUtil.exist(path)) {
                File file = FileUtil.file(path);
                BufferedInputStream in = null;
                ServletOutputStream out = null;
                try {
                    in = FileUtil.getInputStream(file);
                    response.setContentType("application/pdf");
                    IoUtil.copy(in, response.getOutputStream());
                } catch (Exception ex) {
                    log.error("导出错误", ex);
                } finally {
                    IoUtil.close(in);
                    IoUtil.close(out);
                }
                return;
            }
        }
        ServletUtil.write(response, Result.ok().toJSONString(), "text/plain");
    }
}

