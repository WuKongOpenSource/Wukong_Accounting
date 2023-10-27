package com.kakarote.finance.service;

import com.kakarote.core.servlet.BaseService;
import com.kakarote.finance.entity.PO.FinanceFlows;

import java.util.List;

/**
 * <p>
 * 现金流量初始余额 服务类
 * </p>
 *
 * @author zhangzhiwei
 * @since 2021-08-24
 */
public interface IFinanceFlowsService extends BaseService<FinanceFlows> {


    /**
     * 群体编辑现金流量初始余额
     */
    public void updateByIds(List<FinanceFlows> flows);

    /**
     * 查询现金流量初始余额
     */
    public List<FinanceFlows> queryList();
}
