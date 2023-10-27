package com.kakarote.finance.service;

import com.kakarote.common.log.entity.OperationLog;
import com.kakarote.core.servlet.BaseService;
import com.kakarote.finance.entity.PO.FinanceCurrency;

import java.util.List;

/**
 * <p>
 * 币种 服务类
 * </p>
 *
 * @author zhangzhiwei
 * @since 2021-08-24
 */
public interface IFinanceCurrencyService extends BaseService<FinanceCurrency> {

    /**
     * 新建编辑币种
     *
     * @return
     */

    public OperationLog saveAndUpdate(FinanceCurrency currency);

    /**
     * 列表查询币种
     */
    public List<FinanceCurrency> queryList();

    public List<FinanceCurrency> queryListByAccountId(Long id);

    /**
     * 删除币种
     *
     * @return
     */
    public OperationLog deleteById(Long id);

    public String getCurrencyName(Long id);
}
