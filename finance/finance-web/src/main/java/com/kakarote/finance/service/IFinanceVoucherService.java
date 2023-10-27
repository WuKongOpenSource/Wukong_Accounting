package com.kakarote.finance.service;

import com.kakarote.common.log.entity.OperationLog;
import com.kakarote.core.servlet.BaseService;
import com.kakarote.finance.entity.PO.FinanceVoucher;

import java.util.List;

/**
 * <p>
 * 凭证字 服务类
 * </p>
 *
 * @author zhangzhiwei
 * @since 2021-08-24
 */
public interface IFinanceVoucherService extends BaseService<FinanceVoucher> {

    /**
     * 新建凭证字
     *
     * @return
     */
    public OperationLog saveAndUpdate(FinanceVoucher voucher);

    /**
     * 列表查询凭证字
     */
    public List<FinanceVoucher> queryList();

    /**
     * 删除凭证字
     */
    public OperationLog deleteById(Long id);

    /**
     * 凭证排序
     */
    public void sort(Long voucherStartId, Long voucherEndId);

}
