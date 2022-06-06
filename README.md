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

```
maven { url 'https://jitpack.io' }
```

2. 添加依赖

```
// 核心库（必须引入）
implementation "com.github.goweii.CodeX:core:$version"

// 处理器（选择一个）
implementation "com.github.goweii.CodeX:processor-hms:$version"
implementation "com.github.goweii.CodeX:processor-hms-plus:$version"
implementation "com.github.goweii.CodeX:processor-mlkit:$version"
implementation "com.github.goweii.CodeX:processor-zbar:$version"
implementation "com.github.goweii.CodeX:processor-zxing:$version"

// 装饰器（按需引入）
implementation "com.github.goweii.CodeX:decorator-autozoom:$version"
implementation "com.github.goweii.CodeX:decorator-beep:$version"
implementation "com.github.goweii.CodeX:decorator-finder-ios:$version"
implementation "com.github.goweii.CodeX:decorator-finder-wechat:$version"
implementation "com.github.goweii.CodeX:decorator-frozen:$version"
implementation "com.github.goweii.CodeX:decorator-gesture:$version"
implementation "com.github.goweii.CodeX:decorator-vibrate:$version"

// 分析器（按需引入）
implementation "com.github.goweii.CodeX:analyzer-luminosity:$version"
```
