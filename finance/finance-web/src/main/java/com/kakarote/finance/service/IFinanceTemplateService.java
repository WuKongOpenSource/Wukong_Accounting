package com.kakarote.finance.service;

import com.alibaba.fastjson.JSONObject;
import com.kakarote.core.servlet.BaseService;
import com.kakarote.finance.entity.PO.FinanceTemplate;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author zhangzhiwei
 * @since 2021-08-24
 */
public interface IFinanceTemplateService extends BaseService<FinanceTemplate> {

    /**
     * 新建凭证模板
     */
    public void saveAndUpdate(FinanceTemplate template);

    /**
     * 查询凭证模板列表
     */
    public List<JSONObject> queryList();

    /**
     * 删除凭证模板
     */
    public void deleteById(Long id);


}
