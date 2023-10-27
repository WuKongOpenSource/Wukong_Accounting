package com.kakarote.finance.entity.VO;

import com.kakarote.finance.entity.PO.FinanceIndicator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @author zjj
 * @ClassName FinanceIndicatorVO.java
 * @Description 财务指标 VO
 * @createTime 2021-10-08
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "FinanceIndicatorVO 对象", description = "财务指标")
public class FinanceIndicatorVO extends FinanceIndicator {

    @ApiModelProperty(value = "值")
    private BigDecimal value;
    @ApiModelProperty(value = "语言包map")
    private Map<String, String> languageKeyMap;
}
