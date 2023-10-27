package com.kakarote.finance.entity.BO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @ClassName: FinanceCollectCertificateBO
 * @Author: Blue
 * @Description: FinanceCollectCertificateBO
 * @Date: 2021/8/25 16:51
 */
@Data
@ToString
@ApiModel("凭证汇总查询对象")
public class FinanceCollectCertificateBO {

    @ApiModelProperty(value = "汇总时间开始时间")
    private String startTime;

    @ApiModelProperty(value = "汇总时间结束时间")
    private String endTime;

    @ApiModelProperty(value = "凭证字")
    private Long voucherId;

    @ApiModelProperty(value = "小凭证号")
    private Integer minCertificateNum;

    @ApiModelProperty(value = "大凭证号")
    private Integer maxCertificateNum;

    @ApiModelProperty(value = "小科目级次")
    private Integer minLevel;

    @ApiModelProperty(value = "大科目级次")
    private Integer maxLevel;

    @ApiModelProperty(value = "账套ID")
    private Long accountId;

    private List<Long> subjectIds;


}
