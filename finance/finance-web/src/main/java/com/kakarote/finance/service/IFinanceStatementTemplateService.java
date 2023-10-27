package com.kakarote.finance.service;

import com.alibaba.fastjson.JSONObject;
import com.kakarote.common.log.entity.OperationLog;
import com.kakarote.core.servlet.BaseService;
import com.kakarote.finance.entity.BO.FinanceStatementTemplateBO;
import com.kakarote.finance.entity.PO.FinanceStatementTemplate;

import java.util.List;

/**
 * <p>
 * 结账模板表 服务类
 * </p>
 *
 * @author zhangzhiwei
 * @since 2021-08-28
 */
public interface IFinanceStatementTemplateService extends BaseService<FinanceStatementTemplate> {

    /**
     * 根据type获取结账模板
     */
    public List<FinanceStatementTemplate> queryListByType(Integer type);

    /**
     * 根据模板id获取科目信息
     */
    public List<JSONObject> querySubjectList(Long id);

    /**
     * 新建或编辑模板
     */
    public OperationLog saveAndUpdate(FinanceStatementTemplateBO templateBO);
}
