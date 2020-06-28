package jp.co.softbank.fy20.springbootaks.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.session.data.redis.config.ConfigureRedisAction;

/**
 * Redis への接続の設定クラスです。
 */
@Configuration
@Profile("pr")
public class RedisConnectionConfiguration {

  /**
   * Redis の接続を取得する接続ファクトリを返します。
   *
   * @param sessionConfiguration Redis の接続設定情報
   * @return 接続ファクトリ
   */
  @Bean
  public RedisConnectionFactory connectionFactory(HttpSessionConfiguration sessionConfiguration) {
    RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();

    config.setHostName(sessionConfiguration.getHost());
    config.setPort(sessionConfiguration.getPort());
    if (!StringUtils.isBlank(sessionConfiguration.getPassword())) {
      config.setPassword(RedisPassword.of(sessionConfiguration.getPassword()));
    }

    return new LettuceConnectionFactory(config);
  }

  /**
   * Redis の構成と妥当性チェックの戦略を返します。
   *
   * @return Redis の構成と妥当性チェックの戦略
   */
  @Bean
  public static ConfigureRedisAction configureRedisAction() {
    return ConfigureRedisAction.NO_OP;
  }

}
