package com.kakarote.finance.entity.BO;

import com.kakarote.finance.entity.PO.FinanceStatementTemplateSubject;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @ClassName: FinanceStatementTemplateVO
 * @Author: Blue
 * @Description: FinanceStatementTemplateVO
 * @Date: 2021/8/28 15:53
 */
@Data
@ToString
@ApiModel("结账模板BO")
public class FinanceStatementTemplateBO {

    @ApiModelProperty(value = "模板id")
    private Long templateId;

    @ApiModelProperty(value = "凭证摘要")
    private String digestContent;

    @ApiModelProperty(value = "1.日常开支 2.采购销售 3.往来款（含个人借款）4.转账业务 5.高级结转")
    private Integer templateType;

    @ApiModelProperty(value = "是否期末转结 1.是 0.否")
    private Integer isEndOver;

    @ApiModelProperty(value = "转结科目id")
    private Long subjectId;

    @ApiModelProperty(value = "取数规则 1.余额2.借方余额3.贷方余额 4.借方发生额5.贷方发生额6.损益发生额")
    private Integer gainRule;

    @ApiModelProperty(value = "时间类型 1.期末 2.期初 3.年初")
    private Integer timeType;

    @ApiModelProperty(value = "模板和科目关联表")
    private List<FinanceStatementTemplateSubject> templateSubjects;
}
