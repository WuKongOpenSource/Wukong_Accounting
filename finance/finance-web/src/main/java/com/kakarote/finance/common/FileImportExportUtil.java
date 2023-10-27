package com.kakarote.finance.common;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.BigExcelWriter;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.alibaba.fastjson.JSONObject;
import com.kakarote.common.upload.entity.UploadEntity;
import com.kakarote.core.common.MultipartFileUtil;
import com.kakarote.core.common.cache.AdminCacheKey;
import com.kakarote.core.servlet.ApplicationContextHolder;
import com.kakarote.core.utils.BaseUtil;
import com.kakarote.finance.entity.BO.*;
import com.kakarote.finance.service.AdminFileService;
import io.swagger.annotations.ApiModelProperty;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author : zjj
 * @since : 2023/3/7
 */
public class FileImportExportUtil {

    /**
     * 解析表格数据为标准对象
     *
     * @param rowList excel 行数据
     * @return data
     */
    public static List<VoucherImportBO> parseData2Object(List<Map<String, Object>> rowList) throws IllegalAccessException {
        Field[] fields = VoucherImportBO.class.getDeclaredFields();
        List<VoucherImportBO> list = new ArrayList<>();
        AtomicInteger rowNumber = new AtomicInteger(1);
        for (Map<String, Object> map : rowList) {
            VoucherImportBO bo = new VoucherImportBO();
            bo.setRowNumber(rowNumber.getAndIncrement());
            bo.setSourceMap(map);
            for (Field field : fields) {
                String fieldName = field.getName();
                if (VoucherImportBO.ignoreFields(fieldName)) {
                    continue;
                }
                String annotationName = field.getAnnotation(ApiModelProperty.class).value();
                Object value = null;
                field.setAccessible(true);
                Type type = field.getAnnotatedType().getType();
                try {
                    if (Date.class.equals(type)) {
                        value = MapUtil.getDate(map, annotationName);
                    } else if (Integer.class.equals(type)) {
                        value = MapUtil.getInt(map, annotationName);
                    } else if (BigDecimal.class.equals(type)) {
                        String str = Optional.ofNullable(MapUtil.getStr(map, annotationName)).map(String::trim).orElse(null);
                        if (StrUtil.isNotEmpty(str)) {
                            value = MapUtil.get(map, annotationName, BigDecimal.class);
                        }
                    } else {
                        String str = Optional.ofNullable(MapUtil.getStr(map, annotationName)).map(String::trim).orElse(null);
                        value = StrUtil.isEmpty(str) ? null : str;
                    }
                } catch (Exception e) {
                    bo.getErrors().add(String.format("[%s]数据格式错误", annotationName));
                }
                field.set(bo, value);
            }
            list.add(bo);
        }
        return list;
    }

