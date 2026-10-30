# 小兜芽 · 规划型记账

黑客松 MVP：先为钱分配用途，再记录消费如何影响计划。

## 目录

- `server/`：Spring Boot 3 + H2 的 REST API，预留 MySQL、阿里云百炼接入。
- `mobile/`：uni-app + Vue 3 前端，可运行到微信开发者工具、H5 或 App。

## 当前功能

- 收支账单增删改查与分类；
- 资金计划（真实钱包余额中划出的用途额度、已用、剩余、未分配余额）；
- 月度总览、分类消费占比、每日支出；
- 兜兜 AI 会话与历史记录；
- 服务端严格构造用户专属数据快照，百炼不可用时使用可解释的本地兜底回答。
- 每日一句：服务端每天从一言接口获取并缓存，同日全体用户展示相同内容；网络不可用时使用内置原创备选文案。

## 资金计划的规则

“资金计划”不是独立于钱包之外的许愿目标，而是当前真实钱包余额的一部分用途分配：

- 当前钱包 = 全部历史收入 − 全部历史支出；
- 已分配 = 所有计划尚未使用的金额之和；
- 未分配 = 当前钱包 − 已分配，只有这部分可以继续创建计划，或记录“不归入计划”的支出；
- 归入某个计划的支出不能超过该计划剩余金额。收入到账后会先进入“未分配”，由用户决定是否划入计划。

这样可以避免“钱包里没有这么多钱，却设了很多计划”。真正独立于当前余额的未来愿望，后续应以“储蓄目标”功能表达，而不是资金计划。

账单和资金计划都可以编辑或删除。删除账单会重新计算计划已用额；删除计划会保留历史账单，但解除其计划归属。前端会二次确认。所有读写接口均按当前微信用户 ID 校验所有权，不能通过普通接口修改另一位用户的数据。

## 开发启动

1. 安装 Maven 3.9+ 后，进入 `server` 执行 `mvn spring-boot:run`。
2. 在 HBuilderX 导入 `mobile`，运行到 H5 或微信开发者工具。
3. H5 默认请求 `http://localhost:8080/api`；真机/小程序需将 `mobile/src/config.js` 的地址改为局域网或已部署 HTTPS 域名。

## 使用 MySQL 保存真实数据

当前默认的 H2 为演示用内存数据库，服务重启后数据会清空，并会加载演示账本。要保存真实账单，请创建 UTF-8MB4 数据库（例如 `guihua`），然后在 PowerShell 设置：

```powershell
$env:SPRING_PROFILES_ACTIVE = "mysql"
$env:MYSQL_URL = "jdbc:mysql://127.0.0.1:3306/guihua?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai"
$env:MYSQL_USERNAME = "你的数据库账号"
$env:MYSQL_PASSWORD = "你的数据库密码"
mvn spring-boot:run
```

启用 `mysql` profile 后，演示数据不会写入；账单、资金计划、AI 对话历史均会保存至 MySQL。首次运行会自动创建表。数据库密码只保存在后端环境变量中，不能写入 `mobile/` 或提交到仓库。

## 微信身份一键登录

- 小程序首次进入后，点击“微信一键登录”。前端调用 `uni.login` 取得一次性 `code`，后端用该 `code` 向微信换取 OpenID，并据此创建或定位小兜芽用户。
- 不获取、不保存用户手机号；账单、资金计划、AI 会话和 AI 数据快照都以 OpenID 对应的用户 ID 隔离。
- 在微信公众平台取得小程序 AppID 和 AppSecret 后，仅在**后端运行环境**配置：

  ```powershell
  $env:WECHAT_APP_ID = "你的小程序AppID"
  $env:WECHAT_APP_SECRET = "你的小程序AppSecret"
  ```

