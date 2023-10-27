package com.kakarote.finance.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.kakarote.core.servlet.BaseServiceImpl;
import com.kakarote.finance.common.AccountSet;
import com.kakarote.finance.entity.PO.FinanceStatementCertificate;
import com.kakarote.finance.mapper.FinanceStatementCertificateMapper;
import com.kakarote.finance.service.IFinanceStatementCertificateService;
import com.kakarote.finance.service.IFinanceStatementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 结账和凭证关联表 服务实现类
 * </p>
 *
 * @author zhangzhiwei
 * @since 2021-08-27
 */
@Service
public class FinanceStatementCertificateServiceImpl extends BaseServiceImpl<FinanceStatementCertificateMapper, FinanceStatementCertificate> implements IFinanceStatementCertificateService {

    @Autowired
    private IFinanceStatementService statementService;

    @Override
    public FinanceStatementCertificate getByPeriod(String period, Long accountId) {
        return baseMapper.getByPeriod(period, accountId);
    }

    @Override
    public boolean lossSubjectSettled(String period) {
        List<JSONObject> lossDetail = statementService.queryLossDetailByPeriod(period);
        if (CollUtil.isNotEmpty(lossDetail)) {
            FinanceStatementCertificate certificate = baseMapper.getByPeriod(period,AccountSet.getAccountSetId());
            if (ObjectUtil.isNull(certificate)) {
                return false;
            }
        }
        return true;
    }
}
