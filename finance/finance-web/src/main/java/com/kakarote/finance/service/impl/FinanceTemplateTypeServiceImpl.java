package com.kakarote.finance.service.impl;

import com.kakarote.core.exception.CrmException;
import com.kakarote.core.servlet.BaseServiceImpl;
import com.kakarote.finance.common.AccountSet;
import com.kakarote.finance.constant.FinanceCodeEnum;
import com.kakarote.finance.entity.PO.FinanceTemplate;
import com.kakarote.finance.entity.PO.FinanceTemplateType;
import com.kakarote.finance.mapper.FinanceTemplateTypeMapper;
import com.kakarote.finance.service.IFinanceTemplateService;
import com.kakarote.finance.service.IFinanceTemplateTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 凭证模板类型 服务实现类
 * </p>
 *
 * @author zhangzhiwei
 * @since 2021-08-29
 */
@Service
public class FinanceTemplateTypeServiceImpl extends BaseServiceImpl<FinanceTemplateTypeMapper, FinanceTemplateType> implements IFinanceTemplateTypeService {

    @Autowired
    private IFinanceTemplateService templateService;

    @Override
    public void saveAndUpdate(FinanceTemplateType templateType) {
        if (templateType.getTypeId() == null) {
            templateType.setAccountId(AccountSet.getAccountSetId());
            save(templateType);
        } else {
            updateById(templateType);
        }
    }

    @Override
    public List<FinanceTemplateType> queryList() {
        return lambdaQuery()
                .eq(FinanceTemplateType::getAccountId, AccountSet.getAccountSetId()).list();
    }

    @Override
    public void deleteById(Long id) {
        List<FinanceTemplate> templates = templateService.lambdaQuery()
                .eq(FinanceTemplate::getTypeId, id)
                .eq(FinanceTemplate::getAccountId, AccountSet.getAccountSetId()).list();
        if (templates.size() > 0) {
            throw new CrmException(FinanceCodeEnum.FINANCE_TYPE_ERROR);
        }
        removeById(id);
    }
}
