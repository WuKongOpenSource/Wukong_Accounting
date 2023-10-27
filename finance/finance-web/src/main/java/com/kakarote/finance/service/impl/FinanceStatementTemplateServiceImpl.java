package com.kakarote.finance.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.util.IOUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kakarote.common.log.entity.OperationLog;
import com.kakarote.core.servlet.BaseServiceImpl;
import com.kakarote.core.utils.BaseUtil;
import com.kakarote.finance.common.AccountSet;
import com.kakarote.finance.common.CashFlowStatementReportHolder;
import com.kakarote.finance.entity.BO.FinanceStatementTemplateBO;
import com.kakarote.finance.entity.PO.FinanceStatementTemplate;
import com.kakarote.finance.entity.PO.FinanceStatementTemplateSubject;
import com.kakarote.finance.entity.PO.FinanceSubject;
import com.kakarote.finance.mapper.FinanceStatementTemplateMapper;
import com.kakarote.finance.service.IFinanceInitService;
import com.kakarote.finance.service.IFinanceStatementTemplateService;
import com.kakarote.finance.service.IFinanceStatementTemplateSubjectService;
import com.kakarote.finance.service.IFinanceSubjectService;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 结账模板表 服务实现类
 * </p>
 *
 * @author zhangzhiwei
 * @since 2021-08-28
 */
@Service("statementTemplateService")
public class FinanceStatementTemplateServiceImpl extends BaseServiceImpl<FinanceStatementTemplateMapper, FinanceStatementTemplate>
        implements IFinanceStatementTemplateService, IFinanceInitService {

    @Autowired
    private IFinanceSubjectService financeSubjectService;

    @Autowired
    private IFinanceStatementTemplateSubjectService templateSubjectService;

    @Override
    public List<FinanceStatementTemplate> queryListByType(Integer type) {
        List<FinanceStatementTemplate> templates = lambdaQuery()
                .eq(FinanceStatementTemplate::getAccountId, AccountSet.getAccountSetId())
                .eq(FinanceStatementTemplate::getTemplateType, type).list();
        for (FinanceStatementTemplate template : templates) {
            Map<String, String> keyMap = new HashMap<>();
            keyMap.put("digestContent_resourceKey", "finance.config.statement.template." + template.getDigestContent());
            template.setLanguageKeyMap(keyMap);
        }
        return templates;
    }

    @Override
    public List<JSONObject> querySubjectList(Long id) {
        List<JSONObject> list = getBaseMapper().querySubjectList(id, AccountSet.getAccountSetId());
        for (JSONObject json : list) {
            FinanceSubject subject = financeSubjectService.getById(json.getLong("subjectId"));
            Map<String, String> subjectMap = new HashMap<>();
            subjectMap.put("subjectName_resourceKey", "finance.subject." + subject.getSubjectName());
            subject.setLanguageKeyMap(subjectMap);
            json.put("subject", subject);
            Map<String, String> keyMap = new HashMap<>();
            keyMap.put("digestContent_resourceKey", "finance.config.statement.template." + json.getString("digestContent"));
            json.put("languageKeyMap", keyMap);
        }
        return list;
    }

    @Override
    public OperationLog saveAndUpdate(FinanceStatementTemplateBO templateBO) {
        FinanceStatementTemplate template = BeanUtil.copyProperties(templateBO, FinanceStatementTemplate.class);

        OperationLog operationLog = new OperationLog();
        if (template.getTemplateId() == null) {
            template.setAccountId(AccountSet.getAccountSetId());
            save(template);
            operationLog.setOperationObject(template.getDigestContent());
            operationLog.setOperationInfo("新建期末结转凭证模板：" + template.getDigestContent());
        } else {
            updateById(template);
            operationLog.setOperationObject(template.getDigestContent());
            operationLog.setOperationInfo("编辑期末结转凭证模板：" + template.getDigestContent());
        }

        LambdaQueryWrapper<FinanceStatementTemplateSubject> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FinanceStatementTemplateSubject::getTemplateId, template.getTemplateId())
                .eq(FinanceStatementTemplateSubject::getAccountId, AccountSet.getAccountSetId());
        templateSubjectService.remove(wrapper);
        if (templateBO.getTemplateSubjects() != null && templateBO.getTemplateSubjects().size() > 0) {
            for (FinanceStatementTemplateSubject templateSubject : templateBO.getTemplateSubjects()) {
                templateSubject.setTemplateId(template.getTemplateId());
                templateSubject.setAccountId(AccountSet.getAccountSetId());
                templateSubjectService.save(templateSubject);
            }
        }
        return operationLog;
    }

    @Override
    public void importTemplate(Sheet sheet) {

    }

    @Override
    public void init() {
        String str = null;
        ClassPathResource classPathResource = new ClassPathResource("excelTemplates/statement.json");
        try {
            InputStream inputStream = classPathResource.getInputStream();
            str = IoUtil.read(inputStream,Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<FinanceStatementTemplateBO> list = JSONArray.parseArray(str, FinanceStatementTemplateBO.class);
        if (list == null || list.isEmpty()) {
            return;
        }
        List<FinanceStatementTemplate> statementTemplateList = new ArrayList<>();
        List<FinanceStatementTemplateSubject> templateSubjectList = new ArrayList<>();
        Map<String, FinanceSubject> subjectMap = CashFlowStatementReportHolder.getSubjects().stream().collect(Collectors.toMap(FinanceSubject::getNumber, Function.identity(), (k1, k2) -> k1));
        for (FinanceStatementTemplateBO bo : list) {
            FinanceStatementTemplate template = BeanUtil.copyProperties(bo, FinanceStatementTemplate.class);
            template.setTemplateId(BaseUtil.getNextId());
            template.setAccountId(AccountSet.getAccountSetId());
            statementTemplateList.add(template);
            for (FinanceStatementTemplateSubject templateSubject : bo.getTemplateSubjects()) {
                FinanceSubject subject = subjectMap.get(templateSubject.getNumber());
                templateSubject.setTemplateSubjectId(BaseUtil.getNextId());
                templateSubject.setDigestContent(template.getDigestContent());
                templateSubject.setSubjectId(subject.getSubjectId());
                templateSubject.setTemplateId(template.getTemplateId());
                templateSubject.setAccountId(AccountSet.getAccountSetId());
                templateSubjectList.add(templateSubject);
            }
        }
        saveBatch(statementTemplateList);
        templateSubjectService.saveBatch(templateSubjectList);
    }
}
