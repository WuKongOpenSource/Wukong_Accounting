package com.kakarote.finance.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.kakarote.core.servlet.BaseServiceImpl;
import com.kakarote.finance.common.AccountSet;
import com.kakarote.finance.entity.PO.FinanceTemplate;
import com.kakarote.finance.mapper.FinanceTemplateMapper;
import com.kakarote.finance.service.IFinanceTemplateService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author zhangzhiwei
 * @since 2021-08-24
 */
@Service
public class FinanceTemplateServiceImpl extends BaseServiceImpl<FinanceTemplateMapper, FinanceTemplate> implements IFinanceTemplateService {

    @Override
    public void saveAndUpdate(FinanceTemplate template) {
        if (template.getTemplateId() == null) {
            template.setAccountId(AccountSet.getAccountSetId());
            save(template);
        } else {
            updateById(template);
        }
    }

    @Override
    public List<JSONObject> queryList() {
        return getBaseMapper().queryList(AccountSet.getAccountSetId());
    }

    @Override
    public void deleteById(Long id) {
        removeById(id);
    }
}
