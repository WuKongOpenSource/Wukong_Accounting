package com.kakarote.finance.service;

import com.kakarote.core.servlet.BaseService;
import com.kakarote.finance.entity.PO.FinanceSubjectTemplate;

import java.util.List;

/**
 * @description:科目模板
 * @author: zjj
 * @date: 2021-08-31 10:13
 */
public interface IFinanceSubjectTemplateService extends BaseService<FinanceSubjectTemplate> {

    List<FinanceSubjectTemplate> queryList();
}
