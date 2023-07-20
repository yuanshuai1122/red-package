package com.atguigu.redpackage.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

/**
 * 拆红包的算法工具类
 * 拆红包的算法--->二倍均值算法
 */
public class RedPackageUtil {

    /**
     * 拆红包的算法--->二倍均值算法
     *
     * @param totalMoney
     * @param redPackageNumber
     * @return
     */
    public static BigDecimal[] splitRedPackageAlgorithm(BigDecimal totalMoney, int redPackageNumber) {
        BigDecimal[] redPackageNumbers = new BigDecimal[redPackageNumber];
        //已经被抢夺的红包金额,已经被拆分塞进子红包的金额
        BigDecimal useMoney = new BigDecimal("0");

        for (int i = 0; i < redPackageNumber; i++) {
            if (i == redPackageNumber - 1) {
                redPackageNumbers[i] = totalMoney.subtract(useMoney);
            } else {
                //二倍均值算法，每次拆分后塞进子红包的金额 = 随机区间(0,(剩余红包金额M ÷ 未被抢的剩余红包个数N) * 2)
                BigDecimal avgMoney = ((totalMoney.subtract(useMoney)).divide(new BigDecimal(String.valueOf(redPackageNumber - i)))).multiply(new BigDecimal("2"));
                redPackageNumbers[i] = new BigDecimal("1").add(new BigDecimal(String.valueOf(new Random().nextDouble((avgMoney.subtract(new BigDecimal("1")).doubleValue())))));
            }
            useMoney = useMoney.add(redPackageNumbers[i]);
        }
        return redPackageNumbers;
    }
}
