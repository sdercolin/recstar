# RecStar

言語を選択: [English](README.md) | [简体中文](README-zhCN.md) | [日本語](README-ja.md)

UTAUスタイルの録音リストを扱う録音アプリケーション。デスクトップ/iOS/Androidに対応しています。

![platforms.png](readme_images/platforms.png)

## 機能

- 個別設定（録音リスト、ガイドBGMなど）をもつ録音セッションの管理
- コメントファイルを持つ録音リストの管理
- ガイドBGMを用いた連続録音
- 自動アクション（例：録音後の自動再生）
- 設定可能なサンプリングレートとビット深度
- （デスクトップ版のみ）選択可能なオーディオ入力・出力デバイス
- テキストファイル読み込み時の自動エンコーディング検出
- 横向き・縦向きモードでのUIレイアウトの自動調整
- ライト・ダークテーマ
- 多言語対応（英語、日本語、中国語）

## ダウンロード

最新バージョンは、[リリースページ](https://github.com/sdercolin/recstar/releases)でご確認ください。

### デスクトップ

- Windows: `~win64.zip`
- macOS (Intel): `~mac-x64.dmg`
- macOS (Apple Silicon): `~mac-arm64.dmg`
- Ubuntu: `~amd64.deb`

他のLinux OSについては、ご自身でビルドを試みてください。

### Android

#### APK

リリースページに添付されています。

#### Play Store

Play Store で `RecStar` を検索するか、以下のリンクを使用してください。
https://play.google.com/store/apps/details?id=com.sdercolin.recstar

### iOS

App Store で `RecStar` を検索してください。

## 収録をはじめる

1. ご自身のニーズに合ったUTAU録音リストを準備してください。スクリーンショットに使用されている録音リストは、
[巽式連続音録音リスト](https://tatsu3.hateblo.jp/entry/ar426004)からのものです。

2. 新しいセッションを作成するには、"+"ボタンをクリックしてください。
3. 録音リストをインポートするには、"..."ボタンをクリックしてください。
OREMOスタイルのコメントファイルをインポートする必要がある場合は、録音リストと一緒にインポートしてください
（ポップアップが表示され、コメントファイルの選択を求められます）。
4. インポートしたアイテムをクリックすると、セッションが作成されます。
5. ガイドBGMを使用する場合は、音符ボタンをクリックしてBGMファイルをインポートして選択してください。
連続録音とトリミングのタイミングを定義するOREMOスタイルのBGM設定ファイルもインポートできます。
注意：デスクトップ版では、設定ファイルはBGMファイルと同じディレクトリにあり、`<ガイドBGMファイルと同じ名前>.txt`という名前である必要があります。
6. 録音ボタンをクリックして録音を開始してください。
7. 録音されたファイルを再生するには、オーディオグラフをクリックしてください。
8. 録音後に録音されたファイルにアクセスするには、"..."ボタンをクリックして"エクスポート"または"ディレクトリを開く"を選択してください。

## フィードバック

フィードバックがある場合は、[Discordサーバー](https://discord.gg/TyEcQ6P73y)に参加して #recstar チャンネルを探すか、
このリポジトリに Issue を開いてください。

問題を報告する際は、「設定」->「RecStar について」->「デバイス情報をコピー」で取得した情報と関連スクリーンショットを提供してください。

デスクトップ版を使用している場合は、アプリディレクトリの`logs`ディレクトリにあるログファイルも提供してください
（アプリ内のウィンドウメニュー「ヘルプ」->「アプリディレクトリを開く」で開くことができます）。

## 開発を始める

RecStarは[Compose Multiplatform](https://github.com/JetBrains/compose-jb)で構築されています。

開始方法については、[プロジェクトテンプレートのREADME](README-compose.md)をご覧ください。

<details>
<summary>その他の推奨設定</summary>

1. `Kotlin KDoc Formatter`プラグインをインストールし、以下の設定を使用してください。
   ![KDoc Formatter設定](readme_images/kdoc_settings.png)
2. コードをコミットする前に自動的にフォーマットするプリコミットフックを追加するために、
一度`./gradlew addKtlintFormatGitPreCommitHook`を実行してください。
3. 文字列定義ファイル（例：[StringsEnglish.kt](shared/src/commonMain/kotlin/ui/string/StringEnglish.kt)）で、Android
Studioのフォーマッターがワイルドカードインポートを単一インポートに常に変更してしまう場合は、
`ui.string`パッケージのワイルドカードインポートを許可するように設定を調整してください。

</details>

## クレジット

ロゴデザイン：InochiPM
