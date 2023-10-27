package com.kakarote.finance.common;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.poi.excel.BigExcelWriter;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.kakarote.common.upload.entity.UploadEntity;
import com.kakarote.core.common.MultipartFileUtil;
import com.kakarote.core.common.cache.AdminCacheKey;
import com.kakarote.core.common.enums.FieldEnum;
import com.kakarote.core.servlet.ApplicationContextHolder;
import com.kakarote.core.utils.BaseUtil;
import com.kakarote.core.utils.ExcelParseUtil;
import com.kakarote.finance.service.AdminFileService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class FinanceExcelParseUtil {


    /**
     * 财务统一导出数据模板
     */
    public static void exportExcelFinance(int x, List<? extends Map<String, Object>> dataList, FinanceExcelParseService excelParseService, List<?> list) {
        try {
            File file = FileUtil.file("excelData/" + IdUtil.simpleUUID() + ".xls" + (excelParseService.isXlsx() ? "x" : ""));
            BigExcelWriter writer = ExcelUtil.getBigWriter(file);
            if (ObjectUtil.isNotEmpty(excelParseService.addCompany())) {
                Font headFont = writer.createFont();
                headFont.setFontHeightInPoints((short) 22);
                CellStyle headCell = writer.createCellStyle();
                headCell.setFont(headFont);
                headCell.setAlignment(HorizontalAlignment.CENTER);
                writer.merge(0, 0, 0, list.size() - 1, excelParseService.getExcelName(), headCell);

                Font rowFont = writer.createFont();
                rowFont.setFontHeightInPoints((short) 11);
                CellStyle rowCell = writer.createCellStyle();
                rowCell.setFont(rowFont);
                rowCell.setAlignment(HorizontalAlignment.LEFT);
                writer.writeCellValue(0, 1, excelParseService.addCompany()).setStyle(rowCell, 0, 1);
                CellStyle rightCell = writer.createCellStyle();
                rightCell.setFont(rowFont);
                rightCell.setAlignment(HorizontalAlignment.RIGHT);
                if (ObjectUtil.isNotEmpty(excelParseService.addUnit())) {
                    writer.writeCellValue(x, 1, excelParseService.addPeriod()).setStyle(rightCell, x, 1);
                    writer.writeCellValue(list.size() - 1, 1, excelParseService.addUnit()).setStyle(rightCell, list.size() - 1, 1);
                } else {
                    writer.merge(1, 1, 1, list.size() - 1, excelParseService.addPeriod(), rightCell);
                }
                writer.setCurrentRow(2);
            }
            List<ExcelParseUtil.ExcelDataEntity> headList = excelParseService.parseData(list, false);
            Map<String, Integer> headMap = new HashMap<>(headList.size(), 1.0f);
            headList.forEach(head -> {
                writer.addHeaderAlias(head.getFieldName(), head.getName());
                if (!Arrays.asList(FieldEnum.AREA.getType(), FieldEnum.DETAIL_TABLE.getType()).contains(head.getType())) {
                    headMap.put(head.getFieldName(), head.getType());
                }
            });
            // 取消数据的黑色边框以及数据左对齐
            CellStyle cellStyle = writer.getCellStyle();
            cellStyle.setFillBackgroundColor(IndexedColors.BLACK.getIndex());
            cellStyle.setBorderTop(BorderStyle.THIN);
            cellStyle.setBorderBottom(BorderStyle.THIN);
            cellStyle.setBorderLeft(BorderStyle.THIN);
            cellStyle.setBorderRight(BorderStyle.THIN);
            Font defaultFont = writer.createFont();
            defaultFont.setFontHeightInPoints((short) 11);
            cellStyle.setFont(defaultFont);
            // 取消数字格式的数据的黑色边框以及数据左对齐
            CellStyle cellStyleForNumber = writer.getStyleSet().getCellStyleForNumber();
            cellStyleForNumber.setBorderTop(BorderStyle.THIN);
            cellStyleForNumber.setBorderBottom(BorderStyle.THIN);
            cellStyleForNumber.setBorderLeft(BorderStyle.THIN);
            cellStyleForNumber.setBorderRight(BorderStyle.THIN);
            cellStyleForNumber.setAlignment(HorizontalAlignment.RIGHT);
            cellStyleForNumber.setFillBackgroundColor(IndexedColors.BLACK.getIndex());
            cellStyleForNumber.setFont(defaultFont);
            //设置数字格式单元格为货币类型
            DataFormat format = writer.getWorkbook().createDataFormat();
            cellStyleForNumber.setDataFormat(format.getFormat(BuiltinFormats.getBuiltinFormat(4)));
            // 设置数据
            dataList.forEach(record -> excelParseService.getFunc().call(record, headMap));
            //设置行高以及列宽
            writer.setRowHeight(-1, 20);
            writer.setColumnWidth(-1, 30);
            //只保留别名中的字段
            writer.setOnlyAlias(true);
            if (dataList.isEmpty()) {
                Map<String, Object> record = new HashMap<>();
                headList.forEach(head -> record.put(head.getFieldName(), ""));
                writer.write(Collections.singletonList(record), true);
            } else {
                writer.write(dataList, true);
            }
            //自定义标题别名
            //response为HttpServletResponse对象
            HttpServletResponse response = BaseUtil.getResponse();
            response.setContentType("application/vnd.ms-excel;charset=utf-8");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("exportSize", dataList.size() + "");
            //test.xls是弹出下载对话框的文件名，不能为中文，中文请自行编码
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(excelParseService.getExcelName() + "信息", "utf-8") + ".xls" + (excelParseService.isXlsx() ? "x" : ""));
            ServletOutputStream out = response.getOutputStream();
            writer.flush(out);
            writer.close();
            out.close();

            MultipartFile multipartFile = MultipartFileUtil.getMultipartFile(file);
            UploadEntity uploadEntity = ApplicationContextHolder.getBean(AdminFileService.class).uploadTempFile(multipartFile, IdUtil.simpleUUID());
            //上传OSS
            BaseUtil.getRedis().setex(AdminCacheKey.UPLOAD_EXCEL_MESSAGE_PREFIX + "file:" + uploadEntity.getFileId(), 604800, uploadEntity.getPath());
            response.setHeader("fileData", uploadEntity.getFileId());

        } catch (Exception e) {
            log.error("导出错误：", e);
        }
    }


    public static abstract class FinanceExcelParseService {

        /**
         * 设置自定义数据处理方法
         *
         * @return func
         */
        public ExcelParseUtil.DataFunc getFunc() {
            return (record, headMap) -> {
            };
        }

        /**
         * 统一处理数据
         *
         * @param list        请求头数据
         * @param importExcel 是否是导入模板
         * @return 转化后的请求头数据
         */
        public List<ExcelParseUtil.ExcelDataEntity> parseData(List<?> list, boolean importExcel) {
            List<ExcelParseUtil.ExcelDataEntity> entities = list.stream().map(obj -> {
                if (obj instanceof ExcelParseUtil.ExcelDataEntity) {
                    return (ExcelParseUtil.ExcelDataEntity) obj;
                }
                return BeanUtil.copyProperties(obj, ExcelParseUtil.ExcelDataEntity.class);
            }).collect(Collectors.toList());
            if (importExcel) {
                entities.removeIf(head -> FinanceExcelParseUtil.removeFieldByType(head.getType()));
            } else {
                entities.removeIf(head -> FieldEnum.HANDWRITING_SIGN.getType().equals(head.getType()));
            }
            return entities;
        }

        /**
         * 获取excel表格名称
         *
         * @return 表格名称
         */
        public abstract String getExcelName();

        //公司名
        public abstract String addCompany();

        //会计区间
        public abstract String addPeriod();

        //币别
        public abstract String addUnit();

        /**
         * 是否是xlsx格式，xlsx导出会比xlx3倍左右，谨慎使用
         *
         * @return isXlsx
         */
        public boolean isXlsx() {
            return false;
        }
    }

    /**
     * 不支持导入的字段
     */
    private static final List<Integer> TYPE_LIST = Arrays.asList(FieldEnum.FILE.getType(), FieldEnum.CHECKBOX.getType(), FieldEnum.USER.getType(), FieldEnum.STRUCTURE.getType(),
            FieldEnum.AREA.getType(), FieldEnum.AREA_POSITION.getType(), FieldEnum.CURRENT_POSITION.getType(), FieldEnum.DATE_INTERVAL.getType(), FieldEnum.BOOLEAN_VALUE.getType(),
            FieldEnum.HANDWRITING_SIGN.getType(), FieldEnum.DESC_TEXT.getType(), FieldEnum.DETAIL_TABLE.getType(), FieldEnum.CALCULATION_FUNCTION.getType(), FieldEnum.TAG.getType(),
            FieldEnum.FIELD_GROUP.getType(), FieldEnum.SERIAL_NUMBER.getType(), FieldEnum.ATTENTION.getType()
    );


    /**
     * 删除不支持导入的字段
     *
     * @return true为不支持导入
     */
    public static boolean removeFieldByType(Integer type) {
        return TYPE_LIST.contains(type);
    }


    /**
     * 数据表的合并方法
     */
    @FunctionalInterface
    public interface ExcelMergeFunc {
        /**
         * 数据格式化方法
         *
         * @param writer    excel的writer对象
         * @param cellStyle 层样式
         * @param extraData 额外数据
         */
        void call(ExcelWriter writer, CellStyle cellStyle, Object extraData);
    }

}
