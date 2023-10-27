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
 * 币种
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
@TableName("wk_finance_currency")
@ApiModel(value = "FinanceCurrency对象", description = "币种")
public class FinanceCurrency implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("币种id")
    @JsonSerialize(using = ToStringSerializer.class)
    @TableId(value = "currency_id", type = IdType.ASSIGN_ID)
    private Long currencyId;

    @ApiModelProperty("编码")
    private String currencyCoding;

    @ApiModelProperty("币种名称")
    private String currencyName;

    @ApiModelProperty("汇率")
    private BigDecimal exchangeRate;

    @ApiModelProperty("是否是本币位")
    private Integer homeCurrency;

    @ApiModelProperty("创建人")
    @TableField(fill = FieldFill.INSERT)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long createUserId;

    @ApiModelProperty("创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @ApiModelProperty("1.正常 3.删除")
    private Integer status;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long accountId;

    @ApiModelProperty("修改人")
    @TableField(fill = FieldFill.UPDATE)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long updateUserId;

    @ApiModelProperty("修改时间")
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;


}
