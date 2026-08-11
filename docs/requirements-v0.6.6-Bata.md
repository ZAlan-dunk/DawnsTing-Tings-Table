# 懒羊羊当大厨~ v0.6.6-Bata 私有云端备份

## 目标

- 在卸载、换机或无法覆盖安装前，允许使用者从 App 首页一键上传个人数据。
- 重新安装对应发布包后，可读取云端备份并在确认后恢复。
- 保持包名 `com.dawns.tingstable`、现有本地数据键与两个仓库各自的 release 签名链不变。
- 两个 App 均显示“懒羊羊当大厨~”；Lulu 只保留仓库名、APK 文件名、云端目录和签名链差异。

## 数据边界

- 上传：自定义菜谱、收藏 ID、菜篮、采购清单、已选食材、皮肤模式、备份格式版本和创建时间。
- 不上传：APK 内置菜谱、云峰特典内容、来源封面缓存、账号、设备标识、日志或其他文件。
- Ting 路径：`backups/tings/latest.json`。
- Lulu 路径：`backups/lulu/latest.json`。
- 云端令牌必须是仅能访问私有备份仓库且只有 `Contents: Read and write` 权限的细粒度令牌，通过 GitHub Secret 注入发布构建，不写入 Git。

## 交互

- 首页保持紧凑，仅新增一行“云端备份”，显示最近上传或恢复时间。
- “上传”不增加多余步骤，点击后直接更新当前使用者的 `latest.json`，并显示进行中、成功或失败状态。
- “恢复”先下载并校验格式、版本和使用者身份，再显示备份时间与数据摘要。
- 确认恢复后替换本机个人数据；内置菜谱不变。写入失败时尝试恢复操作前快照。
- 网络不可用、超时、无备份、令牌无效、身份不符或格式不支持时，显示可读错误且不修改本机数据。

## 验收

- Given 本机有个人数据，when 点击上传，then 对应云端路径被创建或覆盖，且首页显示最近上传时间。
- Given 云端有有效备份，when 点击恢复并确认，then 六类个人数据被替换，重启后仍存在。
- Given 恢复预览尚未确认，when 取消，then 本机数据不变。
- Given 网络或数据校验失败，when 操作结束，then 本机数据不变并出现错误提示。
- Given Ting 安装包，when 读取 Lulu 路径或身份不匹配内容，then 拒绝恢复；反向同理。
- Given 200% 字号和窄屏，when 首页显示备份行，then 上传、恢复和状态文本仍可达且不遮挡其他内容。
- Given v0.6.5-Bata release 已安装，when 安装同仓库 v0.6.6-Bata release，then 版本号和既有签名允许覆盖升级。

## 发布

- `versionCode 13` / `versionName 0.6.6-Bata`。
- Git 标签与 Release：`v0.6.6-Bata`。
- Ting APK：`LazySheepChef-v0.6.6-Bata-release.apk`。
- 只发布 release APK，不生成或交付额外 debug APK。
