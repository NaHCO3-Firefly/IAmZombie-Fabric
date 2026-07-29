# I Am Zombie? — NeoForge → Fabric 移植进度

> 最后更新：2026-07-29
> 当前 MC 版本：26.2 | Java 25 | 加载器：Fabric（目标）

---

## 📋 项目概况

| 项目 | 详情 |
|---|---|
| 模组名 | I Am Zombie? / 我是僵尸？ |
| mod_id | `iamzombieq` |
| 包路径 | `dev.molang.iamzombieq` |
| 原加载器 | NeoForge 26.2.0.6-beta |
| 目标加载器 | Fabric (Loom 1.10.1) |
| 源代码 | 128 个 Java 文件 |
| 架构特点 | 已有平台抽象层 (`platform/` 包)，便于移植 |

---

## ✅ 已完成

### 1. 构建系统（✅ 完成）

- [x] `settings.gradle` — 使用 Fabric Loom 插件，配置中国镜像（腾讯 → 阿里云 → Maven Central → Fabric）
- [x] `build.gradle` — 从 NeoForge moddev 切换到 Fabric Loom
- [x] `gradle.properties` — 更新为 Fabric 依赖版本
- [x] `gradle-wrapper.properties` — Gradle 本体下载改用腾讯镜像
- [x] 删除 `src/main/templates/` 目录（NeoForge 模板生成机制不再需要）

**关键依赖版本**（需验证）：
```
fabric_version=0.110.0+26.2
cloth_config_version=19.0.0+26.2
cardinal_components_version=7.0.0+26.2
loader_version=0.16.10
yarn_mappings=26.2+build.1
```

### 2. 模组元数据（✅ 完成）

- [x] `src/main/resources/fabric.mod.json` — 创建 Fabric 元数据文件
- [x] 配置 entrypoints（main + client）
- [x] 配置 dependencies（fabric-api, cloth-config, cardinal-components）
- [x] 配置 mixins 引用

### 3. 事件系统基础接口（✅ 部分完成）

- [x] 创建 `api/event/ZombieEvent.java` — 加载器无关的事件标记接口
- [x] 创建 `api/event/Cancellable.java` — 加载器无关的可取消接口
- [x] 重写 4/8 个事件类为加载器无关 POJO：
  - [x] `ZombieTransformPreEvent` ✅
  - [x] `ZombieTransformedEvent` ✅
  - [x] `ZombieEatPreEvent` ✅
  - [x] `ZombieAteEvent` ✅
  - [x] `ZombieEvolvePreEvent` ✅
  - [ ] `ZombieEvolvedEvent` ❌ 待移植
  - [ ] `ZombieInfectPreEvent` ❌ 待移植
  - [ ] `ZombieInfectedEvent` ❌ 待移植

---

## ❌ 未完成（按优先级排序）

### 阶段一：基础设施（阻塞后续所有工作）

#### 1. 完成事件系统重构
- [ ] 移植剩余 4 个事件类（Evolved, InfectPre, Infected）
- [ ] 重写 `platform/EventBusService.java` 接口 — 移除 NeoForge `Event` 类型边界
- [ ] 重写 `internal/event/ZombieEventPublisher.java` — 使用新接口
- [ ] 创建 `platform/fabric/FabricEventBusService.java` 正确实现（当前是占位符）
- [ ] 创建 `platform/fabric/FabricAttachmentService.java` — Cardinal Components 实现
- [ ] 更新 `platform/Services.java` — 切换到 Fabric 实现

#### 2. 移植主模组类
- [ ] 重写 `IAmZombieMod.java` — 从 `@Mod` 改为 `ModInitializer`
- [ ] 重写 `client/IAmZombieClient.java` — 从 NeoForge 客户端事件改为 Fabric Client 初始化

#### 3. 移植配置系统
- [ ] 重写 `IAmZombieConfig.java` — 从 ModConfigSpec 改为 Cloth Config API
- [ ] 重写 `IAmZombieClientConfig.java` — 同上
- [ ] 创建配置屏幕（可选，Cloth Config 自动生成）

### 阶段二：核心系统

