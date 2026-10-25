# tokaI18n

多語系文字、參數替換、數字格式與清單分頁套件，使用 shade 內嵌進各插件，不需另外安裝。需要 Java 21。

語系檔由使用本套件的插件管理，本套件本身不附翻譯檔。查字時先使用選定語系，再查 `en-US`，都沒有才顯示 key；不會自動產生英文翻譯。toka 系列目前主要使用繁體中文 `zh-TW`。

## 使用方式

```java
this.messages = new Messages(getClass().getClassLoader(), getDataFolder().toPath(),
    getConfig().getString("language", "zh-TW"), line -> getLogger().warning(line));
String text = messages.text("msg.home.set", "家A");   // 對應 ${0}
List<String> help = messages.lines("help.home");      // 多行說明，格式為 YAML 清單
```

- 語系檔在 jar 的 `lang/zh-TW.yml`；伺服器擁有者可把同名檔放進 `plugins/<插件>/lang/` 覆蓋個別文字。
- 參數 `${0}`、`${1}` 只替換一次，傳入字串中的 `${…}` 不會再展開；缺參數時保留 `${n}` 並限流警告。
- 查字順序：選定語言、`en-US`、key 本身；缺字警告每次載入只記錄一次。
- `NumberText.withCommas(12345L)` 回傳 `12,345`，負數亦正確。
- `messages.install("config.yml")`：把設定檔範本放進資料夾，保留既有檔案。
- `Pager.of(全部行, 每頁行數, 頁碼)` 回傳該頁的行、頁碼與總頁數；`Pager.parse(參數, 預設)` 把指令參數轉成頁碼。頁尾文字慣例 key 為 `ui.page.footer`。

授權：MIT。
