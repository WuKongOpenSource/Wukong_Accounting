package com.kakarote.finance.service;

import com.alibaba.fastjson.JSONObject;
import com.kakarote.common.log.entity.OperationLog;
import com.kakarote.core.servlet.BaseService;
import com.kakarote.finance.entity.PO.FinanceParameter;

/**
 * <p>
 * 系统参数设置 服务类
 * </p>
 *
 * @author zhangzhiwei
 * @since 2021-08-25
 */
public interface IFinanceParameterService extends BaseService<FinanceParameter> {

    /**
     * 获取当前登录人系统参数
     */
    public JSONObject queryParameter();

    /**
     * 获取账套的系统参数
     *
     * @return
     */
    FinanceParameter getParameter();

    /**
     * 编辑系统参数
     */
    public OperationLog updateParameter(FinanceParameter parameter);

    /**
     * 根据账期获取当前账期所属年的初始账期
     *
     * @param period
     * @return
     */
    String getYearInitialPeriod(String period);

    /**
     * 获取初始账期
     *
     * @return
     */
    String getStartPeriod();

}
