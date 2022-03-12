# CodeX

Android一维码二维码等生成解析和扫描，具有高扩展性和自定义性

其中扫码相机控制基于CameraX

以下内置实现均可按需引入：

- 支持自定义处理器（processor）
  - zxing
  - zxing-cpp（待实现）
  - zbar
  - mlkit
  - hms-scan
  - hms-scan-plus
- 支持自定义扫码装饰（decorator）
  - 自动放大（autozoom）
  - 成功音效（beep）
  - 扫描线样式（finder）
    - iOS样式（finder-ios）
    - 微信样式（finder-wechat）
  - 成功冻结帧（frozen）
  - 手势控制（gesture）
  - 成功震动（vibrate）
- 支持自定义扫码分析器（analyzer）
  - 解码分析（decode）
    - 自定义处理器（processor）
  - 亮度分析（luminosity）


# 如何接入

1. 添加仓库

```groovy
maven { url 'https://jitpack.io' }
```

2. 添加依赖

```groovy
// 核心库（必须引入）
implementation "com.github.goweii.CodeX:core:$version"

// 处理器（选择一个）
implementation "com.github.goweii.CodeX:processor-hms
implementation "com.github.goweii.CodeX:processor-hms-plus
implementation "com.github.goweii.CodeX:processor-mlkit
implementation "com.github.goweii.CodeX:processor-zbar
implementation "com.github.goweii.CodeX:processor-zxing

// 装饰器（按需引入）
implementation "com.github.goweii.CodeX:decorator-autozoom
implementation "com.github.goweii.CodeX:decorator-beep
implementation "com.github.goweii.CodeX:decorator-finder-ios
implementation "com.github.goweii.CodeX:decorator-finder-wechat
implementation "com.github.goweii.CodeX:decorator-frozen
implementation "com.github.goweii.CodeX:decorator-gesture
implementation "com.github.goweii.CodeX:decorator-vibrate

// 分析器（按需引入）
implementation "com.github.goweii.CodeX:analyzer-luminosity
```
