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
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 初始余额辅助核算
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
@TableName("wk_finance_initial_assist")
@ApiModel(value = "FinanceInitialAssist对象", description = "初始余额辅助核算")
public class FinanceInitialAssist implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonSerialize(using = ToStringSerializer.class)
    @TableId(value = "assist_id", type = IdType.ASSIGN_ID)
    private Long assistId;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long initialId;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long subjectId;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long accountId;

    @ApiModelProperty("辅助核算辅助表id wk_finance_assist")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long financeAssistId;

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

    @TableField(exist = false)
    @ApiModelProperty(value = "关联类型")
    private List<FinanceInitialAssistAdjuvant> assistAdjuvantList = new ArrayList<>();

}
