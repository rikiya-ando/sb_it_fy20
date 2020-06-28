package jp.co.softbank.fy20.springbootaks;

import static java.util.Objects.nonNull;

import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * サンプルのコントローラです。
 */
@RestController
public class SampleController {

  @Autowired
  SampleDao sampleDao;

  /**
   * データベースを使用したサンプル・プログラムを実行します。
   *
   * @return 実行結果
   */
  @GetMapping("/datasource")
  public String healthByDataSource() {
    return sampleDao.getHealth();
  }

  /**
   * セッションを使用したサンプルを実行します。
   *
   * @param name パスパラメータで指定された名前
   * @param session HTTP セッション
   * @return 実行結果
   */
  @GetMapping("/hello/{name}")
  public String sayHello(@PathVariable String name, HttpSession session) {
    StringBuilder result = new StringBuilder();
    result.append("Hello, ").append(name).append(".");

    String storedName = (String) session.getAttribute("name");
    if (nonNull(storedName) && storedName.equals(name)) {
      result.append(" Long time no see.");
    }
    session.setAttribute("name", name);

    return result.toString();
  }

}
