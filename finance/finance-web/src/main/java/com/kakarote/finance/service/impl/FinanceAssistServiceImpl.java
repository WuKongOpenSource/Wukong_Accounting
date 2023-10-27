package com.kakarote.finance.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.kakarote.core.servlet.BaseServiceImpl;

import com.kakarote.finance.common.AccountSet;
import com.kakarote.finance.entity.PO.FinanceAssist;
import com.kakarote.finance.entity.PO.FinanceAssistAdjuvant;
import com.kakarote.finance.mapper.FinanceAssistMapper;
import com.kakarote.finance.service.IFinanceAssistAdjuvantService;
import com.kakarote.finance.service.IFinanceAssistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 辅助核算辅助表 服务实现类
 * </p>
 *
 * @author zhangzhiwei
 * @since 2021-10-14
 */
@Service
public class FinanceAssistServiceImpl extends BaseServiceImpl<FinanceAssistMapper, FinanceAssist> implements IFinanceAssistService {

    @Autowired
    private IFinanceAssistAdjuvantService financeAssistAdjuvantService;

    @Override
    public FinanceAssist saveAndUpdate(Long subjectId, List<FinanceAssistAdjuvant> assistAdjuvantList) {
        List<FinanceAssist> assistList = lambdaQuery().eq(FinanceAssist::getSubjectId, subjectId).list();
        for (FinanceAssist assist : assistList) {
            List<FinanceAssistAdjuvant> assistAdjuvants = financeAssistAdjuvantService.lambdaQuery().eq(FinanceAssistAdjuvant::getAssistId, assist.getAssistId()).list();
            if (assistAdjuvantList.size() == assistAdjuvants.size()) {
                Integer count = 0;
                for (FinanceAssistAdjuvant assistAdjuvant : assistAdjuvants) {
                    for (FinanceAssistAdjuvant adjuvant : assistAdjuvantList) {
                        if (ObjectUtil.equal(assistAdjuvant.getAdjuvantId(), adjuvant.getAdjuvantId())
                                && ObjectUtil.equal(assistAdjuvant.getRelationId(), adjuvant.getRelationId())) {
                            count = count + 1;
                        }
                    }
                }
                if (count == assistAdjuvants.size()) {
                    return assist;
                }
            }
        }
        FinanceAssist assist = new FinanceAssist();
        assist.setAccountId(AccountSet.getAccountSetId());
        assist.setSubjectId(subjectId);
        save(assist);
        for (FinanceAssistAdjuvant assistAdjuvant : assistAdjuvantList) {
            assistAdjuvant.setAccountId(AccountSet.getAccountSetId());
            assistAdjuvant.setAssistId(assist.getAssistId());
            financeAssistAdjuvantService.save(assistAdjuvant);
        }
        return assist;
    }
}