#### 4. 移植注册表
- [ ] 重写 `IAmZombieRegistries.java` — 移除 IEventBus 依赖
- [ ] 重写 `IAmZombieBlocks.java` — DeferredRegister → Fabric Registry
- [ ] 重写 `IAmZombieItems.java` — 同上 + CreativeModeTab 注册
- [ ] 重写 `IAmZombieEntities.java` — 同上 + EntityAttribute 注册

#### 5. 移植数据附件
- [ ] 重写 `state/IAmZombieAttachments.java` — AttachmentType → Cardinal Components
- [ ] 实现网络同步（Cardinal Components 的 Sync 策略）
- [ ] 实现持久化序列化

### 阶段三：游戏逻辑

#### 6. 移植事件处理器（~10 个文件）
- [ ] `gameplay/HerobrineEvents.java`
- [ ] `gameplay/ZombieFoodEvents.java`
- [ ] `gameplay/ZombieInfectionEvents.java`
- [ ] `gameplay/ZombieMobTargetingEvents.java`
- [ ] `gameplay/ZombieMountEvents.java`
- [ ] `gameplay/ZombiePlayerEvents.java`
- [ ] `gameplay/ZombieSleepEvents.java`
- [ ] `gameplay/CoffinNapManager.java`
- [ ] `gameplay/DifficultyGuardEvents.java`
- [ ] `gameplay/OmenLightsSavedData.java`

#### 7. 移植客户端渲染
- [ ] `client/ZombiePlayerVisuals.java`
- [ ] `client/ZombiePlayerRenderReplacement.java`
- [ ] `client/ZombiePlayerShapeEntities.java`
- [ ] `client/DrownedVisionEvents.java`
- [ ] `client/HerobrineRenderer.java`

### 阶段四：收尾

#### 8. 更新 Mixin
- [ ] 检查所有 mixin 目标类在 Yarn 映射下的名称
- [ ] 更新 `iamzombieq.mixins.json`（如有需要）
- [ ] 可能需要创建 `iamzombieq.accesswidener`

#### 9. 移植 GameTest（可选）
- [ ] 将所有 NeoForge GameTest 转为 Fabric GameTest

#### 10. 资源文件检查
- [ ] 检查 `src/main/resources/assets/` 是否需要调整
- [ ] 检查 `src/main/resources/data/` 是否需要调整

---

## 🔑 关键技术决策

| 问题 | 决策 |
|---|---|
| 配置系统 | Cloth Config API |
| 玩家数据存储 | Cardinal Components API |
| MC 版本 | 保持 26.2 不变 |
| 网络镜像 | 腾讯镜像优先，阿里云回退 |
| 事件系统 | 自定义加载器无关事件总线 |

---

## 📁 文件状态总览

### 已修改/新建的文件
```
✅ settings.gradle                    — 完全重写（Fabric + 中国镜像）
✅ build.gradle                       — 完全重写（Fabric Loom）
✅ gradle.properties                  — 完全重写（Fabric 依赖版本）
✅ gradle/wrapper/gradle-wrapper.properties — 腾讯镜像
✅ src/main/resources/fabric.mod.json — 新建
✅ src/main/resources/iamzombieq.mixins.json — 暂不修改（大部分兼容）
✅ src/main/java/.../api/event/ZombieEvent.java — 新建
✅ src/main/java/.../api/event/Cancellable.java — 新建
✅ src/main/java/.../api/event/ZombieTransformPreEvent.java — 重写为 POJO
✅ src/main/java/.../api/event/ZombieTransformedEvent.java — 重写为 POJO
✅ src/main/java/.../api/event/ZombieEatPreEvent.java — 重写为 POJO
✅ src/main/java/.../api/event/ZombieAteEvent.java — 重写为 POJO
✅ src/main/java/.../api/event/ZombieEvolvePreEvent.java — 重写为 POJO
✅ src/main/java/.../platform/fabric/FabricEventBusService.java — 新建（占位符）
🗑️ src/main/templates/                — 已删除
```

