package com.kin.family.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信配置
 *
 * @author candong
 */
@Data
@Component
@ConfigurationProperties(prefix = "wechat.miniapp")
public class WeChatConfig {
    private String appid;
    private String secret;
}
