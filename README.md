# K80 USB Debug Countdown

适用于 REDMI K80 的 LSPosed 模块。通过系统设置开启以下功能时，可跳过小米安全服务的倒计时确认页。

- 普通 USB 调试
- USB 调试（安全设置）
- 无障碍服务

模块作用域仅为「安全服务」（`com.miui.securitycenter`）。新电脑的 Android USB 调试授权流程独立于这些提示。实机已验证三种倒计时及已授权电脑的 ADB 重连。新电脑的首次授权弹窗尚未单独验证。

## 兼容性

已验证 REDMI K80（`zorn`）、HyperOS `OS4.0.3.0.XOKCNXM`、安全服务 `13.8.1-261023.0.1`。模块按此版本匹配。

## 安装

1. 从 [Releases](../../releases/latest) 下载并安装 `K80UsbCountdown.apk`。
2. 在 LSPosed 中启用模块，勾选「安全服务」作为作用域。
3. 重新启动手机，让安全服务加载模块。需要恢复原有提示时，可停用模块并重启。

## 从源码编译

在 Windows PowerShell 中进入仓库根目录。需要 JDK 17（`java`、`javac`、`jar`、`keytool` 在 PATH 中）和 [Android SDK 命令行工具](https://developer.android.com/tools/sdkmanager)。通过 Android Studio 的 SDK Manager 或命令行安装 Android SDK Platform 35 和 Build Tools 35.0.0。

```powershell
sdkmanager "platforms;android-35" "build-tools;35.0.0"
.\build.ps1 -SdkRoot 'C:\Android\Sdk'
```

把示例中的 SDK 路径换成实际路径。若已设置 `ANDROID_HOME`，可以直接运行 `.\build.ps1`。脚本从 Maven 镜像下载 Xposed API 82，校验其 SHA-256，并将它作为编译依赖。构建产物是 `build/K80UsbCountdown-unsigned.apk`。

### 签名与验证

首次本地构建可生成自己的签名密钥。已有测试密钥时可直接使用该密钥，并省略第一条命令。命令会提示输入密钥库密码。

```powershell
keytool -genkeypair -keystore build/local-signing.p12 -storetype PKCS12 -alias local -keyalg RSA -keysize 3072 -validity 3650 -dname 'CN=Local K80 USB Countdown'
$signer = 'C:\Android\Sdk\build-tools\35.0.0\lib\apksigner.jar'
java -jar $signer sign --ks build/local-signing.p12 --ks-key-alias local --out build/K80UsbCountdown.apk build/K80UsbCountdown-unsigned.apk
java -jar $signer verify --verbose build/K80UsbCountdown.apk
```

请保管自己的密钥库。Android 要求覆盖安装的 APK 使用与已安装版本相同的签名。若设备安装了 Releases 中的版本，使用自己的密钥签名后需先卸载该版本，再安装自编译 APK 并重新启用 LSPosed 模块。

by 屑屑521@酷安

免责声明：仅供学习交流，请自行承担使用风险。
