package com.kakarote.finance.entity.BO;

import com.kakarote.core.entity.PageEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.List;

/**
 * @ClassName: FinanceSearchCertificateBO
 * @Author: Blue
 * @Description: FinanceSearchCertificateBO
 * @Date: 2021/8/25 15:35
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "凭证列表筛选BO", description = "凭证列表筛选表")
@ToString
public class FinanceSearchCertificateBO extends PageEntity {

    @ApiModelProperty(value = "会计期间开始时间")
    private String startTime;

    @ApiModelProperty(value = "会计期间结束时间")
    private String endTime;

    @ApiModelProperty(value = "凭证字")
    private Long voucherId;

    @ApiModelProperty(value = "凭证号")
    private Integer certificateNum;

    @ApiModelProperty(value = "0待审核、1通过")
    private Integer checkStatus;

    @ApiModelProperty(value = "摘要内容")
    private String digestContent;

    @ApiModelProperty(value = "科目编码")
    private String subjectNumber;

    @ApiModelProperty(value = "最小金额")
    private BigDecimal minAmount;

    @ApiModelProperty(value = "最大金额")
    private BigDecimal maxAmount;

    @ApiModelProperty(value = "制单人")
    private String userName;

    @ApiModelProperty(value = "制单人")
    private Long userId;

    @ApiModelProperty(value = "币别")
    private Long currencyId;

    @ApiModelProperty(value = "科目id")
    private Long subjectId;

    @ApiModelProperty(value = "账套ID")
    private Long accountId;

    private List<Long> certificateIds;

    private List<Long> subjectIds;

}
