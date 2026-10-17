# tokaI18n

給 toka 系列插件共用的多語系小套件：和 tokaStorage 一樣**內嵌（shade）進各插件**，不增加必裝插件。目前只提供繁體中文（`zh-TW`），`en-US` 檔案可以之後再補，沒有的字會自動退回到它／顯示 key。

## 使用方式

```java
// onEnable：語言取自 config.yml 的 language（預設 zh-TW）
this.messages = new Messages(getClass().getClassLoader(), getDataFolder().toPath(), getConfig().getString("language", "zh-TW"), line -> getLogger().warning(line));
String text = messages.text("msg.home.set", "家A");        // 對應 ${0}
List<String> help = messages.lines("help.home");         // 多行說明（YAML 清單）
```

- 語系檔在 jar 的 `lang/zh-TW.yml`；伺服器擁有者可以把同名檔放進 `plugins/<插件>/lang/` 覆蓋個別文字（沒寫到的 key 仍用 jar 裡的）。
- 參數 `${0}`、`${1}` 只替換一次，傳進來的字串裡有 `${…}` 不會再被展開；缺參數時保留 `${n}` 並限流警告。
- 查字順序：選定語言 → `en-US` → 直接回傳 key（可辨識的缺漏）並只警告一次。
- `messages.install("config.yml")`：把設定檔範本放進資料夾（不覆蓋），依序找 `lang/<語言>/config.yml`、`lang/en-US/config.yml`、插件根目錄的同名檔。目前各插件仍用根目錄的繁中 `config.yml`，有第二種語言再放 `lang/<語言>/config.yml`。

## 分頁（0.2.0 起）

`Pager.of(全部行, 每頁行數, 要求頁碼)` 回傳該頁的行、頁碼、總頁數（頁碼超出範圍會移到最近的頁）；`Pager.parse(參數, 預設)` 安靜地把指令參數轉成頁碼。頁尾文字放各插件語系檔，慣例 key 為 `ui.page.footer`（`${0}`=頁碼、`${1}`=總頁數、`${2}`=指令）。長的 help 與清單一律用它分頁，每頁行數放各插件 config。

## 慣例

key 分區、顏色、help 格式與用語等共用原則統一維護在 `zero/docs/ARCHITECTURE.md`。
