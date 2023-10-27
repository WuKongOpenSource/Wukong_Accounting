package com.kakarote.finance.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.BigExcelWriter;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.alibaba.fastjson.JSONObject;
import com.kakarote.common.log.entity.OperationLog;
import com.kakarote.common.log.enums.BehaviorEnum;
import com.kakarote.common.upload.entity.UploadEntity;
import com.kakarote.common.utils.UserUtil;
import com.kakarote.core.common.Const;
import com.kakarote.core.common.MultipartFileUtil;
import com.kakarote.core.common.cache.AdminCacheKey;
import com.kakarote.core.entity.BasePage;
import com.kakarote.core.exception.CrmException;
import com.kakarote.core.servlet.BaseServiceImpl;
import com.kakarote.core.utils.BaseUtil;
import com.kakarote.finance.common.AccountSet;
import com.kakarote.finance.common.ExcelImportUtils;
import com.kakarote.finance.constant.FinanceCodeEnum;
import com.kakarote.finance.entity.BO.FinanceAdjuvantCarteBO;
import com.kakarote.finance.entity.PO.FinanceAdjuvant;
import com.kakarote.finance.entity.PO.FinanceAdjuvantCarte;
import com.kakarote.finance.entity.PO.FinanceCertificateAssociation;
import com.kakarote.finance.mapper.FinanceAdjuvantCarteMapper;
import com.kakarote.finance.service.AdminFileService;
import com.kakarote.finance.service.IFinanceAdjuvantCarteService;
import com.kakarote.finance.service.IFinanceAdjuvantService;
import com.kakarote.finance.service.IFinanceCertificateAssociationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author zhangzhiwei
 * @since 2021-08-24
 */
@Slf4j
@Service
public class FinanceAdjuvantCarteServiceImpl extends BaseServiceImpl<FinanceAdjuvantCarteMapper, FinanceAdjuvantCarte> implements IFinanceAdjuvantCarteService {

    @Autowired
    private IFinanceCertificateAssociationService associationService;

    @Autowired
    private IFinanceAdjuvantService adjuvantService;

    @Autowired
    private AdminFileService fileService;

    private static final int THREE = 3;

    @Override
    public OperationLog saveAndUpdate(FinanceAdjuvantCarte adjuvantCarte) {
        if (ObjectUtil.isNull(adjuvantCarte.getCarteName()) && ObjectUtil.isNull(adjuvantCarte.getCarteNumber())) {
            throw new CrmException(FinanceCodeEnum.FINANCE_ADJUVANT_CARTE_NUM_OR_NAME_NULL_ERROR);
        }
        FinanceAdjuvantCarte carte = lambdaQuery()
                .eq(FinanceAdjuvantCarte::getCarteNumber, adjuvantCarte.getCarteNumber())
                .eq(FinanceAdjuvantCarte::getAdjuvantId, adjuvantCarte.getAdjuvantId())
                .one();


        FinanceAdjuvant adjuvant = adjuvantService.getById(adjuvantCarte.getAdjuvantId());

        OperationLog operationLog = new OperationLog();
        if (ObjectUtil.isNull(adjuvantCarte.getCarteId())) {
            if (ObjectUtil.isNotNull(carte)) {
                throw new CrmException(FinanceCodeEnum.FINANCE_ADJUVANT_CARTE_NUM_EXIST_ERROR);
            }
            adjuvantCarte.setCreateTime(LocalDateTime.now());
            adjuvantCarte.setCreateUserId(UserUtil.getUserId());
            adjuvantCarte.setAccountId(AccountSet.getAccountSetId());
            adjuvantCarte.setStatus(1);
            save(adjuvantCarte);
            operationLog.setOperationObject(adjuvantCarte.getCarteName());
            operationLog.setOperationInfo("新建辅助核算(" + adjuvant.getAdjuvantName() + ")：" + adjuvantCarte.getCarteName());
        } else {
            if (ObjectUtil.isNotNull(carte) && ObjectUtil.notEqual(adjuvantCarte.getCarteId(), carte.getCarteId())) {
                throw new CrmException(FinanceCodeEnum.FINANCE_ADJUVANT_CARTE_NUM_EXIST_ERROR);
            }
            updateById(adjuvantCarte);
            operationLog.setOperationObject(adjuvantCarte.getCarteName());
            operationLog.setOperationInfo("编辑辅助核算(" + adjuvant.getAdjuvantName() + ")：" + adjuvantCarte.getCarteName());
        }
        return operationLog;
    }

