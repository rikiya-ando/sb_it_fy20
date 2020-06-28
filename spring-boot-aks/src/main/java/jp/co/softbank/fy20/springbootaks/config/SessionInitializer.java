package jp.co.softbank.fy20.springbootaks.config;

import org.springframework.session.web.context.AbstractHttpSessionApplicationInitializer;

/**
 * HTTP セッションの初期化クラスです。
 * <p/>
 * HTTP セッションの実装として Redis を使用するように構成します。
 */
public class SessionInitializer extends AbstractHttpSessionApplicationInitializer {

  /**
   * このクラスのインスタンスを初期化します。
   */
  public SessionInitializer() {
    super(RedisConnectionConfiguration.class);
  }

}
