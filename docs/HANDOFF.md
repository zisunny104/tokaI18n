# 多語系導入交接（2026-10-17）

## 部署紀錄（2026-10-17，等玩家 /reload 後生效）

已換上 VPS：zero 1.7.22、tokaPortal 2.3.1（主控台也能用 /portal help）、tokaWorlds 1.1.0、tokaDisplay 0.2.7、tokaSigns 0.4.0、tokaDiscordLink 0.2.0，另新裝 tokaProfiles 0.2.0。SHA 皆核對一致，舊 jar 與舊 config 在 VPS 的 `/home/codex-editor/stage/migrate-20261017/old5/`。各 repo 已打對應 tag（未 push）。tokaPortal、tokaWorlds 的現有 config 已補上 `reserved-names`。

**新增（部署前一刻決定）**：保留字 `help list set del edit` 不能當住家、傳送點（tokaPortal）、世界（tokaWorlds，會建議其他名稱）、領地與區域（zero）的名稱；config 的 `reserved-names` 可調（領地與區域是寫死在 zero）。傳送類指令仍保留「先查目標、查不到才顯示說明」作保險。已存在的同名項目不受影響。

## 分頁與 /portal help 總覽（tokaPortal 2.4.0 已部署，tokaI18n 0.2.0）

- tokaI18n 新增 `Pager`（分頁）；tokaPortal 的 `/portal help [頁]` 列出所有傳送指令、缺少的 zero 功能（顯示「未啟用：…」），以及已安裝的 tokaWorlds、tokaSigns 指令；每頁行數 config `help-lines-per-page`（預設 8）。主控台也可用。
- 其餘插件的 help 目前仍是單頁；內容變長時改用 `Pager`，頁尾 key 用 `ui.page.footer`。下一步建議：tokaSigns（`help.sign` 已 23 行）、tokaDiscordLink、tokaWorlds 先改成分頁。

## 部署後要驗證

1. `/reload` 後後台無 `Text is missing`、無錯誤；各插件載入數量 log 正常。
2. 玩家端：`/portal help`、`/worlds help`、`/sign help`、`/discord help`、`/display help`（OP）、`/profiles help`。
3. 試建名叫 `help` 的住家、傳送點、領地、世界，應被擋下。
4. 側邊欄銀幣、金幣有千分位逗號。
5. 血條：reload 後是否回來（見最下方「另案」）。

## 狀態

| 專案 | 版本 | commit | key 數 | 測試 | 狀態 |
|---|---|---|---|---|---|
| tokaI18n | 0.1.0（tag v0.1.0） | 915a80d | — | 6 | 已 commit，未推 GitHub（尚無遠端） |
| tokaPortal | 2.2.2 → 2.3.0 | 33facf2 | 163 | 16 | 已 commit，**未部署、未 tag、未 push** |
| tokaWorlds | 1.0.9 → 1.1.0 | 8fb985e | 129 | 新增 4 | 同上 |
| tokaDisplay | 0.2.6 → 0.2.7 | 3bb7b76 | 31 | 29 | 同上 |
| tokaSigns | 0.3.0 → 0.4.0 | a5f9f0d | 71 | 9 | 同上（私有 repo） |
| tokaProfiles | 0.1.4 → 0.2.0 | 9e23e1a | 40 | 22 | 同上（VPS 未安裝） |
| tokaDiscordLink | 0.1.6 → 0.2.0 | 1824b10 | 77 | 31 | 同上 |
| zero | 1.7.20 → 1.7.21 | cf4d5f2（tag v1.7.21） | — | — | 已部署（側邊欄銀幣／金幣千分位），未 push |

全部只做了建置與單元測試，**沒有在伺服器上實跑**。tokaStorage 沒有玩家文字，不需處理。zero 依指示不抽取（功能將關閉）。

## 做法（每個插件相同）

- `pom.xml` 依賴 `dev.toka:tokaI18n:0.1.0`，shade 時 relocation 到各插件的 `internal.i18n`。
- `src/main/resources/lang/zh-TW.yml`：所有玩家訊息、表單、指令回應、help、後台 log；`config.yml` 新增 `language: zh-TW`；伺服器可在 `plugins/<插件>/lang/zh-TW.yml` 覆蓋個別文字。
- 每個指令都有 `/<指令> help`（內容在 `help.<指令>`）；`plugin.yml` 的指令說明、用法、權限說明補成完整繁中（這是核心讀的靜態欄位，無法走語系檔）。
- 每個專案的 `docs/I18N.md`：key 分區、help 位置、顏色對照、待確認清單。
- 每個專案新增語系測試：檔內 key 都讀得到、原始碼引用的 key 都存在；多數專案另檢查原始碼沒有中文字面值。
- 設定檔註解：各 `config.yml` 補完整繁中註解。目前只有一種語言，所以註解仍放在根目錄 `config.yml`；有第二種語言時再放 `lang/<語言>/config.yml`，由 `Messages.install()` 依語言挑選。

