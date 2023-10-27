package com.kakarote.finance.entity.BO;

import com.baomidou.mybatisplus.annotation.TableField;
import com.kakarote.finance.entity.PO.FinanceCertificateDetail;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @ClassName: FinanceCertificateBO
 * @Author: Blue
 * @Description: FinanceCertificateBO
 * @Date: 2021/8/25 14:40
 */
@Data
@ToString
@ApiModel("凭证保存对象")
public class FinanceCertificateBO implements Serializable {

    @ApiModelProperty(value = "凭证id")
    private Long certificateId;

    @ApiModelProperty(value = "凭证字id")
    private Long voucherId;

    @ApiModelProperty(value = "凭证号")
    private Integer certificateNum;

    @ApiModelProperty(value = "凭证日期")
    private String certificateTime;

    @ApiModelProperty(value = "批次 比如附件批次")
    private String batchId;

    @ApiModelProperty(value = "附单据张数")
    private Integer fileNum;

    @ApiModelProperty(value = "合计借方金额")
    private BigDecimal debtorBalance;

    @ApiModelProperty(value = "合计贷方金额")
    private BigDecimal creditBalance;

    @ApiModelProperty(value = "合计")
    private String total;


    @TableField(exist = false)
    @ApiModelProperty(value = "凭证详情")
    private List<FinanceCertificateDetail> certificateDetails;
}
