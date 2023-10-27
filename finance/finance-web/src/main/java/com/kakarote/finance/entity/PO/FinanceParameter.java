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
 * 系统参数设置
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
@TableName("wk_finance_parameter")
@ApiModel(value = "FinanceParameter对象", description = "系统参数设置")
public class FinanceParameter implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonSerialize(using = ToStringSerializer.class)
    @TableId(value = "parameter_id", type = IdType.ASSIGN_ID)
    private Long parameterId;

    @ApiModelProperty("启用时间")
    private LocalDateTime startTime;

    @ApiModelProperty("会计制度id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long bookkeeperId;

    @ApiModelProperty("科目级次")
    private Integer level;

    @ApiModelProperty("科目编码长度 例：4-2-2")
    private String rule;

    @ApiModelProperty("账簿余额方向与科目方向相同 1.相同 2.不同")
    private Integer accountBookDirection;

    @ApiModelProperty("现金、银行存款科目赤字检查 1.是 2.否")
    private Integer deficitExamine;

    @ApiModelProperty("凭证审核后才允许结账 1.是 2.否")
    private Integer voucherExamine;

    @ApiModelProperty("生成折旧凭证后不能新增和修改以前期间的卡片 1.是 2.否")
    private Integer propertyUnable;

    @ApiModelProperty("纳税人名称")
    private String taxpayerName;

    @ApiModelProperty("纳税人识别号")
    private String taxpayerNumber;

    @ApiModelProperty("公司名称")
    private String companyName;

    @ApiModelProperty("币种id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long currencyId;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long accountId;

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


}
