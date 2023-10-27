package com.kakarote.finance.entity.BO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

/**
 * @ClassName: FinanceCertificateSettleBO
 * @Author: Blue
 * @Description: FinanceCertificateSettleBO
 * @Date: 2021/9/2 17:20
 */
@Data
@ToString
@ApiModel("凭证整理对象")
public class FinanceCertificateSettleBO {

    @ApiModelProperty(value = "整理凭证日期 yyyyMM 格式")
    private String settleTime;

    @ApiModelProperty(value = "凭证字id，不限不传")
    private Long voucherId;

    @ApiModelProperty(value = "凭证起始号，最小1")
    private Integer certificateNum;

    @ApiModelProperty(value = "1.按凭证号顺序补齐 2.按凭证日期重新排")
    private Integer type;

    @ApiModelProperty(value = "账套id")
    private Long accountId;
}
