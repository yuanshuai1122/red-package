package vip.yuanshuai.redpackage.util;

import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

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

    /**
     * 抢红包方法
     *
     * @param amount 总金额
     * @param min    每个红包最小值
     * @param num    红包数
     *///模拟抢红包过程
    public static List<BigDecimal> splitRedPackage(BigDecimal amount, BigDecimal min, BigDecimal num){
        List<BigDecimal> split = new ArrayList<>();
        BigDecimal remain = amount.subtract(min.multiply(num));
        final Random random = new Random();
        final BigDecimal hundred = new BigDecimal("100");
        final BigDecimal two = new BigDecimal("2");
        BigDecimal sum = BigDecimal.ZERO;
        BigDecimal redpeck;
        for (int i = 0; i < num.intValue(); i++) {
            final int nextInt = random.nextInt(100);
            if(i == num.intValue() -1){
                redpeck = remain;
            }else{
                //RoundingMode.CEILING：取右边最近的整数
                //RoundingMode.FLOOR：取左边最近的正数
                redpeck = new BigDecimal(nextInt).multiply(remain.multiply(two).divide(num.subtract(new BigDecimal(i)),2, RoundingMode.CEILING)).divide(hundred,2, RoundingMode.FLOOR);
            }
            if(remain.compareTo(redpeck) > 0){
                remain = remain.subtract(redpeck);
            }else{
                remain = BigDecimal.ZERO;
            }
            sum = sum.add(min.add(redpeck));
            // 添加到List
            split.add(min.add(redpeck));
        }
        Assert.isTrue(compare(amount, sum), "切分红包出现异常");
        return split;
    }

    private static boolean compare(BigDecimal a, BigDecimal b){
        if(a.compareTo(b) == 0){
            return true;
        }
        return false;
    }

    /**
     * 生成UUID
     *
     * @return {@link String}
     */
    public static String generateUUID() {
        return UUID.randomUUID().toString();
    }

}
