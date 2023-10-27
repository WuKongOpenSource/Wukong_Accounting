package com.kakarote.finance.mapper;

import com.alibaba.fastjson.JSONObject;
import com.kakarote.core.servlet.BaseMapper;
import com.kakarote.finance.entity.PO.FinanceTemplate;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author zhangzhiwei
 * @since 2021-08-24
 */
public interface FinanceTemplateMapper extends BaseMapper<FinanceTemplate> {

    /**
     * 查询凭证模板列表
     */
    public List<JSONObject> queryList(@Param("accountId") Long accountId);

}
