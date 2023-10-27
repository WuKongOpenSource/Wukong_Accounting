package com.kakarote.finance.mapper;

import com.alibaba.fastjson.JSONObject;
import com.kakarote.core.servlet.BaseMapper;
import com.kakarote.finance.entity.PO.FinanceStatementTemplate;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 结账模板表 Mapper 接口
 * </p>
 *
 * @author zhangzhiwei
 * @since 2021-08-28
 */
public interface FinanceStatementTemplateMapper extends BaseMapper<FinanceStatementTemplate> {

    public List<JSONObject> querySubjectList(@Param("id") Long id, @Param("accountId") Long accountId);

}