    /**
     * 凭证数据为空字段检查
     *
     * @param voucherMap 凭证数据
     */
    public static void checkVoucherEmptyField(Map<String, List<VoucherImportBO>> voucherMap) throws IllegalAccessException {
        Field[] fields = VoucherImportBO.class.getDeclaredFields();
        // 不能为空的字段
        List<Field> notEmptyFields = Arrays.stream(fields).filter(f -> !VoucherImportBO.ignoreFields(f.getName()))
                .filter(f -> ObjectUtil.isNotNull(f.getAnnotation(NotEmpty.class))
                ).collect(Collectors.toList());
        for (Map.Entry<String, List<VoucherImportBO>> entry : voucherMap.entrySet()) {
            List<VoucherImportBO> list = entry.getValue();
            for (VoucherImportBO bo : list) {
                // 验证不能为空的字段
                for (Field field : notEmptyFields) {
                    NotEmpty annotation = field.getAnnotation(NotEmpty.class);
                    field.setAccessible(true);
                    if (ObjectUtil.isEmpty(field.get(bo))) {
                        String annotationName = field.getAnnotation(ApiModelProperty.class).value();
                        String errorMessage = String.format("[%s]" + annotation.message(), annotationName);
                        bo.getErrors().add(errorMessage);
                    }
                }
                // 验证必填写一个的字段
                List<Field> mustFillOnlyOneFields = Arrays.stream(fields).filter(f -> VoucherImportBO.mustFillOnlyOne().contains(f.getName())).collect(Collectors.toList());
                // 已填写的字段
                List<Field> filledFields = new ArrayList<>();
                List<Object> values = new ArrayList<>();
                for (Field field : mustFillOnlyOneFields) {
                    field.setAccessible(true);
                    Object value = field.get(bo);
                    if (ObjectUtil.isNotNull(value)) {
                        values.add(value);
                        filledFields.add(field);
                    }
                }
                // 都为空
                if (CollUtil.isEmpty(values)) {
                    List<String> annotationNames = mustFillOnlyOneFields.stream().map(f -> f.getAnnotation(ApiModelProperty.class).value()).collect(Collectors.toList());
                    String errorMessage = String.format("[%s][%s]不能都为空", annotationNames.toArray());
                    bo.getErrors().add(errorMessage);
                }
                // 填写超过一个
                if (values.size() > 1) {
                    List<String> annotationNames = filledFields.stream().map(f -> f.getAnnotation(ApiModelProperty.class).value()).collect(Collectors.toList());
                    String errorMessage = String.format("[%s][%s]不能同时填写", annotationNames.toArray());
                    bo.getErrors().add(errorMessage);
                }
                // 不能为0
                if (values.size() == 1) {
                    Object value = values.get(0);
                    if (BigDecimal.ZERO.equals(value)) {
                        List<String> annotationNames = filledFields.stream().map(f -> f.getAnnotation(ApiModelProperty.class).value()).collect(Collectors.toList());
                        String errorMessage = String.format("[%s]不能为0", annotationNames.toArray());
                        bo.getErrors().add(errorMessage);
                    }
                }
            }
            // 摘要字段判断
            VoucherImportBO bo = list.stream().filter(b -> StrUtil.isNotEmpty(b.getDigestContent())).findFirst().orElse(null);
            if (ObjectUtil.isNull(bo)) {
                VoucherImportBO firstRow = CollUtil.getFirst(list);
                String errorMessage = String.format("第[%s]行摘要不能为空", firstRow.getRowNumber());
                firstRow.getErrors().add(errorMessage);
            }
        }
    }


    /**
     * 凭证字校验
     *
     * @param voucherMap 凭证数据
     */
    public static void checkVoucherNumber(Map<String, List<VoucherImportBO>> voucherMap) throws IllegalAccessException {
        Field[] fields = VoucherImportBO.class.getDeclaredFields();
        // 设置最小值的字段
        List<Field> setMinFields = Arrays.stream(fields).filter(f -> !VoucherImportBO.ignoreFields(f.getName()))
                .filter(f -> ObjectUtil.isNotNull(f.getAnnotation(Min.class))
                ).collect(Collectors.toList());
        for (Map.Entry<String, List<VoucherImportBO>> entry : voucherMap.entrySet()) {
            List<VoucherImportBO> list = entry.getValue();
            for (VoucherImportBO bo : list) {
                // 验证设置最小值的字段
                for (Field field : setMinFields) {
                    String annotationName = field.getAnnotation(ApiModelProperty.class).value();
                    Min minAnnotation = field.getAnnotation(Min.class);
                    field.setAccessible(true);
                    Object value = field.get(bo);
                    long minValue = minAnnotation.value();
                    if (ObjectUtil.isNotNull(value)) {
                        if (Long.parseLong(value.toString()) < minValue) {
                            bo.getErrors().add(String.format("[%s]数据格式错误", annotationName));
                        }
                    }
                }
            }
        }
    }

    /**
     * 分组内数据借贷平衡检查
     *
     * @param voucherMap 凭证数据
     */
    public static void checkVoucherBalance(Map<String, List<VoucherImportBO>> voucherMap) {

        for (Map.Entry<String, List<VoucherImportBO>> entry : voucherMap.entrySet()) {
            List<VoucherImportBO> list = entry.getValue();
            VoucherImportBO anyError = list.stream().filter(d -> CollUtil.isNotEmpty(d.getErrors())).findFirst().orElse(null);
            if (ObjectUtil.isNull(anyError)) {
                BigDecimal debtor = list.stream().map(d -> Optional.ofNullable(d.getDebtorBalance()).orElse(BigDecimal.ZERO))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal credit = list.stream().map(d -> Optional.ofNullable(d.getCreditBalance()).orElse(BigDecimal.ZERO))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                if (debtor.compareTo(credit) != 0) {
                    String errorMessage = "借贷方金额必须相等";
                    setErrorMessage(list, errorMessage);
                }
            }
        }
    }

