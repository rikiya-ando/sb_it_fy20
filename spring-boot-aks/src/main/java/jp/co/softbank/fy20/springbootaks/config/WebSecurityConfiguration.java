package jp.co.softbank.fy20.springbootaks.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * Web セキュリティの設定を構成するクラスです。
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

  /**
   * HttpSecurity の設定を上書きします。
   */
  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.csrf().disable();
  }

}
