package com.jianghao.oplog.config;

import com.jianghao.oplog.handle.OpLogInterceptor;
import com.jianghao.oplog.handle.PerformanceInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import javax.sql.DataSource;

/**
 * @author ：jh
 * @date ：Created in 2020/8/4 14:21
 * @description：Mybatis-Plus配置
 */

@Configuration
@EnableTransactionManagement
@MapperScan("com.jianghao.oplog.orm.dao")
public class MybatisPlusConfig {

    /**
     * @param
     * @return com.jianghao.oplog.handle.PerformanceInterceptor
     * @Description: SQL执行效率插件  设置 dev test 环境开启
     */
    @Bean
    @Profile({"dev","test"})
    public PerformanceInterceptor performanceInterceptor() {
        return new PerformanceInterceptor();
    }

    /**
     * @param dataSource
     * @return OpLogInterceptor
     * @Description: 数据更新操作处理
     */
    @Bean
    @Profile({"dev","test"})
    public OpLogInterceptor dataUpdateInterceptor(DataSource dataSource) {
        return new OpLogInterceptor(dataSource);
    }
}