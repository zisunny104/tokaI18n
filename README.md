# tokaI18n

**tokaI18n 0.2.1** 是 toka 系列插件共用的多語系小套件，內嵌（shade）進各插件，不增加必裝插件。目前只提供繁體中文（`zh-TW`）；沒有的字會退回 `en-US`，再退回顯示 key。需要 Java 21。

## 使用方式

```java
this.messages = new Messages(getClass().getClassLoader(), getDataFolder().toPath(),
    getConfig().getString("language", "zh-TW"), line -> getLogger().warning(line));
String text = messages.text("msg.home.set", "家A");   // 對應 ${0}
List<String> help = messages.lines("help.home");      // 多行說明（YAML 清單）
```

- 語系檔在 jar 的 `lang/zh-TW.yml`；伺服器擁有者可把同名檔放進 `plugins/<插件>/lang/` 覆蓋個別文字。
- 參數 `${0}`、`${1}` 只替換一次，傳入字串中的 `${…}` 不會再展開；缺參數時保留 `${n}` 並限流警告。
- 查字順序：選定語言、`en-US`、key 本身（只警告一次）。
- `messages.install("config.yml")`：把設定檔範本放進資料夾（不覆蓋）。
- `Pager.of(全部行, 每頁行數, 頁碼)` 回傳該頁的行、頁碼與總頁數；`Pager.parse(參數, 預設)` 把指令參數轉成頁碼。頁尾文字慣例 key 為 `ui.page.footer`。

## 版本紀錄

- 0.2.1：Pager 可用於任何清單。
- 0.2.0：新增 Pager，用於 help 與清單分頁。
- 0.1.0：共用文字（位置參數）、語言退回、可覆蓋的語系檔。

授權：MIT。
