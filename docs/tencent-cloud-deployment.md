# 小兜芽：腾讯云轻量服务器部署指南

本指南将 Spring Boot、MySQL 和 HTTPS 反向代理部署在同一台腾讯云轻量应用服务器上。适合小兜芽初版上线与微信小程序审核；数据库不直接暴露公网。

## 1. 购买建议

推荐购买 **腾讯云轻量应用服务器（Lighthouse）**，选择：

- 中国内地地域：优先上海或广州，按你的主要用户所在地就近选；
- Ubuntu 22.04 LTS；
- 最低实用配置：2 核 4 GB 内存、60 GB SSD、5 Mbps 或以上；
- 购买时长：至少 1 年包年包月。

不要选中国香港或海外地域：虽然可能免备案，但不适合面向中国大陆微信用户的正式小程序发布。中国内地轻量服务器申请备案要求包年包月且购买时长不少于 90 天。

## 2. 购买域名并完成实名认证

在腾讯云域名服务中检索并注册域名。建议优先使用 `.cn` 或 `.com`，例如 `xiaodouya.cn`；最终以实际可注册名称为准。

完成域名实名认证，且域名注册人信息要与后续备案主体信息一致。

## 3. 配置服务器与安全组

在轻量服务器控制台记录公网 IP；在防火墙中放行：

- TCP 22：仅限你的当前公网 IP（用于远程登录）；
- TCP 80：HTTP，用于申请 HTTPS 证书；
- TCP 443：HTTPS，小程序接口访问；

不要开放 3306、8080 到公网。

## 4. 配置 DNS 和备案

在域名 DNS 中添加一条记录：

```text
主机记录：api
记录类型：A
记录值：你的轻量服务器公网 IP
```

得到的地址形如 `api.你的域名`。随后在腾讯云备案系统提交：

1. 域名的 ICP 备案；
2. 微信公众平台中的小程序备案。

备案未完成前可以先部署联调，但不能将未备案域名作为小程序的正式服务域名提交发布。

## 5. 将代码上传到服务器

在 Windows PowerShell 中（把 IP 换为实际公网 IP）：

```powershell
ssh root@你的服务器公网IP
```

首次登录后，在服务器执行：

```bash
apt update && apt upgrade -y
apt install -y git ca-certificates curl
curl -fsSL https://get.docker.com | sh
systemctl enable --now docker
mkdir -p /opt/xiaodouya
cd /opt/xiaodouya
git clone https://github.com/liao-xy/xiaodouya-planning-ledger.git .
cp deploy/.env.example deploy/.env
chmod 600 deploy/.env
```

使用 `nano deploy/.env` 填写真实值。绝不要把这个 `.env` 文件提交至 Git。

## 6. 启动服务

确认 `PUBLIC_API_DOMAIN` 已替换为已解析的 API 域名后：

```bash
cd /opt/xiaodouya/deploy
docker compose up -d --build
docker compose ps
docker compose logs -f app
```

当日志中出现 Spring Boot 已启动的信息后，访问：

```text
https://api.你的域名/api/daily-quote
```

能返回 JSON 即代表 HTTPS、反向代理和后端 API 已可访问。

## 7. 连接微信小程序

在 `mobile/src/config.js` 中把 `API_BASE_URL` 改成：

```js
export const API_BASE_URL = 'https://api.你的域名/api'
```

在微信公众平台的“开发 → 开发管理 → 开发设置 → 服务器域名”中，将 `https://api.你的域名` 加入 `request 合法域名`。然后重新构建、上传体验版并真机测试登录、记账和 AI。

## 8. 日常更新和备份

更新代码：

```bash
cd /opt/xiaodouya
git pull
cd deploy
docker compose up -d --build
```

备份数据库：

```bash
cd /opt/xiaodouya/deploy
docker compose exec -T db sh -c 'mysqldump -uroot -p"$MYSQL_ROOT_PASSWORD" --single-transaction "$MYSQL_DATABASE"' > xiaodouya-$(date +%F).sql
```

请定期将备份下载到本地或对象存储；仅有服务器磁盘不等于备份。
