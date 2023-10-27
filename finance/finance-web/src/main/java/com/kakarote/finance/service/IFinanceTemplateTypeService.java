package com.kakarote.finance.service;

import com.kakarote.core.servlet.BaseService;
import com.kakarote.finance.entity.PO.FinanceTemplateType;

import java.util.List;

/**
 * <p>
 * 凭证模板类型 服务类
 * </p>
 *
 * @author zhangzhiwei
 * @since 2021-08-29
 */
public interface IFinanceTemplateTypeService extends BaseService<FinanceTemplateType> {

    /**
     * 新建凭证模板类别
     */
    public void saveAndUpdate(FinanceTemplateType templateType);

    /**
     * 列表查询
     */
    public List<FinanceTemplateType> queryList();

    /**
     * 删除
     */
    public void deleteById(Long id);

}