    /**
     * 查询辅助核算关联数据信息
     *
     * @param carteBO
     * @return
     */
    @Override
    public BasePage<FinanceAdjuvantCarte> queryByAdjuvantId(FinanceAdjuvantCarteBO carteBO) {
        return getBaseMapper().queryByAdjuvantId(carteBO.parse(), carteBO);
    }

    @Override
    public OperationLog updateStatusById(FinanceAdjuvantCarte adjuvantCarte) {
        FinanceAdjuvantCarte carte = getById(adjuvantCarte.getCarteId());
        FinanceAdjuvant adjuvant = adjuvantService.getById(adjuvantCarte.getAdjuvantId());
        OperationLog operationLog = new OperationLog();
        operationLog.setOperationObject(carte.getCarteName());
        operationLog.setBehavior(adjuvantCarte.getStatus() == 1 ? BehaviorEnum.START : BehaviorEnum.FORBID);
        operationLog.setOperationInfo(operationLog.getBehavior().getName() + "辅助核算(" + adjuvant.getAdjuvantName() + ")：" + adjuvantCarte.getCarteName());

        lambdaUpdate()
                .set(FinanceAdjuvantCarte::getStatus, adjuvantCarte.getStatus())
                .eq(FinanceAdjuvantCarte::getCarteId, adjuvantCarte.getCarteId())
                .update();
        return operationLog;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<OperationLog> deleteById(List<Long> ids) {
        List<OperationLog> operationLogs = new ArrayList<>();
        boolean exists = associationService.lambdaQuery().in(FinanceCertificateAssociation::getRelationId, ids).exists();
        if (exists) {
            throw new CrmException(FinanceCodeEnum.FINANCE_ASSOCIATION_DELETE_ERROR);
        }
        // 获取要删除的辅助核算项
        List<FinanceAdjuvantCarte> carteList = lambdaQuery().in(FinanceAdjuvantCarte::getCarteId, ids).list();
        List<Long> adjuavntIds = carteList.stream().map(FinanceAdjuvantCarte::getAdjuvantId).collect(Collectors.toList());
        // 获取辅助核算所属分类
        List<FinanceAdjuvant> adjuvantList = adjuvantService.lambdaQuery().in(FinanceAdjuvant::getAdjuvantId, adjuavntIds).list();
        // 构造操作记录对象
        carteList.forEach(carte -> {
            FinanceAdjuvant adjuvant = adjuvantList.stream().filter(a -> ObjectUtil.equal(a.getAdjuvantId(), carte.getAdjuvantId())).findFirst().orElse(null);
            OperationLog operationLog = new OperationLog();
            operationLog.setOperationObject(carte.getCarteName());
            operationLog.setOperationInfo("删除辅助核算(" + adjuvant.getAdjuvantName() + ")：" + carte.getCarteName());
            operationLogs.add(operationLog);
        });
        // 删除辅助核算项
        lambdaUpdate()
                .set(FinanceAdjuvantCarte::getStatus, 3)
                .in(FinanceAdjuvantCarte::getCarteId, ids)
                .update();

        return operationLogs;
    }

    @Override
    public void downloadExcel(HttpServletResponse response, Integer label) {
        try (ExcelWriter writer = ExcelUtil.getWriter()) {

            List<String> dataList = new ArrayList<>();
            switch (label) {
                case 1:
                case 2: {
                    dataList = Arrays.asList("编码", "名称", "备注");
                    break;
                }
                case 3:
                case 4:
                case 5:
                case 7: {
                    dataList = Arrays.asList("编码", "名称");
                    break;
                }
                case 6: {
                    dataList = Arrays.asList("编码", "名称", "规格", "单位");
                    break;
                }
                default:
                    break;
            }

            writer.writeHeadRow(dataList);
            writer.setOnlyAlias(true);
            writer.setRowHeight(0, 20);
            for (int i = 0; i < dataList.size(); i++) {
                writer.setColumnWidth(i, 30);
            }

            /**设置单元格格式为文本格式*/
            DataFormat format = writer.getWorkbook().createDataFormat();
            CellStyle textStyle = writer.getCellStyle();
            textStyle.setDataFormat(format.getFormat("@"));
            Sheet sheet = writer.getSheet();
            for (int i = 0; i < dataList.size(); i++) {
                sheet.setDefaultColumnStyle(i, textStyle);
            }

            Cell cell = writer.getCell(0, 0);
            CellStyle cellStyle = cell.getCellStyle();
            cellStyle.setFillForegroundColor(IndexedColors.SKY_BLUE.getIndex());
            cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font font = writer.createFont();
            font.setFontHeightInPoints((short) 12);
            cellStyle.setFont(font);
            cell.setCellStyle(cellStyle);
            //response为HttpServletResponse对象
            response.setContentType("application/vnd.ms-excel;charset=utf-8");
            response.setCharacterEncoding("UTF-8");
            //弹出下载对话框的文件名，不能为中文，中文请自行编码
            response.setHeader("Content-Disposition", "attachment;filename=import.xls");
            ServletOutputStream out = response.getOutputStream();
            writer.flush(out);
        } catch (Exception e) {
            log.error("下载导入模板错误：", e);
        }
    }

    /**
     * 导入辅助核算
     *
     * @param file file
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public JSONObject excelImport(MultipartFile file, Integer label, Long adjuvantId) {
        List<List<Object>> errList = new ArrayList<>();
        String filePath = ExcelImportUtils.getFilePath(file);
        List<FinanceAdjuvantCarte> carteList = lambdaQuery()
                .eq(FinanceAdjuvantCarte::getStatus, 1)
                .eq(FinanceAdjuvantCarte::getAdjuvantId, adjuvantId)
                .eq(FinanceAdjuvantCarte::getAccountId, AccountSet.getAccountSetId()).list();
        AtomicReference<Integer> num = new AtomicReference<>(0);
        ExcelUtil.readBySax(filePath, 0, (int sheetIndex, long rowIndex, List<Object> rowList) -> {
            if (rowIndex > 0) {
                //判断编码跟名称是否都为空，都为空则跳过
                if (StrUtil.isEmptyIfStr(rowList.get(0)) && StrUtil.isEmptyIfStr(rowList.get(1))) {
                    return;
                }
                num.getAndSet(num.get() + 1);
                if (StrUtil.isEmptyIfStr(rowList.get(0))) {
                    rowList.add(0, "第" + rowIndex + "行，编码不能为空");
                    errList.add(rowList);
                    return;
                }
                if (StrUtil.isEmptyIfStr(rowList.get(1))) {
                    rowList.add(1, "第" + rowIndex + "行，名称不能为空");
                    errList.add(rowList);
                    return;
                }
                FinanceAdjuvantCarte existCarte = carteList.stream().filter(c -> ObjectUtil.equal(c.getCarteNumber(), rowList.get(0).toString())).findFirst().orElse(null);
                if (ObjectUtil.isNotNull(existCarte)) {
                    rowList.add(2, "第" + rowIndex + "行，编码重复");
                    errList.add(rowList);
                    return;
                }
                FinanceAdjuvantCarte adjuvantCarte = new FinanceAdjuvantCarte();
                adjuvantCarte.setCarteNumber(rowList.get(0).toString());
                adjuvantCarte.setCarteName(rowList.get(1).toString());
                int two = 2;
                int four = 4;
                int six = 6;
                if (label == 1 || label == two) {
                    if (rowList.size() == THREE) {
                        adjuvantCarte.setRemark(rowList.get(2) != null ? rowList.get(2).toString() : "");
                    }
                } else if (label == six) {
                    if (rowList.size() == THREE) {
                        adjuvantCarte.setSpecification(rowList.get(2) != null ? rowList.get(2).toString() : "");
                    }
                    if (rowList.size() == four) {
                        adjuvantCarte.setSpecification(rowList.get(2) != null ? rowList.get(2).toString() : "");
                        adjuvantCarte.setUnit(rowList.get(3) != null ? rowList.get(3).toString() : "");
                    }
                }

                adjuvantCarte.setAdjuvantId(adjuvantId);
                adjuvantCarte.setAccountId(AccountSet.getAccountSetId());
                adjuvantCarte.setStatus(1);
                save(adjuvantCarte);
                carteList.add(adjuvantCarte);
            } else {
                errList.add(Convert.toInt(rowIndex), rowList);
            }
        });
        FileUtil.del(filePath);
        JSONObject result = new JSONObject().fluentPut("totalSize", num.get()).fluentPut("errSize", 0);
        if (errList.size() > 1) {
            File errFile = FileUtil.file("excelData/" + IdUtil.simpleUUID() + ".xlsx");
            BigExcelWriter writer = ExcelUtil.getBigWriter(errFile);
            // 取消数据的黑色边框以及数据左对齐
            CellStyle cellStyle = writer.getCellStyle();
            cellStyle.setFillBackgroundColor(IndexedColors.BLACK.getIndex());
            cellStyle.setBorderTop(BorderStyle.NONE);
            cellStyle.setBorderBottom(BorderStyle.NONE);
            cellStyle.setBorderLeft(BorderStyle.NONE);
            cellStyle.setBorderRight(BorderStyle.NONE);
            cellStyle.setAlignment(HorizontalAlignment.LEFT);
            Font defaultFont = writer.createFont();
            defaultFont.setFontHeightInPoints((short) 11);
            cellStyle.setFont(defaultFont);
            // 取消数字格式的数据的黑色边框以及数据左对齐
            CellStyle cellStyleForNumber = writer.getStyleSet().getCellStyleForNumber();
            cellStyleForNumber.setBorderTop(BorderStyle.NONE);
            cellStyleForNumber.setBorderBottom(BorderStyle.NONE);
            cellStyleForNumber.setBorderLeft(BorderStyle.NONE);
            cellStyleForNumber.setBorderRight(BorderStyle.NONE);
            cellStyleForNumber.setAlignment(HorizontalAlignment.LEFT);
            cellStyleForNumber.setFillBackgroundColor(IndexedColors.BLACK.getIndex());
            cellStyleForNumber.setFont(defaultFont);

            CellStyle textStyle = writer.getWorkbook().createCellStyle();
            DataFormat format = writer.getWorkbook().createDataFormat();
            textStyle.setDataFormat(format.getFormat("@"));

            writer.merge(errList.get(1).size() + 1, errList.get(0).get(0).toString().trim(), true);
            writer.getHeadCellStyle().setAlignment(HorizontalAlignment.LEFT);
            writer.getHeadCellStyle().setWrapText(true);
            Font headFont = writer.createFont();
            headFont.setFontHeightInPoints((short) 11);
            writer.getHeadCellStyle().setFont(headFont);
            writer.getHeadCellStyle().setFillPattern(FillPatternType.NO_FILL);
            writer.getOrCreateRow(0).setHeightInPoints(120);
            writer.setRowHeight(-1, 20);

            //writer.merge(6, "系统用户导入模板(*)为必填项");
            for (int i = 0; i < errList.get(1).size(); i++) {
                writer.getSheet().setDefaultColumnStyle(i, textStyle);
            }
            errList.remove(0);
            writer.write(errList);
            writer.close();
            MultipartFile multipartFile = MultipartFileUtil.getMultipartFile(errFile);
            com.kakarote.common.upload.entity.UploadEntity uploadEntity = fileService.uploadTempFile(multipartFile, IdUtil.simpleUUID());
            BaseUtil.getRedis().setex(AdminCacheKey.UPLOAD_EXCEL_MESSAGE_PREFIX + "file:" + uploadEntity.getFileId(), 604800, uploadEntity.getPath());
            result.fluentPut("errSize", errList.size()).fluentPut("token", uploadEntity.getFileId());
        }
        return result;
    }

    /**
     * crm数据同步
     *
     * @param carteBOS
     */
    @Override
    public void synchronizeData(List<FinanceAdjuvantCarteBO> carteBOS) {

        List<FinanceAdjuvantCarte> adjuvantCartes = new ArrayList<>();
        carteBOS.forEach(car -> {
            FinanceAdjuvantCarte carte = new FinanceAdjuvantCarte();
            carte.setStatus(1);
            carte.setCarteName(car.getCarteName());
            carte.setAdjuvantId(car.getAdjuvantId());
            carte.setAccountId(AccountSet.getAccountSetId());
            carte.setCarteNumber(RandomUtil.randomNumbers(3));
            adjuvantCartes.add(carte);
        });
        saveBatch(adjuvantCartes, Const.BATCH_SAVE_SIZE);
    }

    /**
     * 导出数据
     *
     * @param carteBO
     * @param response
     */
    @Override
    public void exportExcel(FinanceAdjuvantCarteBO carteBO, HttpServletResponse response) {
        if (ObjectUtil.isEmpty(carteBO.getAdjuvantId())) {
            return;
        }
        //根据核算id查询标签类型
        Integer label = adjuvantService.lambdaQuery().select(FinanceAdjuvant::getLabel).eq(FinanceAdjuvant::getAdjuvantId, carteBO.getAdjuvantId()).one().getLabel();
        carteBO.setLabel(label);
        List<LinkedHashMap<String, Object>> list = getBaseMapper().getAdjuvantData(carteBO);
        // 通过工具类创建writer，默认创建xls格式
        File file = FileUtil.file("excelData/" + IdUtil.simpleUUID() + ".xlsx");
        BigExcelWriter writer = ExcelUtil.getBigWriter(file);
        // 一次性写出内容，使用默认样式，强制输出标题
        writer.write(list, true);
        //out为OutputStream，需要写出到的目标流
        //response为HttpServletResponse对象
        response.setContentType("application/vnd.ms-excel;charset=utf-8");
        //test.xls是弹出下载对话框的文件名，不能为中文，中文请自行编码
        ServletOutputStream out = null;
        try {
            response.setHeader("Content-Disposition", "attachment;filename=" + DateUtil.formatDate(DateUtil.date()) + ".xls");
            out = response.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 关闭writer，释放内存
        writer.close();
        //此处记得关闭输出Servlet流
        IoUtil.close(out);

        //将错误信息的excel保存七天 604800 七天的秒数
        MultipartFile multipartFile = MultipartFileUtil.getMultipartFile(file);
        UploadEntity uploadEntity = fileService.uploadTempFile(multipartFile, IdUtil.simpleUUID());

        BaseUtil.getRedis().setex(AdminCacheKey.UPLOAD_EXCEL_MESSAGE_PREFIX + "file:" + uploadEntity.getFileId(), 604800, uploadEntity.getPath());

        response.setHeader("fileData", uploadEntity.getFileId());
        response.setHeader("exportSize", list.size() + "");

    }

    /**
     * 获取标签名称根据id
     *
     * @param id
     * @return
     */
    @Override
    public String getName(Long id) {
        FinanceAdjuvantCarte carte = lambdaQuery().eq(FinanceAdjuvantCarte::getCarteId, id).one();
        if (ObjectUtil.isNotEmpty(carte)) {
            return carte.getCarteName();
        }
        return new String();
    }
}
