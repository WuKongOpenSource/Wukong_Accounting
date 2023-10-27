package com.kakarote.finance.entity.VO;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @ClassName: FinanceStatementSaveBO
 * @Author: Blue
 * @Description: FinanceStatementSaveBO
 * @Date: 2021/8/27 15:09
 */
@Data
@ToString
@ApiModel("结账获取信息VO")
public class FinanceStatementVO {

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime settleTime;

    @ApiModelProperty(value = "本期录入张数")
    private Integer number;

    @ApiModelProperty(value = "账套ID")
    private Long accountId;

    @ApiModelProperty(value = "结账返回")
    private List<JSONObject> statements;

}
