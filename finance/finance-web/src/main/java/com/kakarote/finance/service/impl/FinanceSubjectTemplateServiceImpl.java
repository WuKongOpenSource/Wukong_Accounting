package com.kakarote.finance.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.kakarote.common.utils.UserUtil;
import com.kakarote.core.servlet.ApplicationContextHolder;
import com.kakarote.core.servlet.BaseServiceImpl;
import com.kakarote.core.utils.BaseUtil;
import com.kakarote.finance.common.AccountSet;
import com.kakarote.finance.common.CashFlowStatementReportHolder;
import com.kakarote.finance.constant.FinanceBalanceDirectionEnum;
import com.kakarote.finance.constant.FinanceCategoryEnum;
import com.kakarote.finance.entity.PO.FinanceSubject;
import com.kakarote.finance.entity.PO.FinanceSubjectTemplate;
import com.kakarote.finance.mapper.FinanceSubjectTemplateMapper;
import com.kakarote.finance.service.IFinanceInitService;
import com.kakarote.finance.service.IFinanceSubjectService;
import com.kakarote.finance.service.IFinanceSubjectTemplateService;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * @description:科目模板
 * @author: zjj
 * @date: 2021-08-31 10:14
 */
@Service("subjectTemplateService")
public class FinanceSubjectTemplateServiceImpl extends BaseServiceImpl<FinanceSubjectTemplateMapper, FinanceSubjectTemplate> implements IFinanceSubjectTemplateService, IFinanceInitService {

    @Autowired
    private IFinanceSubjectService subjectService;

    @Override
    public List<FinanceSubjectTemplate> queryList() {
        return baseMapper.queryList();
    }

    @Override
    public void importTemplate(Sheet sheet) {
        List<String> headers = Arrays.asList("编码", "名称", "类别", "余额方向", "是否现金科目", "状态");
        Row headerRow = sheet.getRow(0);
        Map<String, Integer> headIndexMap = getHeadIndexMap(headers, headerRow);
        Map<String, FinanceSubjectTemplate> numberSubjectMap = new HashMap<>();
        List<FinanceSubjectTemplate> subjects = new ArrayList<>();
        // 先删除原有配置
        lambdaUpdate().remove();
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);

            String number = row.getCell(MapUtil.getInt(headIndexMap, "编码")).getStringCellValue().trim();
            String name = row.getCell(MapUtil.getInt(headIndexMap, "名称")).getStringCellValue().trim();
            String category = row.getCell(MapUtil.getInt(headIndexMap, "类别")).getStringCellValue().trim();
            String balanceDirection = row.getCell(MapUtil.getInt(headIndexMap, "余额方向")).getStringCellValue().trim();
            String isCash = row.getCell(MapUtil.getInt(headIndexMap, "是否现金科目")).getStringCellValue().trim();
            String status = row.getCell(MapUtil.getInt(headIndexMap, "状态")).getStringCellValue().trim();
            FinanceCategoryEnum categoryEnum = FinanceCategoryEnum.parseName(category);

            FinanceSubjectTemplate subject = new FinanceSubjectTemplate();
            subject.setNumber(number);
            subject.setSubjectName(name);
            subject.setType(categoryEnum.getType());
            subject.setCategory(categoryEnum.getCategory());
            subject.setBalanceDirection(FinanceBalanceDirectionEnum.parseName(balanceDirection).getType());
            subject.setIsCash(StrUtil.equals("是", isCash) ? 1 : 0);
            subject.setStatus(StrUtil.equals("启用", status) ? 1 : 0);
            subject.setGrade(1);
            subjects.add(subject);
        }
        subjects.sort(Comparator.comparing(FinanceSubjectTemplate::getNumber));
        for (FinanceSubjectTemplate subject : subjects) {
            int length = subject.getNumber().length();
            int four = 4;
            if (length > 4) {
                int index = length - 2;
                while (index >= four) {
                    FinanceSubjectTemplate pSubject = numberSubjectMap.get(StrUtil.sub(subject.getNumber(), 0, index));
                    if (ObjectUtil.isNotNull(pSubject)) {
                        subject.setParentId(pSubject.getId());
                        subject.setGrade(pSubject.getGrade() + 1);
                        break;
                    }
                    index -= 2;
                }
            }
            save(subject);
            numberSubjectMap.put(subject.getNumber(), subject);
        }
    }

    @Override
    public void init() {
        List<FinanceSubjectTemplate> templates = baseMapper.queryList();
        if (CollUtil.isEmpty(templates)) {
            return;
        }
        subjectService.lambdaUpdate()
                .eq(FinanceSubject::getAccountId, AccountSet.getAccountSetId())
                .remove();
        Map<Long, Long> sourceNewIdMap = new HashMap<>(templates.size());
        List<FinanceSubject> subjectList = new ArrayList<>();
        for (FinanceSubjectTemplate template : templates) {
            FinanceSubject subject = BeanUtil.copyProperties(template, FinanceSubject.class);
            subject.setSubjectId(BaseUtil.getNextId());
            subject.setAccountId(AccountSet.getAccountSetId());
            subject.setCreateUserId(UserUtil.getUserId());
            subject.setCreateTime(LocalDateTime.now());
            if (ObjectUtil.notEqual(0L, template.getParentId())) {
                subject.setParentId(sourceNewIdMap.get(template.getParentId()));
            } else {
                subject.setParentId(0L);
            }
            subjectList.add(subject);
            sourceNewIdMap.put(template.getId(), subject.getSubjectId());
        }
        subjectService.saveBatch(subjectList);
        //优化初始化速度
        CashFlowStatementReportHolder.setSubjects(subjectList);
        //初始化现金流量表
        IFinanceInitService statementService = ApplicationContextHolder.getBean("statementService");
        statementService.init();
    }
}
