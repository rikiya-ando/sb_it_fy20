package jp.co.softbank.fy20.springbootaks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * サンプルの DAO です。
 */
@Component
public class SampleDao {

  @Autowired
  JdbcTemplate jdbcTemplate;

  /**
   * データソースに対して、検証 SQL を実行し、結果を返します。
   *
   * @return 検証の結果
   */
  public String getHealth() {
    return jdbcTemplate.queryForObject("SELECT 1", String.class);
  }

}
