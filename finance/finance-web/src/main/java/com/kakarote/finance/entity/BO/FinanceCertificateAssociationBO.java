package com.kakarote.finance.entity.BO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

/**
 * <p>
 * 凭证详情关联类型表
 * </p>
 *
 * @author dsc
 * @since 2021-09-02
 */
@Data
@ToString
@ApiModel(value = "FinanceCertificateAssociation对象", description = "凭证详情关联类型表")
public class FinanceCertificateAssociationBO {

    @ApiModelProperty(value = "会计期间开始时间")
    private String startTime;

    @ApiModelProperty(value = "会计期间结束时间")
    private String endTime;

    @ApiModelProperty(value = "辅助核算id")
    private Long adjuvantId;

    @ApiModelProperty(value = "账套ID")
    private Long accountId;
}
