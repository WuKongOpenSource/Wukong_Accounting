package com.kakarote.finance.entity.PO;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * <p>
 * 结账模板表
 * </p>
 *
 * @author dsc
 * @since 2021-12-22
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TableName("wk_finance_statement_template")
@ApiModel(value = "FinanceStatementTemplate对象", description = "结账模板表")
public class FinanceStatementTemplate implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonSerialize(using = ToStringSerializer.class)
    @TableId(value = "template_id", type = IdType.ASSIGN_ID)
    private Long templateId;

    @ApiModelProperty("凭证摘要")
    private String digestContent;

    @ApiModelProperty("1.日常开支 2.采购销售 3.往来款（含个人借款）4.转账业务 5.高级结转")
    private Integer templateType;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long accountId;

    @ApiModelProperty("是否期末转结 1.是 0.否")
    private Integer isEndOver;

    @ApiModelProperty("取数规则 1.余额2.借方余额3.贷方余额 4.借方发生额5.贷方发生额6.损益发生额")
    private Integer gainRule;

    @ApiModelProperty("时间类型 1.期末 2.期初 3.年初")
    private Integer timeType;

    @ApiModelProperty("转结科目id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long subjectId;

    @ApiModelProperty("创建人")
    @TableField(fill = FieldFill.INSERT)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long createUserId;

    @ApiModelProperty("创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @ApiModelProperty("修改人")
    @TableField(fill = FieldFill.UPDATE)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long updateUserId;

    @ApiModelProperty("修改时间")
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "多语言key")
    @TableField(exist = false)
    private Map<String, String> languageKeyMap;

}
