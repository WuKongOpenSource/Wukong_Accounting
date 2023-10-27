package com.kakarote.finance.entity.VO;

import com.alibaba.fastjson.JSONObject;
import com.kakarote.finance.entity.PO.FinanceSubject;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @ClassName: FinanceDiversificationVO
 * @Author: Blue
 * @Description: FinanceDiversificationVO
 * @Date: 2021/9/3 15:12
 */
@Data
@ToString
@ApiModel("多栏账返回数据")
public class FinanceDiversificationVO {

    @ApiModelProperty(value = "科目")
    private List<FinanceSubject> subjects;

    @ApiModelProperty(value = "多栏账数据")
    private List<JSONObject> jsonObjects;
}
