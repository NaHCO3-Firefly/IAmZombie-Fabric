# 模组开发入门 — 项目状态检查清单

> 项目: **I Am Zombie?** (iamzombieq)
> 状态: NeoForge → Fabric 移植中（构建系统已验证通过）
> MC 版本: 26.2 | Java 25 | 加载器: Fabric (Loom 1.17.17 非混淆模式)

---

## §1 确认初始输入 — ✅ 已完成

| 项目 | 值 | 状态 |
|------|-----|:----:|
| 模组展示名称 | I Am Zombie? / 我是僵尸？ | ✅ |
| mod_id | `iamzombieq` | ✅ |
| 作者 | `molang` | ✅ |
| 包基础路径 | `dev.molang.iamzombieq` | ✅ |
| 目标 Minecraft 版本 | 26.2 | ✅ |
| 加载器 | Fabric (单加载器) | ✅ |
| Java 版本 | 25 (与 MC 26.2 基线匹配) | ✅ |

## §1.5 版本依赖配置 — ✅ 已完成（构建系统修复后）

- [x] `minecraft_version=26.2`
- [x] `loader_version=0.16.10` → **0.19.3**（已升级）
- [x] `fabric_version=0.110.0+26.2` → **0.156.0+26.2**（已升级）
- [x] `cloth_config_version=19.0.0+26.2` → **26.2.155**（已升级）
- [x] `cardinal_components_version=7.0.0+26.2` → **8.0.1**（已升级）
- [x] ~~`yarn_mappings=26.2+build.1`~~ **已删除** — MC 26.2 无 Yarn 映射
- [x] ~~`fabric-loom`~~ → **`net.fabricmc.fabric-loom`**（非混淆模式）
- [x] ~~`mappings` 配置~~ **已删除**（非混淆模式无需映射层）
- [x] ~~`modImplementation`/`modApi`~~ → **`implementation`**
- [x] **Gradle**: 9.2.1 → **9.5.0**（腾讯镜像）
- [x] Java target/release = 25 ✅
- [x] Java toolchain = 25 ✅

## §2 中国大陆网络镜像配置 — ✅ 已完成（新增 BMCLAPI）

- [x] `settings.gradle` → `pluginManagement` 配置腾讯 → 阿里云 → BMCLAPI → Fabric
- [x] `settings.gradle` → `dependencyResolutionManagement` 配置腾讯 → 阿里云 → BMCLAPI → 各MC仓库
- [x] `gradle-wrapper.properties` → `distributionUrl` 腾讯镜像（9.5.0）
- [x] 依赖解析验证通过：所有 MC 依赖均从镜像成功下载
- [x] NeoForge `exclusiveContent` 坑点不适用（纯 Fabric）

## §3 选择加载器与架构策略 — ✅ 已完成

- [x] 单加载器 Fabric
- [x] NeoForge → Fabric 移植

## §4 选择起始模板 — ✅ 已完成

- [x] 已从 NeoForge 迁移到 Fabric Loom
- [x] 使用 `net.fabricmc.fabric-loom` 1.17.17（非混淆模式）

## §5 获取并初始化模板脚手架 — ✅ 已完成

- [x] `build.gradle` — Fabric Loom 非混淆模式配置完整
- [x] `settings.gradle` — 工程名 `iamzombieq`，镜像配置完整
- [x] `gradle.properties` — 依赖版本已验证
- [x] `fabric.mod.json` — entrypoints、dependencies、mixins 已配置

## §6 审计初始脚手架 — ✅ 已完成

### 始终检查项
- [x] `gradle.properties` — 已审计并修复版本号
- [x] `settings.gradle` — 已审计并添加 BMCLAPI 镜像
- [x] `build.gradle` — 已审计并修复语法错误 + 适配非混淆模式
- [x] 主模组入口类 — `IAmZombieMod.java`（**仍需移植**）
- [x] 资源目录 — `src/main/resources/` 结构正确

### Fabric 特定文件
- [x] `fabric.mod.json` — 已创建，引用正确
- [x] `iamzombieq.mixins.json` — 已存在，配置正确
- [x] 客户端初始化类 — `IAmZombieClient.java`（**仍需移植**）
- [x] 平台抽象层 — `platform/fabric/` 已有占位实现

