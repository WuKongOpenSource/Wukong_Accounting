package com.kakarote.finance.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.kakarote.common.log.entity.OperationLog;
import com.kakarote.common.utils.UserUtil;
import com.kakarote.core.exception.CrmException;
import com.kakarote.core.servlet.BaseServiceImpl;
import com.kakarote.finance.common.AccountSet;
import com.kakarote.finance.constant.FinanceCodeEnum;
import com.kakarote.finance.entity.PO.FinanceCertificate;
import com.kakarote.finance.entity.PO.FinanceVoucher;
import com.kakarote.finance.mapper.FinanceVoucherMapper;
import com.kakarote.finance.service.IFinanceCertificateService;
import com.kakarote.finance.service.IFinanceVoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 凭证字 服务实现类
 * </p>
 *
 * @author zhangzhiwei
 * @since 2021-08-24
 */
@Service
public class FinanceVoucherServiceImpl extends BaseServiceImpl<FinanceVoucherMapper, FinanceVoucher> implements IFinanceVoucherService {

    @Autowired
    private IFinanceCertificateService financeCertificateService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OperationLog saveAndUpdate(FinanceVoucher voucher) {
        OperationLog operationLog = new OperationLog();
        if (ObjectUtil.isNull(voucher.getVoucherId())) {
            voucher.setCreateUserId(UserUtil.getUserId());
            voucher.setCreateTime(LocalDateTime.now());
            voucher.setAccountId(AccountSet.getAccountSetId());
            if (ObjectUtil.equal(1, voucher.getIsDefault())) {
                lambdaUpdate().set(FinanceVoucher::getIsDefault, 0)
                        .eq(FinanceVoucher::getAccountId, AccountSet.getAccountSetId())
                        .update();
            }
            save(voucher);
            operationLog.setOperationObject(voucher.getVoucherName());
            operationLog.setOperationInfo("新建凭证字：" + voucher.getVoucherName());
        } else {
            boolean count = financeCertificateService.lambdaQuery().eq(FinanceCertificate::getVoucherId, voucher.getVoucherId()).exists();
            if (count) {
                throw new CrmException(FinanceCodeEnum.FINANCE_USED_VOUCHER_EDIT_ERROR);
            }
            if (ObjectUtil.equal(1, voucher.getIsDefault())) {
                lambdaUpdate().set(FinanceVoucher::getIsDefault, 0)
                        .eq(FinanceVoucher::getAccountId, AccountSet.getAccountSetId())
                        .update();
            }
            updateById(voucher);
            operationLog.setOperationObject(voucher.getVoucherName());
            operationLog.setOperationInfo("编辑凭证字：" + voucher.getVoucherName());
        }
        return operationLog;
    }

    @Override
    public List<FinanceVoucher> queryList() {
        List<FinanceVoucher> vouchers = lambdaQuery()
                .eq(FinanceVoucher::getAccountId, AccountSet.getAccountSetId())
                .orderByAsc(FinanceVoucher::getSort).list();
        if (CollectionUtil.isNotEmpty(vouchers)) {
            for (FinanceVoucher voucher : vouchers) {
                Map<String, String> keyMap = new HashMap<>();
                keyMap.put("voucherName_resourceKey", "finance.voucher." + voucher.getVoucherName());
                voucher.setLanguageKeyMap(keyMap);
            }
        }
        return vouchers;
    }

    @Override
    public OperationLog deleteById(Long id) {
        List<FinanceCertificate> certificates = financeCertificateService.lambdaQuery()
                .eq(FinanceCertificate::getVoucherId, id).list();
        if (certificates.size() > 0) {
            throw new CrmException(FinanceCodeEnum.FINANCE_VOUCHER_DELETE_ERROR);
        }
        OperationLog operationLog = new OperationLog();
        FinanceVoucher byId = getById(id);
        operationLog.setOperationObject(byId.getVoucherName());
        operationLog.setOperationInfo("删除凭证字：" + byId.getVoucherName());
        removeById(id);
        return operationLog;
    }

    @Override
    public void sort(Long voucherStartId, Long voucherEndId) {
        Integer sort;
        FinanceVoucher financeStartVoucher = getById(voucherStartId);
        FinanceVoucher financeEndVoucher = getById(voucherEndId);
        sort = financeStartVoucher.getSort();
        financeStartVoucher.setSort(financeEndVoucher.getSort());
        financeEndVoucher.setSort(sort);
        updateById(financeStartVoucher);
        updateById(financeEndVoucher);
    }


}