### 仍需移植的核心文件（按依赖顺序）
```
1. src/main/java/.../api/event/ZombieEvolvedEvent.java
2. src/main/java/.../api/event/ZombieInfectPreEvent.java
3. src/main/java/.../api/event/ZombieInfectedEvent.java
4. src/main/java/.../platform/EventBusService.java
5. src/main/java/.../internal/event/ZombieEventPublisher.java
6. src/main/java/.../platform/fabric/FabricEventBusService.java
7. src/main/java/.../platform/fabric/FabricAttachmentService.java
8. src/main/java/.../platform/Services.java
9. src/main/java/.../IAmZombieMod.java
10. src/main/java/.../IAmZombieConfig.java
11. src/main/java/.../IAmZombieClientConfig.java
12. src/main/java/.../IAmZombieRegistries.java
13. src/main/java/.../IAmZombieBlocks.java
14. src/main/java/.../IAmZombieItems.java
15. src/main/java/.../IAmZombieEntities.java
16. src/main/java/.../state/IAmZombieAttachments.java
17. src/main/java/.../client/IAmZombieClient.java
18. src/main/java/.../gameplay/*.java (10 个文件)
19. src/main/java/.../client/ZombiePlayerVisuals.java
20. src/main/java/.../client/ZombiePlayerRenderReplacement.java
21. src/main/java/.../client/ZombiePlayerShapeEntities.java
22. src/main/java/.../client/DrownedVisionEvents.java
23. src/main/java/.../client/HerobrineRenderer.java
```

### 不需要修改的文件
```
- src/main/java/.../rules/ — 纯逻辑，加载器无关
- src/main/java/.../api/core/ — 接口定义
- src/main/java/.../api/extension/ — 接口定义
- src/main/java/.../api/registry/ — 数据类
- src/main/java/.../block/ — 方块逻辑（仅注册表调用需改）
- src/main/java/.../entity/ — 实体逻辑（仅注册表调用需改）
- src/main/java/.../state/PlayerZombieData.java 等 — 数据类
- src/main/java/.../tags/ — 标签
- src/main/java/.../util/ — 工具类
- src/main/java/.../mixin/ — 大部分兼容，待验证
- src/main/resources/assets/ — 资源文件
- src/main/resources/data/ — 数据包
```

---

## 🚧 已知坑点

1. **Yarn 映射**：Mixin 中的 `net.minecraft` 类名需要用 Yarn 映射（Mojang → Yarn），与 NeoForge 的 Mojang 映射不同
2. **Cardinal Components 版本**：需确认 26.2 对应的版本号
3. **Fabric API 版本**：需确认 26.2 对应的版本号
4. **Cloth Config 版本**：需确认 26.2 对应的版本号
5. **RenderPlayerEvent**：Fabric 没有直接等价物，需要用 Fabric Rendering API 或 mixin
6. **ClientTickEvent**：Fabric 有 `ClientTickEvents.END_CLIENT_TICK`
7. **EntityJoinLevelEvent/EntityLeaveLevelEvent**：Fabric 有 `EntityEvents.ENTITY_LOAD` / `ENTITY_UNLOAD`
8. **PlaySoundEvent**：Fabric 有 `PlaySoundCallback`
9. **ItemTooltipEvent**：Fabric 有 `ItemTooltipCallback`
10. **BlockEntityTypeAddBlocksEvent**：Fabric 需要用 mixin 实现

---

## 📌 下一步建议

1. **先完成事件系统**（剩余 4 个事件类 + EventBusService 接口 + Fabric 实现）
2. **然后移植 Services 和主类**
3. **接着移植配置和注册表**
4. **再移植游戏逻辑事件处理器**
5. **最后移植客户端渲染**
6. **构建测试**：`./gradlew build`
7. **运行测试**：`./gradlew runClient`

---

## 🔗 参考资源

- Fabric 模板生成器：https://fabricmc.net/develop/template
- Fabric API 版本查询：https://modrinth.com/mod/fabric-api/versions
- Cloth Config：https://modrinth.com/mod/cloth-config
- Cardinal Components：https://modrinth.com/mod/cardinal-components-api
- Fabric Loom 文档：https://fabricmc.net/wiki/documentation:fabric_loom
- Yarn 映射查询：https://fabricmc.net/develop/
