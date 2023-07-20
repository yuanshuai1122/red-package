package vip.yuanshuai.redpackage;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import vip.yuanshuai.redpackage.util.RedPackageUtil;

import java.math.BigDecimal;
import java.util.Arrays;

/**
 * @program: red-package
 * @description: 红包算法测试
 * @author: yuanshuai
 * @create: 2023-07-20 09:41
 **/
@SpringBootTest
public class RedPackageTest {


    /**
     * 测试拆分红包算法
     */
    @Test
    void testSplitRedPackageAlgorithm() {
        BigDecimal[] bigDecimals = RedPackageUtil.splitRedPackageAlgorithm(new BigDecimal("199"), 5);
        System.out.println(Arrays.toString(bigDecimals));
    }

}
