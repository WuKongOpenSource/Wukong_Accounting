package com.kakarote.finance.service;

import cn.hutool.core.util.StrUtil;
import com.kakarote.core.servlet.ApplicationContextHolder;
import com.kakarote.finance.constant.FinanceRuleEnum;
import com.kakarote.finance.entity.BO.FinanceFormulaBO;
import com.kakarote.finance.entity.PO.FinanceSubjectTemplate;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.*;


/**
 * 财务数据初始化service
 *
 * @author zhangzhiwei
 */
public interface IFinanceInitService {

    /**
     * 导入excel模板
     *
     * @param sheet
     */
    void importTemplate(Sheet sheet);

    /**
     * 初始化用户数据
     */
    default void init() {

    }

    /**
     * 将json字符串内容初始化到企业中
     *
     * @param sheet
     */
    default void init(String sheet) {

    }

    default Map<String, Integer> getHeadIndexMap(List<String> headers, Row headerRow) {
        Map<String, Integer> headIndexMap = new HashMap<>();
        Iterator<Cell> cellIterator = headerRow.cellIterator();
        while (cellIterator.hasNext()) {
            Cell cell = cellIterator.next();
            String value = cell.getStringCellValue();
            if (headers.contains(value)) {
                if (headIndexMap.containsKey(value.trim())) {
                    headIndexMap.put(value.trim() + "_1", cell.getColumnIndex());
                } else {
                    headIndexMap.put(value.trim(), cell.getColumnIndex());
                }
            }
        }
        return headIndexMap;
    }

    default List<FinanceFormulaBO> initFormulaBO(String number, FinanceRuleEnum ruleEnum) {
        IFinanceSubjectTemplateService subjectService = ApplicationContextHolder.getBean(IFinanceSubjectTemplateService.class);
        List<FinanceFormulaBO> formulaBOS = new ArrayList<>();
        String regex = ",";
        for (String nbr : number.split(regex)) {
            if (StrUtil.isEmpty(nbr)) {
                continue;
            }
            int index = nbr.indexOf("~");
            if (index > 0) {
                Integer start = Integer.valueOf(nbr.substring(0, index).trim());
                Integer end = Integer.valueOf(nbr.substring(index + 1).trim());
                for (int i = start; i <= end; i++) {
                    FinanceSubjectTemplate subject = subjectService.lambdaQuery().eq(FinanceSubjectTemplate::getNumber, i).one();
                    FinanceFormulaBO formulaBO = new FinanceFormulaBO();
                    formulaBO.setSubjectId(Optional.ofNullable(subject.getId()).map(id -> String.valueOf(id)).orElse(""));
                    formulaBO.setSubjectName(subject.getSubjectName());
                    formulaBO.setSubjectNumber(subject.getNumber());
                    formulaBO.setOperator("+");
                    formulaBO.setRules(ruleEnum.getType());
                    formulaBOS.add(formulaBO);
                }
            } else {
                FinanceSubjectTemplate subject = subjectService.lambdaQuery().eq(FinanceSubjectTemplate::getNumber, nbr).one();
                FinanceFormulaBO formulaBO = new FinanceFormulaBO();
                formulaBO.setSubjectId(Optional.ofNullable(subject.getId()).map(id -> String.valueOf(id)).orElse(""));
                formulaBO.setSubjectName(subject.getSubjectName());
                formulaBO.setSubjectNumber(subject.getNumber());
                formulaBO.setOperator("+");
                formulaBO.setRules(ruleEnum.getType());
                formulaBOS.add(formulaBO);
            }
        }
        return formulaBOS;
    }

    default Map<String, FinanceRuleEnum> parseRuleCell(String rule) {
        Map<String, FinanceRuleEnum> ruleMap = new HashMap<>();
        if (StrUtil.isNotEmpty(rule)) {
            List<String> rules = Arrays.asList(rule.split(","));
            for (String s : rules) {
                if (StrUtil.isEmpty(s)) {
                    continue;
                }
                int index = s.indexOf(":");
                String key = s.substring(0, index);
                String value = s.substring(index + 1);
                ruleMap.put(key, FinanceRuleEnum.parseName(value));
            }
        }
        return ruleMap;
    }
}
