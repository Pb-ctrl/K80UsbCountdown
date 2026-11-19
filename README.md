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

by 屑屑521@酷安