## §7 清理模板 — ✅ 已完成

- [x] `src/main/templates/` — 已删除
- [x] 构建产物无残留

## §8 重命名核心项目身份 — ✅ 已完成

- [x] **mod_id**: `iamzombieq` — 所有文件一致
- [x] **包路径**: `dev.molang.iamzombieq` — 所有文件一致
- [x] **展示名称**: `I Am Zombie?` — 所有文件一致
- [x] **作者**: `molang` — 所有文件一致
- [x] **资源命名空间**: `iamzombieq` — 一致

## §9 跨文件重命名检查 — ✅ 已完成

- [x] `gradle.properties` → `mod_id=iamzombieq`, `mod_group_id=dev.molang.iamzombieq`
- [x] `settings.gradle` → `rootProject.name = 'iamzombieq'`
- [x] `build.gradle` → `archivesName = project.mod_id`
- [x] `fabric.mod.json` → `id=${mod_id}`, entrypoints 包路径正确
- [x] `iamzombieq.mixins.json` → package 和 mixin 类名一致
- [x] 源代码 → 所有包声明为 `dev.molang.iamzombieq.*`
- [x] 资源 → `assets/iamzombieq/` 和 `data/iamzombieq/` 一致

## §10 加载器特定重命名 — ✅ 已完成

- [x] `fabric.mod.json` entrypoints 与包路径对齐
- [x] mixin 包路径 `dev.molang.iamzombieq.mixin` 正确

## §11 交接前的验证 — ⚠️ **构建系统已验证，源代码仍待移植**

### 构建系统验证

| 步骤 | 命令 | 状态 | 备注 |
|------|------|:----:|------|
| Gradle wrapper 下载 | `gradlew --version` | ✅ | Gradle 9.5.0，腾讯镜像 |
| 依赖解析 | `gradlew dependencies` | ✅ | 所有 MC/Fabric 依赖解析成功 |
| 清理构建 | `gradlew clean build` | ❌ | 源代码含 NeoForge 引用无法编译 |
| 运行客户端 | `gradlew runClient` | ❌ | 同上 |

### 源代码移植阻塞项

| 文件 | 问题 | 优先级 |
|------|------|:------:|
| `IAmZombieMod.java` | `@Mod(neoforge)`, `IEventBus`, `NeoForge.EVENT_BUS` | 🔴 高 |
| `IAmZombieConfig.java` | `ModConfigSpec` (NeoForge) | 🔴 高 |
| `IAmZombieClientConfig.java` | `ModConfigSpec` (NeoForge) | 🔴 高 |
| `Services.java` | 硬编码 `NeoForgeEventBusService` / `NeoForgeAttachmentService` | 🔴 高 |
| `Events*` 事件类 | 2/8 个待重写为 POJO | 🟡 中 |
| `gameplay/*.java` | 10 个事件处理器注册在 NeoForge 总线上 | 🟡 中 |
| `client/*.java` | 客户端渲染依赖 NeoForge 事件 | 🟡 中 |

### 修复路线（详见 `PORTING_PROGRESS.md`）

1. **事件系统重构** — EventBusService 接口 + Fabric 实现
2. **主模组类** — `IAmZombieMod.java` → Fabric `ModInitializer`
3. **配置系统** — Cloth Config API 替换 NeoForge ModConfigSpec
4. **注册表** — Fabric Registry 替代 NeoForge DeferredRegister
5. **数据附件** — Cardinal Components 替代 NeoForge AttachmentType
6. **事件处理器** — `gameplay/*.java` Fabric 化
7. **客户端渲染** — Fabric 事件替代 NeoForge 事件
8. **最终构建测试** — `gradlew clean build` + `runClient`

---

## 总结

`getting-started-modding` 的 **§1–§10 已全部完成**。构建系统已从旧版 Fabric Loom 1.10.1 + Yarn 映射成功迁移到 **`net.fabricmc.fabric-loom` 1.17.17 非混淆模式**（MC 26.2 新模式），所有依赖通过中国镜像正确解析。下一步是按路线图进行源代码移植。
