package com.kakarote.finance.service;

import com.kakarote.core.servlet.BaseService;
import com.kakarote.finance.entity.PO.FinanceAssist;
import com.kakarote.finance.entity.PO.FinanceAssistAdjuvant;

import java.util.List;

/**
 * <p>
 * 辅助核算辅助表 服务类
 * </p>
 *
 * @author zhangzhiwei
 * @since 2021-10-14
 */
public interface IFinanceAssistService extends BaseService<FinanceAssist> {

    public FinanceAssist saveAndUpdate(Long subjectId, List<FinanceAssistAdjuvant> assistAdjuvantList);

}