- 在 HBuilderX 的 `manifest.json` / 微信小程序配置中填写同一个 AppID。`AppSecret` 不得写入前端、`manifest.json`、截图或 Git 仓库。
- MySQL 会使用 `app_users` 与 `auth_tokens` 表保存用户身份与登录令牌；原有 `transactions`、`money_plans`、`chat_sessions`、`chat_messages` 表保存对应用户的数据。
- 当前收入分类已含“生活费”。首页、消费构成、资金规划、我的为底部导航页；右下角钱包吉祥物始终可进入 AI 对话。

本地调试应运行到微信开发者工具；正式发布前需要将 HTTPS 后端域名配置为小程序 request 合法域名，并在微信公众平台的隐私保护指引中说明使用“微信登录身份信息”用于账户与数据隔离。

## 正式发布与备案

小兜芽当前新增了“我的 → 服务与资质”页面，用户在两级菜单内即可查看备案号、主办者与服务联系方式，并可提交内容安全反馈或注销账户。该页的真实主体信息不写死在前端，而是由后端环境变量提供；注销会删除个人业务数据，但依法必须保留的安全处置记录除外。

在取得官方核准/备案编号后，必须在生产环境配置以下真实值并重启后端：

```powershell
$env:APP_ICP_FILING_NO = "粤ICP备XXXXXXXX号-X"
$env:APP_OPERATOR_NAME = "你的真实姓名"
$env:APP_SERVICE_EMAIL = "你的服务邮箱"
$env:APP_SERVICE_PHONE = "你的联系电话"
```

未配置 `APP_ICP_FILING_NO` 时，页面会明确显示“待配置”，该版本仅应作为开发/测试版本，不应面向公众正式上线。小程序需将 `https://beian.miit.gov.cn` 配置到微信公众平台的业务域名白名单，才能在应用内打开备案核验链接。

服务端会对明显涉及诈骗、洗钱、赌博、非法交易等高风险 AI 提问进行前置拦截，并对拦截和用户反馈保留必要的处置记录。这只是 MVP 的技术底座；正式运营仍需指定实际负责人员、处理时限和与主管部门的上报流程。

## 接入阿里云百炼 AI

1. 在阿里云百炼控制台的 API Key 页面创建与调用地域一致的 API Key；建议使用北京地域和 `qwen3.8-flash`。
2. **只在运行后端的电脑上**设置密钥。Windows PowerShell 示例：

   ```powershell
   $env:BAILIAN_API_KEY = "你的百炼 API Key"
   mvn spring-boot:run
   ```

   这只在当前 PowerShell 窗口有效。若要给当前 Windows 用户永久保存：

   ```powershell
   [Environment]::SetEnvironmentVariable("BAILIAN_API_KEY", "你的百炼 API Key", [EnvironmentVariableTarget]::User)
   ```

   然后重新打开 HBuilderX / IDE 或 PowerShell，再启动后端。也可使用官方常用变量名 `DASHSCOPE_API_KEY`，项目同样会识别。
3. 默认 Base URL 为北京地域兼容接口 `https://dashscope.aliyuncs.com/compatible-mode/v1`。若使用业务空间专属域名，在启动前覆盖：

   ```powershell
   $env:BAILIAN_BASE_URL = "https://你的WorkspaceId.cn-beijing.maas.aliyuncs.com/compatible-mode/v1"
   $env:BAILIAN_MODEL = "qwen3.8-flash"
   mvn spring-boot:run
   ```

4. 重新启动后端，进入小程序的“兜兜 AI”发送一句问题；有有效密钥时将调用百炼，没有密钥或调用失败时才会退回本地规则回答。AI 会使用服务端生成的当前月汇总、分类占比、资金计划、近 6 个月趋势与近期真实账单生成建议；不会信任前端提交的统计结果。

不要把 API Key 写入 `mobile/`、前端环境变量、截图或 Git 仓库。小程序只能调用你的 Spring Boot 后端。

开发数据库地址：`http://localhost:8080/h2-console`，JDBC URL 为 `jdbc:h2:mem:guihua`。
