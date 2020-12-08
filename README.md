# CodeX

Android一维码二维码等生成解析和扫描，具有高扩展性和自定义性

其中扫码基于CameraX，故在稳定和后期维护上有保障

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