    /**
     * 日期凭证号校验
     *
     * @param voucherMap                凭证数据
     * @param certificateWithVoucherBOS 系统中已存在的凭证日期凭证号信息
     */
    public static void checkVoucherNameNum(Map<String, List<VoucherImportBO>> voucherMap,
                                           List<CertificateWithVoucherBO> certificateWithVoucherBOS) {
        Set<String> certificateVoucherNumberSet = certificateWithVoucherBOS.stream().map(c ->
                String.format("%s_%s_%s",
                        Optional.ofNullable(DateUtil.format(c.getCertificateTime(), DatePattern.PURE_DATE_PATTERN)).orElse("*"),
                        Optional.ofNullable(c.getVoucherName()).orElse("*"),
                        Optional.ofNullable(c.getVoucherNum()).map(String::valueOf).orElse("*")
                )).collect(Collectors.toSet());
        for (Map.Entry<String, List<VoucherImportBO>> entry : voucherMap.entrySet()) {
            if (certificateVoucherNumberSet.contains(entry.getKey())) {
                String errorMessage = "日期凭证字与凭证号重复";
                FileImportExportUtil.setErrorMessage(entry.getValue(), errorMessage);
            }
        }
    }

    /**
     * 设置错误信息
     *
     * @param list         数据
     * @param errorMessage 错误信息
     */
    public static void setErrorMessage(List<VoucherImportBO> list, String errorMessage) {
        if (CollUtil.isNotEmpty(list)) {
            for (VoucherImportBO bo : list) {
                bo.getErrors().add(errorMessage);
            }
        }
    }

    /**
     * 科目检查
     *
     * @param voucherMap  凭证数据
     * @param subjectList 科目信息
     */
    public static void checkSubject(Map<String, List<VoucherImportBO>> voucherMap,
                                    List<FinanceSubjectImportBO> subjectList) {
        Set<String> subjectNumbers = subjectList.stream().map(FinanceSubjectImportBO::getNumber).collect(Collectors.toSet());
        for (Map.Entry<String, List<VoucherImportBO>> entry : voucherMap.entrySet()) {
            List<VoucherImportBO> list = entry.getValue();
            for (VoucherImportBO bo : list) {
                if (!CollUtil.contains(subjectNumbers, bo.getSubjectNumber())) {
                    String errorMessage = String.format("系统不存在编码为 [%s][%s] 的科目，请修改后再导入", bo.getSubjectNumber(), bo.getSubjectName());
                    bo.getErrors().add(errorMessage);
                }
            }
        }
    }

    /**
     * 辅助核算检查
     *
     * @param voucherMap        凭证数据
     * @param subjectList       科目信息
     * @param adjuvantCarteList 辅助核算数据
     */
    public static void checkAdjuvant(Map<String, List<VoucherImportBO>> voucherMap,
                                     List<FinanceSubjectImportBO> subjectList,
                                     List<FinanceAdjuvantCarteImportBO> adjuvantCarteList) {
        // 添加了辅助核算的科目
        Map<String, FinanceSubjectImportBO> subjectAddAdjuvantMap = subjectList.stream()
                .filter(s -> ObjectUtil.isNotNull(s.getLabel()))
                .collect(Collectors.toMap(FinanceSubjectImportBO::getNumber, Function.identity()));
        Map<Integer, Set<String>> labelCartNumberMap = adjuvantCarteList.stream()
                .collect(Collectors.groupingBy(FinanceAdjuvantCarteImportBO::getLabel,
                        Collectors.mapping(FinanceAdjuvantCarteImportBO::getCarteNumber, Collectors.toSet()))
                );
        for (Map.Entry<String, List<VoucherImportBO>> entry : voucherMap.entrySet()) {
            List<VoucherImportBO> list = entry.getValue();
            // 应有辅助核算的行
            List<VoucherImportBO> withAdjuvantList = list.stream().filter(v -> subjectAddAdjuvantMap.containsKey(v.getSubjectNumber())).collect(Collectors.toList());
            for (VoucherImportBO bo : withAdjuvantList) {
                FinanceSubjectImportBO subject = subjectAddAdjuvantMap.get(bo.getSubjectNumber());
                Integer label = subject.getLabel();
                String labelName = subject.getLabelName();
                String value = MapUtil.getStr(bo.getSourceMap(), labelName);
                if (StrUtil.isEmpty(value)) {
                    String errorMessage = String.format("辅助核算[%s]编码不可为空", labelName);
                    bo.getErrors().add(errorMessage);
                } else {
                    Set<String> cartNumbers = labelCartNumberMap.get(label);
                    if (!CollUtil.contains(cartNumbers, value)) {
                        String errorMessage = String.format("辅助核算[%s]编码不存在", labelName);
                        bo.getErrors().add(errorMessage);
                    }
                }
                // 未开启数量核算
                if (!subject.isCash()) {
                    if (ObjectUtil.isNotNull(bo.getQuality()) || ObjectUtil.isNotNull(bo.getPrice())) {
                        String errorMessage = String.format("[%s][%s]科目不能数量核算", subject.getNumber(), subject.getSubjectName());
                        bo.getErrors().add(errorMessage);
                    }
                }
            }
        }
    }

