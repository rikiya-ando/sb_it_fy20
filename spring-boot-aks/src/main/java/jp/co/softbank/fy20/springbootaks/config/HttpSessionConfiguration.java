package jp.co.softbank.fy20.springbootaks.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * HTTP セッションの Redis への接続設定を格納するクラスです。
 */
@Component
@ConfigurationProperties(prefix = "spring.redis")
@Data
@NoArgsConstructor
public class HttpSessionConfiguration {

  private String host;

  private int port;

  private String password;

}
