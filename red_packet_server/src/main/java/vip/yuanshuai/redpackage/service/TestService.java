package vip.yuanshuai.redpackage.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import vip.yuanshuai.redpackage.util.RedPackageUtil;

import java.math.BigDecimal;
import java.util.Arrays;

/**
 * @program: red-package
 * @description: 测试服务
 * @author: yuanshuai
 * @create: 2023-07-20 09:47
 **/
@Service
@Slf4j
public class TestService implements CommandLineRunner {


    @Override
    public void run(String... args) throws Exception {
        BigDecimal[] bigDecimals = RedPackageUtil.splitRedPackageAlgorithm(new BigDecimal("200"), 5);
        BigDecimal bigDecimal = new BigDecimal("0");
        for (BigDecimal decimal : bigDecimals) {
            bigDecimal.add(decimal);
        }
        log.info(String.valueOf(bigDecimal));
        log.info(Arrays.toString(bigDecimals));
    }
}