    /**
     * 凭证日期检查
     *
     * @param voucherMap    凭证数据
     * @param currentPeriod 当前日期
     */
    public static void checkCertificateTime(Map<String, List<VoucherImportBO>> voucherMap,
                                            String currentPeriod) {
        for (Map.Entry<String, List<VoucherImportBO>> entry : voucherMap.entrySet()) {
            List<VoucherImportBO> list = entry.getValue();
            Date certificateTime = list.get(0).getCertificateTime();
            // 日期为空则跳过
            if (ObjectUtil.isNull(certificateTime)) {
                continue;
            }
            // 日期小于当前账期
            String certificateTimeStr = DateUtil.format(certificateTime, DatePattern.SIMPLE_MONTH_PATTERN);
            if (Convert.toLong(certificateTimeStr) < Convert.toLong(currentPeriod)) {
                String errorMessage = "该期已结账，不能进行添加凭证";
                setErrorMessage(list, errorMessage);
            }
        }
    }

    /**
     * 生成错误文件
     *
     * @param voucherMap 凭证数据
     * @param reader     reader
     * @return ExcelWriter
     */
    public static JSONObject generateErrorFile(Map<String, List<VoucherImportBO>> voucherMap,
                                               ExcelReader reader) {
        JSONObject result = new JSONObject();
        Sheet sourceSheet = reader.getSheet();
        List<VoucherImportBO> errorRows = new ArrayList<>();
        for (Map.Entry<String, List<VoucherImportBO>> entry : voucherMap.entrySet()) {
            List<VoucherImportBO> list = entry.getValue();
            VoucherImportBO anyError = list.stream().filter(d -> CollUtil.isNotEmpty(d.getErrors())).findFirst().orElse(null);
            if (ObjectUtil.isNotNull(anyError)) {
                errorRows.addAll(list);
            }
        }
        errorRows = errorRows.stream().sorted(Comparator.comparing(VoucherImportBO::getRowNumber)).collect(Collectors.toList());
        if (CollUtil.isEmpty(errorRows)) {
            result.fluentPut("errSize", errorRows.size());
            return result;
        }

        List<Map<String, Object>> rowList = new ArrayList<>();
        for (VoucherImportBO errorRow : errorRows) {
            Map<String, Object> newRow = new LinkedHashMap<>();
            Map<String, Object> sourceMap = errorRow.getSourceMap();
            newRow.put("错误信息", StrUtil.join(";", errorRow.getErrors()));
            newRow.putAll(sourceMap);
            rowList.add(newRow);
        }

        File file = FileUtil.file("excelData/" + IdUtil.simpleUUID() + ".xlsx");
        BigExcelWriter writer = ExcelUtil.getBigWriter(file);

        defaultStyle(writer);
        headerStyle(writer);
        writer.renameSheet(sourceSheet.getSheetName());
        Row sourceTopRow = sourceSheet.getRow(0);
        generateHeader(sourceTopRow, writer);
        writer.write(rowList, true);
        // 关闭writer，释放内存
        writer.close();
        MultipartFile multipartFile = MultipartFileUtil.getMultipartFile(file);

        AdminFileService fileService = ApplicationContextHolder.getBean(AdminFileService.class);
        UploadEntity uploadEntity = fileService.uploadTempFile(multipartFile, IdUtil.simpleUUID());
        BaseUtil.getRedis().setex(AdminCacheKey.UPLOAD_EXCEL_MESSAGE_PREFIX + "file:" + uploadEntity.getFileId(), 604800, uploadEntity.getPath());
        result.fluentPut("errSize", errorRows.size()).fluentPut("token", uploadEntity.getFileId());
        return result;
    }

