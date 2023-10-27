package com.kakarote.finance.service.impl;

import com.kakarote.core.servlet.BaseServiceImpl;
import com.kakarote.finance.entity.PO.FinanceStatementSubject;
import com.kakarote.finance.mapper.FinanceStatementSubjectMapper;
import com.kakarote.finance.service.IFinanceStatementSubjectService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 结账和科目关联表（除结转损益类型） 服务实现类
 * </p>
 *
 * @author zhangzhiwei
 * @since 2021-08-27
 */
@Service
public class FinanceStatementSubjectServiceImpl extends BaseServiceImpl<FinanceStatementSubjectMapper, FinanceStatementSubject> implements IFinanceStatementSubjectService {

}
