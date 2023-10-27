package com.kakarote.finance.entity.VO;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@Data
@ToString
@ApiModel("账套授权")
public class FinanceAccountVO {

    @ApiModelProperty("账套名称")
    private String companyName;

    @ApiModelProperty("员工")
    private List<Map<String, Object>> userList;
}
