# RecStar

选择语言：[English](README.md) | [简体中文](README-zhCN.md) | [日本語](README-ja.md) | [한국어](README-ko.md)

基于 UTAU 式录音表的音频录制工具，支持桌面/iOS/Android平台。

**(本页面基于 ChatGPT 的翻译。)**

![platforms.png](readme_images/platforms.png)

## 功能

- 管理具有个别设置（录音列表、指导BGM等）的录音会话
- 管理带有相应评论文件的录音列表
- 使用BGM进行连续录音
- 自动化操作（例如，录音后自动回放）
- 可配置的采样率和位深度
- （仅限桌面版）可选择的音频输入/输出设备
- 加载文本文件时自动编码检测
- 横屏和竖屏模式下的自适应UI布局
- 浅色和深色主题
- 多语言支持（英语、日语、中文, 韩国）

## 下载

最新版本请参见[发布页面](https://github.com/sdercolin/recstar/releases)。

### 桌面版

- Windows: `~win64.zip`
- macOS (Intel): `~mac-x64.dmg`
- macOS (Apple Silicon): `~mac-arm64.dmg`
- Ubuntu: `~amd64.deb`

对于其他类型的Linux操作系统，请尝试自行构建。

### Android

#### APK

请从发布页面的附件中下载。

#### Play Store

在 Play Store 搜索 `RecStar`，或使用以下链接：
https://play.google.com/store/apps/details?id=com.sdercolin.recstar

### iOS

在 App Store 搜索 `RecStar`。

## 入门

1. 准备符合您需求的UTAU录音表。截图中使用的录音表来自[巽式连续音录音列表](https://tatsu3.hateblo.jp/entry/ar426004)。
2. 点击"+"按钮创建新会话。
3. 点击"..."按钮导入录音列表。如果需要导入 OREMO 评论文件，请与录音列表一起导入（会弹出对话框询问是否需要导入评论文件）。
4. 点击导入的录音表以创建会话。
5. （可选）如果您想使用BGM，请点击音符符号按钮导入并选择BGM文件。您也可以导入OREMO定义的BGM配置文件来配置连续录音和修剪。
注意：在桌面版上，只有与BGM文件位于同一目录的文件名为`<与BGM文件同名>.txt`的配置文件才会被自动导入。
6. 点击录音按钮开始录音。
7. 点击音频波形图播放录制的文件。
8. 录音后，点击"..."按钮并选择"导出"或"打开目录"以访问录制的文件。

## 反馈

如果您有任何反馈，请加入我们的[Discord服务器](https://discord.gg/TyEcQ6P73y)并找到#recstar频道，或在此仓库中提出 Issue。

报告问题时，请提供由"设置" -> "关于 RecStar" -> "复制设备信息"得到的信息和相关截图。

如果您使用的是桌面版，请一并提供位于应用目录下`logs`目录中的日志文件（您可以通过应用中的窗口菜单"帮助" -> "打开应用目录"打开它）。

## 开始开发

RecStar是基于[Compose Multiplatform](https://github.com/JetBrains/compose-jb)构建的。

请参阅[项目模板的README](README-compose.md)来了解如何开始参与开发。

<details>
<summary>其他推荐设置</summary>

1. 安装`Kotlin KDoc Formatter`插件，并使用以下设置：
   ![KDoc Formatter设置](readme_images/kdoc_settings.png)
2. 运行 `./gradlew addKtlintFormatGitPreCommitHook` 一次，添加一个 pre-commit hook，它会在提交前自动格式化代码。
3. 如果在字符串定义文件（例如，[StringsEnglish.kt](shared/src/commonMain/kotlin/ui/string/StringEnglish.kt)）中，
如果您的 Android Studio 的 Formatter 总是将通配符导入转换为单个导入，请调整设置以允许 `ui.string` 包的通配符导入。

</details>

## 致谢

Logo设计：InochiPM
