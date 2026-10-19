# Aoita

Kotlin + Jetpack Compose 的 Android 聊天应用（minSdk 24，只打包 arm64-v8a）。

## 结构

```
data/       ChatMessage、ChatProvider（后端接口）、DemoProvider（本地演示）
ui/chat/    ChatViewModel、ChatScreen、消息组件、Markdown 渲染、图标
ui/theme/   配色、字体、主题
ui/         CrashScreen（崩溃后下次启动显示错误栈）
```

接入真实模型：实现 `ChatProvider.stream(history)`，返回文本增量流，
然后在 `ChatViewModel.provider` 里替换 `DemoProvider`。

## 构建（GitHub Actions）

推送到 `main` 即自动构建，在该次运行的 Artifacts 里下载
`Aoita-arm64-v8a-release.apk`（另附 debug 包，用于排查问题）。

### 固定签名密钥（推荐）

不配置时每次构建临时生成密钥，签名不同，更新前必须先卸载旧版。
要可覆盖安装，生成一次密钥并存入仓库 Secrets：

```
keytool -genkeypair -v -keystore release.jks -alias aoita \
  -keyalg RSA -keysize 4096 -validity 36500
base64 -w0 release.jks
```

Settings → Secrets and variables → Actions，添加：
`KEYSTORE_BASE64`（上面的输出）、`KEYSTORE_PASSWORD`、`KEY_ALIAS`（aoita）、`KEY_PASSWORD`。
密钥文件和密码请自行备份，不要提交进仓库。
