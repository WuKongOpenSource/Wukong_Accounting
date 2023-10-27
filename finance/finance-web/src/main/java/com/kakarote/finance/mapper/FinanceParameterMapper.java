package com.kakarote.finance.mapper;

import com.alibaba.fastjson.JSONObject;
import com.kakarote.core.servlet.BaseMapper;
import com.kakarote.finance.entity.PO.FinanceParameter;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 系统参数设置 Mapper 接口
 * </p>
 *
 * @author zhangzhiwei
 * @since 2021-08-25
 */
public interface FinanceParameterMapper extends BaseMapper<FinanceParameter> {

    /**
     * 获取当前登录人系统参数
     */
    public JSONObject queryParameter(@Param("accountId") Long accountId);

}
