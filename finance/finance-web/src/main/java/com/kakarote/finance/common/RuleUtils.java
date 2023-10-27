package com.kakarote.finance.common;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 规则工具类
 *
 * @author 10323
 * @date 2022/1/612:05
 */
public class RuleUtils {

    public static String transNumber(String number, String oldRule, String newRule) {
        if (ObjectUtil.isNull(number)) {
            return null;
        }
        if (ObjectUtil.notEqual(oldRule, newRule)) {
            List<Integer> oldRuleList = Arrays.stream(oldRule.split("-")).map(Integer::parseInt).collect(Collectors.toList());
            List<Integer> newRuleList = Arrays.stream(newRule.split("-")).map(Integer::parseInt).collect(Collectors.toList());
            if (CollUtil.isEmpty(oldRuleList) || CollUtil.isEmpty(newRuleList)) {
                return number;
            }

            StringBuilder sb = new StringBuilder(number);
            Integer numberLength = number.length();
            int dealLength = 0;
            int totalLength = 0;
            AtomicInteger i = new AtomicInteger(0);
            for (Integer o : oldRuleList) {
                if (o > numberLength) {
                    return number;
                }
                dealLength += o;
                if (dealLength > numberLength) {
                    return sb.toString();
                }
                int index = i.getAndIncrement();
                int n = newRuleList.get(index);
                totalLength += n;
                String str = generateStr("0", n - o);
                if (index == 0) {
                    sb.insert(1, str);
                } else {
                    sb.insert(totalLength - n, str);
                }
            }
            return sb.toString();
        }
        return number;
    }

    private static String generateStr(String s, Integer length) {
        return String.join("", Collections.nCopies(length, s));
    }

    /**
     * 根据规则补齐科目编码
     *
     * @param number
     * @param rule
     * @return
     */
    public static BigDecimal fillUpNumber(String number, String rule) {
        if (ObjectUtil.isEmpty(number)) {
            return BigDecimal.ZERO;
        }
        number = number.replace("_", "");
        if (StrUtil.isEmpty(rule)) {
            return new BigDecimal(number);
        }
        List<Integer> rules = Arrays.stream(rule.split("-")).map(Integer::parseInt).collect(Collectors.toList());
        Integer totalLength = rules.stream().mapToInt(Integer::intValue).sum();
        Integer numberLength = number.length();
        String str = generateStr("0", totalLength - numberLength);
        return new BigDecimal(number + str);
    }

    /**
     * 根据规则补齐最大科目编码
     *
     * @param number
     * @param rule
     * @return
     */
    public static BigDecimal fillUpMaxNumber(String number, String rule) {
        if (ObjectUtil.isEmpty(number)) {
            return BigDecimal.ZERO;
        }
        number = number.replace("_", "");
        if (StrUtil.isEmpty(rule)) {
            return new BigDecimal(number);
        }
        List<Integer> rules = Arrays.stream(rule.split("-")).map(Integer::parseInt).collect(Collectors.toList());
        Integer totalLength = rules.stream().mapToInt(Integer::intValue).sum();
        Integer numberLength = number.length();
        String str = generateStr("9", totalLength - numberLength);
        return new BigDecimal(number + str);
    }

    public static boolean zero(BigDecimal number) {
        return number.compareTo(BigDecimal.ZERO) == 0;
    }
}