## 已定的慣例（見 tokaI18n/README.md）

key 分區 `cmd / help / msg / err / ui / log / status`；色碼：文字 `§b`、數字與名稱 `§a`、警告 `§e`、成功 `§a`、錯誤 `§c`、次要 `§7`、標題 `§6`；後台 log 不加方括號標籤。

子任務自行新增了兩個分區，建議收進慣例表：`param.*`（參數名稱，tokaPortal）、`cli.*`（命令列工具輸出，tokaWorlds）；tokaDiscordLink 另用 `discord.*`（送到 Discord 的文字，不用 § 色碼，Discord 只認 Markdown）。

## 已決定（2026-10-17）

- 錯誤／警告用亮色 `§c`／`§e`，玩家與後台一致（已寫進 README 慣例）。
- 新增的前綴與顏色保留。
- `/display help` 先只給 OP，之後開放再調整內容。
- `help` 衝突：tokaPortal 的 `/tpa /tpl /tph /tpp /tpw` 先查目標，有叫 help 的目標就傳送，查不到才顯示說明（Nukkit 會吃掉引號，無法用 `"help"` 繞過）。`/home` 無名稱目標，不衝突。

## 其餘待你決定（完整清單在各 docs/I18N.md）

1. **錯誤色統一 `§c`**：tokaWorlds 原本全用 `§e`，已改成 `§c`；tokaDiscordLink 的後台儲存錯誤用 `§c`。慣例表沒有「後台錯誤色」，要不要補。
2. **原本無色的玩家訊息現在都上色並加前綴**：tokaPortal `§7[傳送]§r`、tokaSigns `§7[告示]§r`、tokaProfiles `§7[玩家資料]§r`，tokaDiscordLink 的遊戲內訊息也上色。畫面會有差異，部署前先看一次。
3. **`help` 變成保留字**：tokaPortal 的 `/tpa /tph /tpl /tpp /tpw /home`、tokaProfiles 的 `/profiles` 加了 `help` 子指令；名叫 `help` 的玩家、住家、領地、傳送點或世界，無法再用指令直接傳送。
4. **`/display help` 權限**：目前在 `tokaDisplay.admin` 內，一般玩家看不到；要不要開給 `tokaDisplay.use`。
5. **tokaWorlds journal 存翻譯字**：`status.link.zero`／`status.link.portal` 的翻譯文字會寫進資料庫 `receipts.description`，與「資料只存穩定 code」衝突。為了不改資料格式先保留，之後要改成存 code 加參數。
6. **tokaPortal 的 `StorageManager` 設定錯誤訊息改為英文**（靜態工具，被單元測試直接呼叫）。管理員啟動失敗時看到英文；要中文化需改成傳入 `Messages`。
7. **tokaPortal `PlayerPortalEvent.toString()` 改走語系檔**：zero 若依賴舊格式需確認。
8. **表單標題與按鈕文字同時用來判斷點了哪個**（tokaPortal）：語系檔內不可重複；有 en-US 後要改成以 id 判斷，否則換語言會壞。
9. YAML 陷阱：`yes`／`no`／`on`／`off` 當 key 會被讀成布林，要加引號或避開（tokaPortal 已改 `agree`／`refuse`，tokaSigns 已加引號）。

## 已知限制

- 目前只有 zh-TW；en-US 沒做，缺字會顯示 key 並警告一次。
- 玩家個人語言偏好（tokaProfiles 提供）還沒接，所有人用插件設定的語言。
- 各插件版本號都已往前加，但尚未部署；部署時照慣例：停服或 reload、備份舊 jar、核對 SHA、tag。
- `build.ps1` 要用 pwsh 7 執行（無 BOM 的 UTF-8，Windows PowerShell 5.1 會解析失敗）。
- tokaProfiles 的 `StagedMigrationTest.interruptedBatchCanResumeWithoutDuplicateRecords` 第一次建置失敗、重跑通過，原因未查（疑似 2 秒輪詢逾時）。

## 另案：reload 後血條沒重送

尚未找到原因。後台記錄在 reload 後沒有錯誤或警告，zero 每輪都會重送。需要你確認：是 `/reload` 全部還是只重載 tokaDisplay、等幾秒或重進服後會不會回來、側邊欄有沒有回來。
