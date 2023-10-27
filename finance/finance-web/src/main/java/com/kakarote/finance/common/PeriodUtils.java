package com.kakarote.finance.common;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 10323
 * @description: 账期工具类
 * @date 2021/8/2811:32
 */
public class PeriodUtils {

    public static final String PERIOD_FORMAT = "yyyyMM";

    public static final String ACCOUNT_FORMAT = "yyyy-MM-dd";

    public static String parsePeriod(Date date) {
        return DateUtil.format(date, PERIOD_FORMAT);
    }

    public static String previousPeriod(String period, int off) {
        Date date = DateUtil.parse(period, PERIOD_FORMAT);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + off);
        date = calendar.getTime();
        return parsePeriod(date);
    }

    public static String getFirstMonth(String period) {
        Date date = DateUtil.parse(period, PERIOD_FORMAT);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.MONTH, Calendar.JANUARY);
        date = calendar.getTime();
        return parsePeriod(date);
    }

    public static Date parseDate(String period) {
        return DateUtil.parse(period, ACCOUNT_FORMAT);
    }

    public static String getCurrentPeriod() {
        return parsePeriod(DateUtil.date());
    }

    /**
     * 获取当前账往前推一年的所有账期
     *
     * @return
     */
    public static List<String> getOneYearPeriod(String period) {
        List<String> periods = new ArrayList<>();
        int maxLength = -12;
        for (int off = 0; off > maxLength; off--) {
            periods.add(previousPeriod(period, off));
        }
        return periods.stream().sorted().collect(Collectors.toList());
    }

    /**
     * 获取账期间得所有账期
     *
     * @param fromPeriod
     * @param toPeriod
     * @return
     */
    public static List<String> getPeriodByFromTo(String fromPeriod, String toPeriod) {
        List<String> periods = new ArrayList<>();
        Date minDate = DateUtil.parse(fromPeriod, PERIOD_FORMAT);
        Date maxDate = DateUtil.parse(toPeriod, PERIOD_FORMAT);
        Calendar min = Calendar.getInstance();
        min.setTime(minDate);
        min.set(min.get(Calendar.YEAR), min.get(Calendar.MONTH), 1);
        Calendar max = Calendar.getInstance();
        max.setTime(maxDate);
        max.set(max.get(Calendar.YEAR), max.get(Calendar.MONTH), 2);
        Calendar current = min;
        while (current.before(max)) {
            periods.add(DateUtil.format(current.getTime(), PERIOD_FORMAT));
            current.add(Calendar.MONTH, 1);
        }
        return periods.stream().sorted().collect(Collectors.toList());
    }

    /**
     * a是否在b之后
     *
     * @param a
     * @param b
     * @return
     */
    public static Boolean isAfter(String a, String b) {
        if (ObjectUtil.equal(a, b)) {
            return true;
        }
        Date aDate = DateUtil.parse(a, PERIOD_FORMAT);
        Date bDate = DateUtil.parse(b, PERIOD_FORMAT);
        if (aDate.after(bDate)) {
            return true;
        }
        return false;
    }

}
