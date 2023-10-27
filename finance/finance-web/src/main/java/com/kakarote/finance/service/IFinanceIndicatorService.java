package com.kakarote.finance.service;

import com.alibaba.fastjson.JSONObject;
import com.kakarote.core.servlet.BaseService;
import com.kakarote.finance.entity.PO.FinanceIndicator;
import com.kakarote.finance.entity.VO.FinanceIndicatorVO;

import java.util.List;
import java.util.Map;

/**
 * @author zjj
 * @ClassName IFinanceIndicatorService.java
 * @Description 财务指标
 * @createTime 2021-10-08
 */
public interface IFinanceIndicatorService extends BaseService<FinanceIndicator> {

    List<FinanceIndicatorVO> indicatorList();

    List<JSONObject> statistics(Long id);


    List<Map<String, Object>> getAllFieldLanguageRel();
}
