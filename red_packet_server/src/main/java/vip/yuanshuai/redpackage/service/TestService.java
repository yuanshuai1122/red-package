package vip.yuanshuai.redpackage.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import vip.yuanshuai.redpackage.util.RedPackageUtil;

import java.math.BigDecimal;
import java.util.List;

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
        BigDecimal totalAmount = new BigDecimal("100");
        BigDecimal minAmount = new BigDecimal("0.01");
        BigDecimal nums = new BigDecimal("10");
        List<BigDecimal> bigDecimals = RedPackageUtil.splitRedPackage(totalAmount, minAmount, nums);
        System.out.println(bigDecimals);
    }
}
