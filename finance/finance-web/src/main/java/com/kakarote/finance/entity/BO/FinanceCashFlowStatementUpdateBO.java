package com.kakarote.finance.entity.BO;


import com.kakarote.finance.entity.PO.FinanceCashFlowStatementReport;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@ToString
@ApiModel("现金流量表数据更新BO")
public class FinanceCashFlowStatementUpdateBO {

    @NotNull
    @ApiModelProperty(value = "数据")
    private List<FinanceCashFlowStatementReport> dataList;

    @ApiModelProperty(value = "1 月报 2 季报")
    private Integer type;

    @NotNull
    @ApiModelProperty(value = "开始账期：格式 yyyyMM")
    private String fromPeriod;

    @NotNull
    @ApiModelProperty(value = "结束账期：格式 yyyyMM")
    private String toPeriod;
}
