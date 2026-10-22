package com.yupi.yuaiagent.campus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * 新生办事助手的独立入口。演示阶段无需模型密钥或数据库。
 */
@SpringBootApplication(scanBasePackages = "com.yupi.yuaiagent.campus",
        exclude = DataSourceAutoConfiguration.class)
public class CampusApplication {
    public static void main(String[] args) {
        SpringApplication.run(CampusApplication.class, args);
    }
}
