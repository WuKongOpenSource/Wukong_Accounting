package com.kakarote.finance.entity.BO;

import com.kakarote.core.entity.PageEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * @ClassName: FinanceInitialPageBO
 * @Author: Blue
 * @Description: FinanceInitialPageBO
 * @Date: 2021/9/27 16:47
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "凭证列表筛选BO", description = "凭证列表筛选表")
@ToString
public class FinanceInitialPageBO extends PageEntity {

    @ApiModelProperty(value = "科目类型 1.资产 2.负债 3.权益 4.成本 5.损益")
    private Integer subjectType;

    @ApiModelProperty(value = "账套ID")
    private Long accountId;
}
