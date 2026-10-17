# tokaI18n

給 toka 系列插件共用的多語系小套件：和 tokaStorage 一樣**內嵌（shade）進各插件**，不增加必裝插件。目前只提供繁體中文（`zh-TW`），`en-US` 檔案可以之後再補，沒有的字會自動退回到它／顯示 key。

設計依據：`zero/docs/ARCHITECTURE.md` 的「共用多語系規劃」（`${0}`、`${1}` 位置參數、`lang/zh-TW.yml`、查字回退）。

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

## 慣例（所有新插件共用）

**key 命名**（小寫、點分層，穩定不改）：

| 區 | 用途 | 例 |
|---|---|---|
| `cmd.<指令>.desc` / `cmd.<指令>.usage` | 指令說明、用法（`/指令 help` 與錯誤用法提示用） | `cmd.home.usage` |
| `help.<指令>` | `/指令 help` 的多行說明（YAML 清單，第一行標題） | `help.display` |
| `msg.<區>.<名>` | 玩家看到的訊息 | `msg.home.set` |
| `err.<名>` | 錯誤／拒絕 | `err.noPermission` |
| `ui.<表單>.<名>` | 表單標題、按鈕、說明 | `ui.portal.title` |
| `log.<名>` | 後台文字 | `log.loaded` |
| `status.<名>` | 狀態字（開／關、單位等） | `status.on` |

資料庫／設定／API 回傳存**穩定的 code／ID**，不存翻譯後的字；顯示時才轉文字。指令名稱、權限節點、config key、事件名稱、API 方法名不翻譯。

**顏色與樣式**（`§` 碼直接寫在語系檔內，核心支援哪些就用哪些）：

| 角色 | 色碼 | 說明 |
|---|---|---|
| 一般文字 | `§b` | 後台與玩家訊息的主文字 |
| 數字／名稱／值 | `§a` | 數量、名稱、座標等變動值（參數處用 `§a${0}§b`） |
| 警告 | `§e` | 後台警告、需要注意 |
| 成功 | `§a` | 完成的結果 |
| 錯誤／拒絕 | `§c` | 失敗、沒有權限 |
| 次要說明 | `§7` | 用法、提示、help 的補充 |
| 標題 | `§6` | help 標題（可加 `§l`） |
| 結尾 | `§r` | 需要重設樣式時 |

- 錯誤／警告一律用亮色（`§c`、`§e`），玩家訊息與後台相同，不用暗色（`§4`、`§6` 當警告）。
- **後台 log 不加方括號標籤**（日誌本身已有插件名），行首直接寫文字；結尾不必 `§r`。
- 玩家訊息若需要分類，可用 `§7[類別]§r` 之類的前綴，同一個插件內固定一種。
- help 格式：第一行 `§6§l<名稱> 說明`，之後每行 `§e/指令 <參數> §7- §b說明`。
- 衝突（同一件事插件間色碼不同）以本表為準；表裡沒涵蓋的先記在各插件 `docs/I18N.md` 的「待確認」。
