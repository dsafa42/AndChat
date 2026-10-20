# 固定签名密钥（推荐）

不配置时工作流会临时生成密钥，每次构建签名不同，更新 APK 必须先卸载旧版。
想要每次签名一致、可以直接覆盖安装，生成一次密钥并存入 GitHub Secrets：

1. 生成密钥（任意装了 JDK 的环境，Termux 里 `pkg install openjdk-21` 也行）：

   keytool -genkeypair -v -keystore release.jks -alias aoita \
     -keyalg RSA -keysize 4096 -validity 36500

2. 转成 base64（单行）：

   base64 -w0 release.jks        # Termux / Linux

3. 仓库 Settings → Secrets and variables → Actions → New repository secret，添加：

   KEYSTORE_BASE64    上一步输出的内容
   KEYSTORE_PASSWORD  密钥库密码
   KEY_ALIAS          aoita
   KEY_PASSWORD       密钥密码（与库密码相同可不填）

4. 妥善备份 release.jks 和密码，丢失后无法再签出可覆盖安装的同名更新。
   不要把 release.jks 提交进仓库（.gitignore 已忽略 *.jks）。
