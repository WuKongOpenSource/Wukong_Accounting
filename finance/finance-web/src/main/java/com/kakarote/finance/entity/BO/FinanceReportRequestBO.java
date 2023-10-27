package com.kakarote.finance.entity.BO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * @author 10323
 * @description: 报表请求BO
 * @date 2021/8/289:51
 */
@Data
@ToString
@ApiModel("报表请求BO")
public class FinanceReportRequestBO {

    @ApiModelProperty(value = "1 月报 2 季报")
    private Integer type;

    @NotNull
    @ApiModelProperty(value = "开始账期：格式 yyyyMM")
    private String fromPeriod;

    @NotNull
    @ApiModelProperty(value = "结束账期：格式 yyyyMM")
    private String toPeriod;

    @ApiModelProperty(value = "现金流量表分类")
    private Integer category;

    @ApiModelProperty(value = "表达式")
    private String expression;

    @ApiModelProperty(value = "账套ID")
    private Long accountId;

    @ApiModelProperty(value = "是否获取上期")
    private boolean queryLastPeriod;

    @ApiModelProperty(value = "是否保存")
    private boolean toSave;

    @ApiModelProperty(value = "导出资产负债表用")
    private List<Map<String, Object>> exportDataList;
}