    public static void generateHeader(Row sourceTopRow, ExcelWriter writer) {
        Sheet currentSheet = writer.getSheet();
        Row currentTopRow = currentSheet.createRow(0);
        AtomicInteger index = new AtomicInteger(0);
        // 创建错误信息表头
        Cell errorInfoCell = currentTopRow.createCell(index.getAndIncrement());
        errorInfoCell.setCellValue("错误信息");

        Iterator<Cell> iterator = sourceTopRow.cellIterator();
        while (iterator.hasNext()) {
            Cell sourceCell = iterator.next();
            Comment cellComment = sourceCell.getCellComment();
            Cell cell = currentTopRow.createCell(index.getAndIncrement());
            cell.setCellValue(sourceCell.getStringCellValue());
            if (ObjectUtil.isNotNull(cellComment)) {
                addComment(cell, cellComment.getString().getString());
            }
        }
    }

    /**
     * 生成凭证导入模板
     *
     * @param sheetBOS bos
     * @return ExcelWriter
     */
    public static ExcelWriter generateVoucherImportFile(List<SheetBO> sheetBOS) throws IOException {
        ClassPathResource classPathResource = new ClassPathResource("excelTemplates/voucherImport.xlsx");
        ExcelReader reader = ExcelUtil.getReader(classPathResource.getInputStream());
        ExcelWriter writer = reader.getWriter();
        defaultStyle(writer);
        List<Sheet> sheets = writer.getSheets();
        sheetBOS = sheetBOS.stream().sorted(Comparator.comparing(SheetBO::getIndex)).collect(Collectors.toList());
        for (SheetBO sheetBO : sheetBOS) {
            String sheetName = sheetBO.getName();
            Sheet sheet = sheets.stream().filter(s -> StrUtil.equals(sheetName, s.getSheetName())).findFirst().orElse(null);

            if (ObjectUtil.isNotNull(sheet)) {
                if (StrUtil.equals("凭证模板", sheetName)) {
                    Row row = sheet.getRow(0);
                    AtomicInteger index = new AtomicInteger(row.getLastCellNum());
                    for (Map.Entry<String, String> entry : sheetBO.getHeaderNameComment().entrySet()) {
                        String value = entry.getKey();
                        String comment = entry.getValue();

                        int columnNum = index.getAndIncrement();
                        Cell cell = row.createCell(columnNum);
                        cell.setCellValue(value);
                        if (StrUtil.isNotEmpty(comment)) {
                            addComment(cell, comment);
                        }
                        sheet.autoSizeColumn(columnNum);
                        CellStyle cellStyle = headerStyle(writer);
                        cell.setCellStyle(cellStyle);
                        CellStyle columnStyle = writer.createColumnStyle(columnNum);
                        DataFormat dataFormat = writer.getWorkbook().createDataFormat();
                        columnStyle.setDataFormat(dataFormat.getFormat("@"));
                    }
                    for (Map.Entry<String, String[]> entry : sheetBO.getHeaderNameDefaultValue().entrySet()) {
                        String key = entry.getKey();
                        String[] values = entry.getValue();
                        Iterator<Cell> cellIterator = row.cellIterator();
                        Iterable<Cell> iterable = () -> cellIterator;
                        List<Cell> cells = StreamSupport.stream(iterable.spliterator(), false).collect(Collectors.toList());
                        Cell cell = cells.stream().filter(c -> StrUtil.equals(key, c.getStringCellValue())).findFirst().orElse(null);
                        if (ObjectUtil.isNotNull(cell)) {
                            CellRangeAddressList cellRangeAddressList = new CellRangeAddressList(1, 10002, cell.getColumnIndex(), cell.getColumnIndex());
                            DataValidationHelper validationHelper = sheet.getDataValidationHelper();
                            DataValidationConstraint constraint = validationHelper.createExplicitListConstraint(values);
                            // 设置下拉框数据
                            DataValidation dataValidation = validationHelper.createValidation(constraint, cellRangeAddressList);
                            sheet.addValidationData(dataValidation);
                        }

                    }

                } else {
                    writer.setSheet(sheetName);
                    writer.passRows(sheet.getLastRowNum() + 1);
                    List<Map<String, Object>> rows = sheetBO.getRows();
                    List<LinkedHashMap<String, Object>> dataList = new ArrayList<>();
                    Row row = sheet.getRow(sheet.getLastRowNum());
                    for (Map<String, Object> map : rows) {
                        LinkedHashMap<String, Object> newMap = new LinkedHashMap<>();
                        Iterator<Cell> cellIterator = row.cellIterator();
                        while (cellIterator.hasNext()) {
                            Cell next = cellIterator.next();
                            String cellValue = next.getStringCellValue();
                            Object value = map.get(cellValue);
                            newMap.put(cellValue, value);
                        }
                        dataList.add(newMap);
                    }

                    writer.write(dataList);
                }
            }
        }
        return writer;
    }


