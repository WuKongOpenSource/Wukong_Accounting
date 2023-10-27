package com.kakarote.finance.entity.VO;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.util.Date;

@Data
@ToString
@ApiModel("切换账套列表")
public class FinanceAccountListVO {

    @ApiModelProperty(value = "账套ID")
    private Long accountId;

    @ApiModelProperty("账套名称")
    private String companyName;

    @ApiModelProperty(value = "启用期间")
    private Date enableTime;

    @ApiModelProperty(value = "结账时间")
    private Date startTime;

    @ApiModelProperty(value = "是否是默认账套（0 不是，1 是）")
    private Integer isDefault;

    @ApiModelProperty(value = "是否有账套（0 没有  1 有）")
    private Integer status;

    @ApiModelProperty(value = "联系人")
    private String contacts;

    @ApiModelProperty(value = "手机号码")
    private String mobile;
}
