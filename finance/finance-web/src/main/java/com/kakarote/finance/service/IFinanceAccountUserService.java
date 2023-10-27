package com.kakarote.finance.service;

import com.alibaba.fastjson.JSONObject;
import com.kakarote.core.servlet.BaseService;
import com.kakarote.finance.entity.PO.FinanceAccountUser;

/**
 * <p>
 * 账套员工对应关系表 服务类
 * </p>
 *
 * @author dsc
 * @since 2021-08-29
 */
public interface IFinanceAccountUserService extends BaseService<FinanceAccountUser> {

    JSONObject financeAuth();
}