    public static void addComment(Cell cell, String value) {
        Sheet sheet = cell.getSheet();
        ClientAnchor anchor = new XSSFClientAnchor();
        anchor.setDx1(0);
        anchor.setDx2(0);
        anchor.setDy1(0);
        anchor.setDy2(0);
        anchor.setCol1(cell.getColumnIndex());
        anchor.setRow1(cell.getRowIndex());
        anchor.setCol2(cell.getColumnIndex() + 5);
        anchor.setRow2(cell.getRowIndex() + 6);
        Drawing<?> drawing = sheet.createDrawingPatriarch();
        Comment comment = drawing.createCellComment(anchor);
        comment.setString(new XSSFRichTextString(value));
        cell.setCellComment(comment);
    }

    public static void defaultStyle(ExcelWriter writer) {
        Font font = writer.createFont();
        font.setFontHeightInPoints((short) 11);
        // 取消数据的黑色边框以及数据左对齐
        CellStyle cellStyle = writer.getCellStyle();
        cellStyle.setFillBackgroundColor(IndexedColors.BLACK.getIndex());
        cellStyle.setBorderTop(BorderStyle.NONE);
        cellStyle.setBorderBottom(BorderStyle.NONE);
        cellStyle.setBorderLeft(BorderStyle.NONE);
        cellStyle.setBorderRight(BorderStyle.NONE);
        cellStyle.setAlignment(HorizontalAlignment.LEFT);
        // 取消数字格式的数据的黑色边框以及数据左对齐
        CellStyle cellStyleForNumber = writer.getStyleSet().getCellStyleForNumber();
        cellStyleForNumber.setBorderTop(BorderStyle.NONE);
        cellStyleForNumber.setBorderBottom(BorderStyle.NONE);
        cellStyleForNumber.setBorderLeft(BorderStyle.NONE);
        cellStyleForNumber.setBorderRight(BorderStyle.NONE);
        cellStyleForNumber.setAlignment(HorizontalAlignment.LEFT);
        cellStyleForNumber.setFillBackgroundColor(IndexedColors.BLACK.getIndex());
        cellStyleForNumber.setFont(font);
        // 取消日期格式的数据的黑色边框以及数据左对齐
        CellStyle cellStyleForDate = writer.getStyleSet().getCellStyleForDate();
        cellStyleForDate.setBorderTop(BorderStyle.NONE);
        cellStyleForDate.setBorderBottom(BorderStyle.NONE);
        cellStyleForDate.setBorderLeft(BorderStyle.NONE);
        cellStyleForDate.setBorderRight(BorderStyle.NONE);
        cellStyleForDate.setAlignment(HorizontalAlignment.LEFT);
        cellStyleForDate.setFillBackgroundColor(IndexedColors.BLACK.getIndex());
        DataFormat dataFormat = writer.getWorkbook().createDataFormat();
        short dateFormat = dataFormat.getFormat(DatePattern.NORM_DATE_PATTERN);
        cellStyleForDate.setDataFormat(dateFormat);
        cellStyleForDate.setFont(font);
        cellStyle.setFont(font);
    }

    public static CellStyle headerStyle(ExcelWriter writer) {
        // 取消数据的黑色边框以及数据左对齐
        CellStyle cellStyle = writer.getHeadCellStyle();
        cellStyle.setFillBackgroundColor(IndexedColors.BLACK.getIndex());
        cellStyle.setBorderTop(BorderStyle.NONE);
        cellStyle.setBorderBottom(BorderStyle.NONE);
        cellStyle.setBorderLeft(BorderStyle.NONE);
        cellStyle.setBorderRight(BorderStyle.NONE);
        cellStyle.setAlignment(HorizontalAlignment.LEFT);
        Font headerFont = writer.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 11);
        cellStyle.setFont(headerFont);
        return cellStyle;
    }
}
