package com.jianghao.limit;

import com.jianghao.limit.service.Limit;
import com.jianghao.limit.service.LimitType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ：jh
 * @date ：Created in 2020/6/27 17:24
 * @description：
 */

@RestController
public class LimiterController {

    private static final AtomicInteger ATOMIC_INTEGER_1 = new AtomicInteger();
    private static final AtomicInteger ATOMIC_INTEGER_2 = new AtomicInteger();
    private static final AtomicInteger ATOMIC_INTEGER_3 = new AtomicInteger();

    /**
     * @param
     * @return int
     * @Description:
     */
    @Limit(key = "limitTest", period = 30, count = 3)
    @GetMapping("/limitTest1")
    public int testLimiter1() {

        return ATOMIC_INTEGER_1.incrementAndGet();
    }

    /**
     * @param
     * @return int
     * @Description:
     */
    @Limit(key = "customer_limit_test", period = 30, count = 3, limitType = LimitType.CUSTOMER)
    @GetMapping("/limitTest2")
    public int testLimiter2() {

        return ATOMIC_INTEGER_2.incrementAndGet();
    }

    /**
     * @param
     * @return int
     * @Description:
     */
    @Limit(key = "ip_limit_test", period = 30, count = 3, limitType = LimitType.IP)
    @GetMapping("/limitTest3")
    public int testLimiter3() {

        return ATOMIC_INTEGER_3.incrementAndGet();
    }

}