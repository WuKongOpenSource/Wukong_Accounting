package com.kakarote.finance.entity.BO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @ClassName: FinanceStatementCertificateBO
 * @Author: Blue
 * @Description: FinanceStatementCertificateBO
 * @Date: 2021/8/27 17:00
 */
@Data
@ToString
@ApiModel("结账生成凭证BO")
public class FinanceStatementCertificateBO {

    @ApiModelProperty(value = "生成凭证日期")
    private String certificateTime;

    @ApiModelProperty(value = "需要生成凭证的Ids")
    private List<Long> statementIds;

    @ApiModelProperty(value = "结账反结账 1.结账 2.反结账")
    private Integer type;

    @ApiModelProperty(value = "账套ID")
    private Long accountId;
}
