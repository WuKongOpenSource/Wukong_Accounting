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

/**
 * <p>
 * 结账表
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
@TableName("wk_finance_statement")
@ApiModel(value = "FinanceStatement对象", description = "结账表")
public class FinanceStatement implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonSerialize(using = ToStringSerializer.class)
    @TableId(value = "statement_id", type = IdType.ASSIGN_ID)
    private Long statementId;

    @ApiModelProperty("结账名称")
    private String statementName;

    @ApiModelProperty("是否期末转结 1.是 0.否")
    private Integer isEndOver;

    @ApiModelProperty("转结科目id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long subjectId;

    @ApiModelProperty("取数规则 1.余额2.借方余额3.贷方余额 4.借方发生额5.贷方发生额6.损益发生额")
    private Integer gainRule;

    @ApiModelProperty("时间类型 1.期末 2.期初 3.年初")
    private Integer timeType;

    @ApiModelProperty("凭证字")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long voucherId;

    @ApiModelProperty("凭证摘要")
    private String digestContent;

    @ApiModelProperty("凭证分类 1收益和损失分开结转 （分别生成收益凭证和损失凭证） 2.收益和损失同时结转")
    private Integer voucherType;

    @ApiModelProperty("“以前年度损益调整”科目")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long adjustSubjectId;

    @ApiModelProperty("“以前年度损益调整”科目的结转科目")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long endSubjectId;

    @ApiModelProperty("其他损益科目的结转科目")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long restSubjectId;

    @ApiModelProperty("结转方式：按余额反向结转 1.是 0.否")
    private Integer endWay;

    @ApiModelProperty("创建人")
    @TableField(fill = FieldFill.INSERT)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long createUserId;

    @ApiModelProperty("创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @ApiModelProperty("结账类型 1.普通结账 2.结账损益 3.转出未交增值税	4.计提地税 5.计提所得税")
    private Integer statementType;

    @ApiModelProperty("账套id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long accountId;

    @ApiModelProperty("结转损益日期，最大31，超过31默认最后一天")
    private Integer endTimeDays;

    @ApiModelProperty("修改人")
    @TableField(fill = FieldFill.UPDATE)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long updateUserId;

    @ApiModelProperty("修改时间")
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;


}
