# I Am Zombie? — NeoForge → Fabric 移植进度

> 最后更新：2026-07-29
> 当前 MC 版本：26.2 | Java 25 | 加载器：Fabric

---

## 📋 项目概况

| 项目 | 详情 |
|---|---|
| 模组名 | I Am Zombie? / 我是僵尸？ |
| mod_id | `iamzombieq` |
| 包路径 | `dev.molang.iamzombieq` |
| 原加载器 | NeoForge |
| 目标加载器 | Fabric (Loom 1.17\.17, 非混淆模式) |
| 源代码 | ~130 个 Java 文件 |

---

## ✅ 已完成

### 构建系统 + 模组元数据
- [x] settings.gradle / build.gradle / gradle.properties — Fabric Loom + 中国镜像
- [x] fabric.mod.json — Fabric 元数据
- [x] 非混淆模式（MC 26.2 原生 Mojang 名称）

### 核心框架
- [x] IAmZombieMod.java — Fabric ModInitializer + 所有事件注册
- [x] IAmZombieClient.java — Fabric 客户端初始化
- [x] IAmZombieRegistries.java — Fabric Registry.register
- [x] IAmZombieBlocks.java / Items.java / Entities.java — Fabric 注册
- [x] IAmZombieConfig.java — Supplier shim 模式
- [x] IAmZombieAttachments.java — Cardinal Components 适配
- [x] platform/Services.java + FabricAttachmentService + FabricEventBusService

### 事件处理器（ gameplay/ ）
- [x] **ZombiePlayerEvents** — 玩家 tick/登录/登出/死亡/克隆
- [x] **ZombieMountEvents** — 坐骑系统（蜘蛛/鸡/僵尸/马/鹦鹉螺）
- [x] **ZombieInfectionEvents** — 感染系统（村民→僵尸村民，猪/猪灵→僵尸猪灵）
- [x] **ZombieFoodEvents** — 食物系统（僵尸食物规则+人类食物惩罚）
- [x] **ZombieMobTargetingEvents** — 生物目标选择（无视列表+主动种子）
- [x] **ZombieSleepEvents** — 床爆炸+棺材睡眠
- [x] **CoffinNapManager** — 棺材小睡+白昼跳过（ClockManager API）
- [x] **DifficultyGuardEvents** — 难度保护

### Mixin
- [x] ItemStackMixin — hurtAndBreak MC 26.2 签名
- [x] ServerPlayerFoodMixin — completeUsingItem 注入
- [x] AvatarRendererMixin — 僵尸皮肤纹理
- [x] PlayerModelMixin — 僵尸手臂姿势

### 已修复的运行时问题
- [x] 面具模型（Equippable.builder + EquipmentAssets.LEATHER）
- [x] 棺材渲染（cullface）
- [x] 水里着火（isSunBurnTick 守卫）
- [x] 僵尸皮肤（AvatarRenderer mixin）
- [x] 僵尸手臂（PlayerModel mixin）
- [x] 食物注册（UseItemCallback + completeUsingItem）
- [x] 死亡进化（ServerLivingEntityEvents.AFTER_DEATH）

---

## 🔄 进行中

### 客户端渲染（stub 待填充）
- [ ] HerobrineEvents — 完全 stub
- [ ] DrownedVisionEvents — 完全 stub
- [ ] ZombiePlayerVisuals — 部分 stub
- [ ] ZombiePlayerRenderReplacement — 部分 stub

### 待验证
- [ ] 完整游戏测试（runClient）
- [ ] 多人游戏同步

---

## ❌ 未开始

- [ ] GameTest 移植
- [ ] 资源文件检查
- [ ] AccessWidener（如需要）

---

## 🔑 关键技术决策

| 问题 | 决策 |
|---|---|
| 配置系统 | Supplier shim（Cloth Config 准备） |
| 玩家数据存储 | Cardinal Components API |
| 事件注册 | Fabric API callbacks + mixin |
| 非混淆模式 | MC 26.2 原生 Mojang 名称，无需映射 |
| 时钟 API | ClockManager.moveToTimeMarker |
| 装备模型 | Equippable.builder + EquipmentAssets |

---

## 📌 下一步

1. 填充 HerobrineEvents stub
2. 填充客户端渲染 stub
3. 完整游戏测试
4. 修复运行时问题
