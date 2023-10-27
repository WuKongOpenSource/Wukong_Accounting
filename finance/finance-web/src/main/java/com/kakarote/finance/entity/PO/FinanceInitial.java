package com.kakarote.finance.entity.PO;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 财务初始余额
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
@TableName("wk_finance_initial")
@ApiModel(value = "FinanceInitial对象", description = "财务初始余额")
public class FinanceInitial implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonSerialize(using = ToStringSerializer.class)
    @TableId(value = "initial_id", type = IdType.ASSIGN_ID)
    private Long initialId;

    @ApiModelProperty("科目id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long subjectId;

    @ApiModelProperty("是否是辅助核算")
    private Boolean isAssist;

    @ApiModelProperty("期初金额")
    private BigDecimal initialBalance = new BigDecimal("0");

    @ApiModelProperty("期初数量")
    private Integer initialNum;

    @ApiModelProperty("本年累计借方金额")
    private BigDecimal addUpDebtorBalance = new BigDecimal("0");

    @ApiModelProperty("本年累计借方数量")
    private Integer addUpDebtorNum;

    @ApiModelProperty("本年累计贷方金额")
    private BigDecimal addUpCreditBalance = new BigDecimal("0");

    @ApiModelProperty("本年累计贷方数量")
    private Integer addUpCreditNum;

    @ApiModelProperty("年初金额")
    private BigDecimal beginningBalance = new BigDecimal("0");

    @ApiModelProperty("年初数量")
    private Integer beginningNum;

    @ApiModelProperty("实际损益发生金额")
    private BigDecimal profitBalance = new BigDecimal("0");

    @ApiModelProperty("创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long accountId;

    @ApiModelProperty("创建人")
    @TableField(fill = FieldFill.INSERT)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long createUserId;

    @ApiModelProperty("修改人")
    @TableField(fill = FieldFill.UPDATE)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long updateUserId;

    @ApiModelProperty("修改时间")
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    @ApiModelProperty(value = "关联类型")
    private FinanceInitialAssist assist;

    @TableField(exist = false)
    @ApiModelProperty(value = "辅助核算类型 ID")
    private Long adjuvantId;

    @TableField(exist = false)
    @ApiModelProperty(value = "核算项 ID")
    private Long carteId;

}
