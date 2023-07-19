package com.atguigu.redpackage.util;

import java.math.BigDecimal;
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
    public static Integer[] splitRedPackageAlgorithm(BigDecimal totalMoney, int redPackageNumber) {
        Integer[] redPackageNumbers = new Integer[redPackageNumber];
//        //已经被抢夺的红包金额,已经被拆分塞进子红包的金额
//        int useMoney = 0;
//
//        for (int i = 0; i < redPackageNumber; i++) {
//            if (i == redPackageNumber - 1) {
//                redPackageNumbers[i] = totalMoney - useMoney;
//            } else {
//                //二倍均值算法，每次拆分后塞进子红包的金额 = 随机区间(0,(剩余红包金额M ÷ 未被抢的剩余红包个数N) * 2)
//                int avgMoney = ((totalMoney - useMoney) / (redPackageNumber - i)) * 2;
//                redPackageNumbers[i] = 1 + new Random().nextInt(avgMoney - 1);
//            }
//            useMoney = useMoney + redPackageNumbers[i];
//        }
        return redPackageNumbers;
    }
}
