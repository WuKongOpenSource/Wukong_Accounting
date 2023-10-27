package com.kakarote.finance.entity.BO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

/**
 * @ClassName: FinanceCertificateInsertBO
 * @Author: Blue
 * @Description: FinanceCertificateInsertBO
 * @Date: 2021/8/31 10:37
 */
@Data
@ToString
@ApiModel("凭证插入对象")
public class FinanceCertificateInsertBO {

    @ApiModelProperty(value = "凭证日期")
    private String certificateTime;

    @ApiModelProperty(value = "凭证字id")
    private Long voucherId;

    @ApiModelProperty(value = "要移动的凭证号")
    private Integer moveCertificateNum;

    @ApiModelProperty(value = "要移动到的凭证号")
    private Integer insertCertificateNum;
}
