# BackroomsCore 开发计划

## 1. 项目定位

BackroomsCore 是一个面向 Paper 1.21.4 的后室主题服务器核心插件。

服务器主要体验方向：

- PVE 生存恐怖为主，玩家之间默认合作。
- 不是纯副本制，而是开放世界式 Level 穿梭。
- 首发 MVP 只做 Level 0 与 Level 1。
- Level、战利品、怪物、事件、房间、切层、基地等内容必须配置化。
- 核心流程由代码保证稳定，具体内容由配置与后续管理命令扩展。
- 普通建筑不可破坏，资源方块与特殊交互方块可以交互。
- CraftEngine 作为自定义物品、方块、容器、尸体、资源点的主要框架。
- MythicMobs 负责实体技能、怪物行为和部分实体掉落。

核心目标不是复刻单一 Backrooms Wiki，而是参考官方/社区设定，做一个适合 Minecraft 长期游玩的后室生存服务器。

## 2. 世界结构

MVP 世界规划：

```text
lobby              MEG 主基地 / 初始大厅
level_0            Level 0，阈限体验与基础探索层
level_1            Level 1，资源竞争、基地、局部 PVP 与撤离层
```

### 2.1 Lobby：MEG 主基地

玩家第一次进入服务器时位于 lobby 世界。

设定上 lobby 是 MEG 主基地，功能包括：

- 新手引导。
- 后室入口。
- 基础说明。
- 后续任务、商人、档案、图鉴入口。
- 后续可以接入剧情系统。

玩家从 MEG 主基地进入 Level 0 后，不能直接从 Level 0 回到主基地，必须先找到进入 Level 1 的路径，再从 Level 1 找到稳定撤离点或楼梯井返回。

### 2.2 Level 0

Level 0 是首个后室层级，主要目标不是战斗，而是阈限感、迷路感、教学和基础生存。

关键词：

- 黄色墙纸。
- 潮湿地毯。
- 荧光灯嗡鸣。
- 办公/商场后场式空房间。
- 非线性、重复、相似、容易迷路。
- 低危险，但不完全安全。
- 玩家可以相遇，但空间倾向于分散玩家。

玩家目标：

- 搜集基础生存物资。
- 学习后室规则。
- 体验 Level 0 的阈限空间。
- 寻找 Level 1 入口。

规则：

- 不设置玩家基地。
- 不设置组织据点。
- 不允许 PVP。
- 不允许普通建筑破坏。
- 允许资源方块、容器、尸体、特殊 CE 方块交互。
- 不能直接返回 lobby。

设计原则：

- 不要让玩家永远见不到彼此，但汇合应该有难度。
- 不要做成无聊跑图，需要通过声音、灯光、异常、稀有房间、基础资源点保持探索反馈。
- Level 0 是体验层和入门层，不是刷怪层。

### 2.3 Level 1

Level 1 是 MVP 的第二层，是从阈限体验进入真正生存玩法的第一层。

关键词：

- 混凝土空间。
- 工业仓库。
- 维修通道。
- 管道。
- 黑暗区域。
- 资源更丰富。
- 风险更高。
- 可以存在玩家基地和组织据点。

Level 1 核心压力：

- 资源竞争。
- 局部 PVP。
- 更强迷路压力。
- 更高实体威胁。
- 寻找撤离楼梯井或安全路线。

规则：

- 玩家基地只在 Level 1 内实现。
- 玩家可以在 Level 1 找到可改造房间后 claim。
- 普通建筑不可破坏。
- 资源方块可交互。
- PVP 后期按区域、事件或规则逐步开放。
- 组织之间攻击基地后期再做，MVP 不做基地战争。

## 3. 切层与撤离

主要切层方式：楼梯井。

切层类型预留：

```text
STAIRWELL       楼梯井
DOOR            门
ELEVATOR        电梯
NOCLIP_ZONE     卡出空间/异常穿模点
BLACKOUT_EVENT  停电事件切层
ITEM_TRIGGER    物品触发
COMMAND         管理员/测试命令
```

MVP 目标：

- Level 0 通过楼梯井、维护门或异常入口进入 Level 1。
- Level 1 通过稳定楼梯井返回 lobby / MEG 主基地。

楼梯井设计：

- 稳定楼梯井：固定通向某个层级或主基地。
- 不稳定楼梯井：后期随机通向不同层级或危险区域。
- 锁定楼梯井：后期需要钥匙卡、电力、组织权限或任务进度。
- 异常楼梯井：后期可作为陷阱或恐怖事件入口。

切层提示优先使用 MiniMessage 与 Adventure Title，例如：

```text
title: <yellow><bold>Level 0</bold></yellow>
subtitle: <gray>Threshold | 生存难度 1 | 实体：未确认</gray>
```

## 4. 玩家基地与 Survivor Cell

玩家基地不做在 Level 0，全部放在 Level 1。

玩家组织名称方向：Survivor Cell。

Survivor Cell 是玩家小队/组织的主题化名称，类似帮派、公会或幸存者小组，但表达上更符合后室生存氛围。

### 4.1 基地获得方式

玩家在 Level 1 找到可改造房间后 claim。

可改造房间可以是：

- 废弃仓库。
- 维修间。
- 安全室。
- 旧 MEG 前哨残骸。
- 封闭办公室。
- 发电机房旁边的辅助间。

### 4.2 基地功能

MVP 基地功能：

- 真实存在于 Level 1 世界中，不是单纯 GUI 仓库。
- 可以作为玩家或 Survivor Cell 的据点。
- 可升级。
- 可存储物资。
- 可作为后续组织系统、任务系统、前哨站系统的基础。

后续基地功能：

- 组织成员权限。
- 组织仓库。
- 基地等级。
- 发电机或电力系统。
- 楼梯井稳定器。
- 工作台、医疗站、无线电、档案终端。
- MEG 认证前哨。

### 4.3 基地升级

基地升级使用玩家在 Level 0/1 搜寻到的物资。

升级材料可以包括：

- 金属废料。
- 电缆。
- 工具箱。
- 电池。
- 发电机部件。
- MEG 认证芯片。
- 木板、布料、管道、螺丝。
- CE 自定义材料。

示例等级：

```text
Level 1: 临时避难所
Level 2: 加固营地
Level 3: Survivor Cell 前哨
Level 4: 稳定据点
Level 5: MEG 认证前哨
```

## 5. 方块与交互规则

总体规则：

- 普通建筑不可破坏。
- 资源方块可交互。
- 基地内建设规则后续再细化。
- MVP 先不做复杂保护和组织战争。

CraftEngine 用途：

- 自定义物品。
- 自定义方块。
- 资源方块。
- 尸体方块。
- 物资箱。
- 楼梯井标记。
- 维护门。
- 基地升级设施。

需要测试：

- CraftEngine 方块能否被 WorldEdit/FAWE schematic 正确保存和粘贴。
- CE 容器方块能否通过 API 或 Bukkit 事件接入自定义 loot。
- CE 方块破坏后是否能由 BackroomsCore 拦截并替换为自定义掉落逻辑。

如果 CE 方块无法稳定进入 schematic，则使用 marker 方块方案：

```text
房间模板内放置标记方块/标记牌
生成后 BackroomsCore 扫描 marker
替换成 CE 方块
删除 marker
注册 loot source / transition / event point
```

## 6. 战利品系统

战利品必须配置化。

Loot Source 类型预留：

```text
VANILLA_CONTAINER      原版容器
CE_CONTAINER_BLOCK     CraftEngine 容器方块
CE_BREAKABLE_BLOCK     CraftEngine 可破坏资源方块
CE_CORPSE_BLOCK        CraftEngine 尸体方块
ENTITY_DROP            实体掉落
EVENT_REWARD           事件奖励
COMMAND_REWARD         命令奖励
BASE_UPGRADE_COST      基地升级消耗
```

Level 0 战利品：

- 杏仁水。
- 简单食物。
- 电池。
- 手电相关材料。
- 纸条。
- 基础工具。
- Level 1 入口线索。

Level 1 战利品：

- 金属废料。
- 电缆。
- 工业零件。
- 发电机部件。
- 更高级食物和药品。
- 基地升级材料。
- 稀有钥匙卡。
- 高价值异常物品。

## 7. 死亡、尸体与保险箱

保险箱大小：1 格。

死亡规则：

- 保险箱 1 格内物品保留。
- 普通背包物品进入尸体容器。
- 尸体后续使用 CraftEngine 自定义尸体方块表现。
- 尸体可被本人或其他玩家搜刮，具体权限后续配置化。

尸体状态预留：

```text
fresh_corpse       新鲜尸体，保留玩家物资
looted_corpse      已被搜刮尸体
decayed_corpse     腐化尸体，变成低级 loot source
unknown_corpse     环境随机尸体
meg_corpse         MEG 成员尸体
wanderer_corpse    流浪者尸体
```

## 8. 世界生成与回收

MVP：

- 固定边界。
- Level 0/1 先使用有限范围测试。
- 预制房间随机拼接 + 程序迷宫。

后期：

- 玩家未访问区域动态生成。
- 旧区域可回收。
- 玩家基地和 Survivor Cell 据点不回收。
- 系统前哨、MEG 主设施不回收。

区域生命周期预留：

```text
GENERATED       已生成
VISITED         被访问
LOOTED          已搜刮
STALE           长期无人访问
RECLAIMABLE     可回收
CLAIMED         玩家/组织占领
PROTECTED       系统保护
```

## 9. 配置驱动原则

不要把 Level 写死在 enum 中。

所有内容通过 id 注册：

```text
level_0
level_1
level_2
level_fun
custom_level_x
```

配置目录预期：

```text
plugins/BackroomsCore/
  config.yml
  levels/
    level_0.yml
    level_1.yml
  rooms/
    level_0/
    level_1/
  loot/
  entities/
  events/
  transitions/
  bases/
  messages/
```

管理命令预留：

```text
/br level list
/br level info <levelId>
/br level create <levelId> <world>
/br level reload <levelId>
/br level tp <levelId>

/br room list <levelId>
/br room register <levelId> <roomId> <schematic>
/br room testgenerate <levelId>

/br loot list
/br loot test <tableId>

/br base claim
/br base info
/br base upgrade

/br cell create <name>
/br cell invite <player>
/br cell info
```

## 10. 外部插件规划

强建议：

- CraftEngine：自定义物品和方块框架。
- MythicMobs：实体、技能、掉落。
- Multiverse-Core：多世界管理。
- WorldEdit / FAWE：地图制作和 schematic。
- WorldGuard：区域规则基础能力。
- PlaceholderAPI：变量显示。
- LuckPerms：权限。
- Vault：经济兼容，后期可选。
- CoreProtect：管理和查日志。
- spark：性能分析。
- TAB：显示与界面增强。
- GrimAC：反作弊。

后期考虑：

- Citizens。
- Typewriter / BetonQuest。
- Simple Voice Chat。
- OpenAudioMc。
- Plan。
- ProtocolLib。

## 11. 开发记录规则

每次修改后维护 `step.md`，记录：

- 本次完成了什么。
- 修改了哪些文件。
- 为什么这样设计。
- 下一步建议。
- 是否有测试或验证结果。

`plan.md` 记录长期设计，`step.md` 记录每一步执行总结。

## 12. Pursue-goal 执行模式

从测试服 `D:\dev\backrooms\devserver` 搭好后，开发进入持续推进模式：

- 目标是持续完善项目，直到形成可运行、稳定、可玩的 Backrooms MVP。
- 每次开发都先读取并维护 `plan.md`，如果计划太粗就拆细。
- 优先选择最高优先级未完成任务，直接实现，不只做规划。
- 每次完成一个 step 必须：运行相关检查、更新 `plan.md`、更新 `step.md`、commit 并 push。
- 服务器插件配置也要版本化保存到项目内的 `server-configs/`，再同步到 `devserver/plugins/`。
- 插件功能尽可能支持 `/br reload` 热重载；外部插件配置尽量使用它们自己的 reload 命令，例如 CraftEngine `/ce reload all`、TAB `/tab reload`。

### 12.1 当前最高优先级任务

- 已完成第一批 CraftEngine 26.6 Backrooms 测试物品/方块配置，模型先复用 Minecraft 原版 model/texture，后续再替换美术资源。
- 已完成 TAB 测试服基础显示配置，减少演示占位符和无关动画。
- 已完成 Level 随机 spawn 点配置，先用于 `/br level tp` 和切层入口，避免所有玩家永远落在单一坐标。
- 已实现正式 Transition/撤离点系统的 MVP：Level 0 -> Level 1，Level 1 -> lobby。
- 已新增 Transition 触发指引命令 `/br transition guide <id>`，用于在地图中临时显示 region / block 触发位置，方便摆放 CraftEngine 楼梯井标记。
- 已完成第一版 Room 生成原型：`rooms.yml` 配置 room/corridor 模板，`/br room generate <id> [level]` 可用 Bukkit 原生方块生成简单占位房间。
- 已新增 `/br debug config` 运行时配置摘要命令，用于实机快速检查 Level 世界缺失、Transition/Room 引用问题和模块数量。
- 已新增第一版 Loot Table MVP：`loot.yml` 配置 Bukkit Material 战利品表，`/br loot list/info/roll` 可用于测试，资源方块可通过 `loot-tables` 复用命名掉落池。
- 已扩展第二批 CraftEngine Backrooms 测试资产，增加维护门、撤离口、基地终端、发电机核心、荧光灯等地图制作 marker / 设施方块，以及保险丝、布料、管道、手电框架、工具箱等材料物品。
- 已修正 CraftEngine Faithful Level 0 资产迁移问题：所有 `faithful_*` item model 重新从 Faithful 原始 `models/item/*.json` 迁移，保留原始 `display` 变换，并恢复原包使用 `item/generated` 的门、灯、管道、踢脚线和 wide crate 图标。
- 已补齐 Faithful item texture 迁移到 `resourcepack/assets/backrooms/textures/item/faithful/`，并确认 block/custom/item 模型不再引用 `faithfulbackrooms:` 或缺失资源。
- 已按 CraftEngine 本地文档新增 `categories.yml`、`translations.yml` 和 `lang.yml`，给 Backrooms CE 物品/方块增加 tooltip 描述、`/ce menu` 分类，以及 server-side l10n / client-side lang 翻译。
- 已调整半墙、踢脚线、管道、牌子、CCTV、插座、黑霉等非完整装饰为 `lower_tripwire` + non-occluding 设置；完整墙体、地毯、天花板、crate 保持 `note_block`，避免使用不适合透明/非完整模型的完整方块 fallback 状态。
- 已确认测试服 `/ce reload all` 日志中 `backrooms` 包、items、categories、blocks、资源包生成和上传均成功；后续仍需玩家实机观察模型、碰撞、灯光、storage 和客户端资源包表现。
- 已将 `/br level tp` 与 Transition 迁移到 Paper `teleportAsync`，当前代码中没有同步 `Player#teleport` 调用残留。
- 已将 `/br reload` 改为 staged runtime reload：先用新配置临时加载 Level、Item、Sanity HUD、Sanity、Loot、Resource、Transition、Room、Worldgen，全部成功后统一提交；失败时 live runtime 保持不变。
- 已新增 Loot Source MVP：原版 `CHEST` / `BARREL` 容器打开时可按配置从 Loot Table 注入物品，并用 TileState PDC 标记 one-time 生成状态。
- 已新增 Gradle `syncDevServerConfig` 与 `deployDevServerAll`，用于同步 BackroomsCore 运行时 YAML 到测试服 `plugins/backrooms`，避免 jar 与 YAML 版本不一致。
- 已用 Java 21 重启测试服并验证 BackroomsCore 启动日志：`lootSources=2`、`lootTables=2`、`resourceBlocks=2`、`transitions=2`、`rooms=3`；CraftEngine 在 Java 21 下不再出现 Java 26 的 ASM class major 70 警告。
- 已新增 Loot Source 调试命令：`/br loot sources` 和 `/br loot source info <id>`，用于实机查看容器源的 Level、材质、坐标、loot table、one-time 和 fill-empty-only。
- 已新增 Loot Source direct reward：`event_reward` 与 `command_reward` 类型可通过 `LootSourceService#triggerReward(...)` 对玩家发放 Loot Table 产物，`/br loot source trigger <id> [player]` 可用于管理员测试和脚本触发。
- 已新增尸体缓存 MVP：`corpses.yml` 配置启用 Level、原版容器材料、保险物品堆数量和死亡点附近搜索半径；玩家在 Backrooms Level 死亡时，普通掉落进入尸体容器，保险物品在重生后返还。
- 已新增基地 claim MVP：`bases.yml` 配置 Level 1 固定可 claim 区域，`/br base claim <id>` 持久化占领；Level 规则保护会对 owner 在已 claim 区域内放行破坏/放置。
- 已新增 `README.md`，作为 `plan.md` 的简化执行入口，记录已完成/未完成 TODO、测试流程和地图生成说明。
- 下一阶段最高优先级：重启测试服加载最新 jar，验证 staged `/br reload`、`/br debug config`、`/br level tp`、Transition guide/trigger、`/br loot roll`、资源点 loot table、原版容器 loot source、Room 原型；同时安装 VectorDisplays 与 packetevents，继续实机观察理智 HUD、Faithful item/block 模型、非完整装饰遮挡/碰撞、灯具亮度和 crate storage。

### 12.2 为什么当前仍是第一阶段 MVP

当前阶段重点是把服务器跑起来并形成可测试闭环：Level 配置、跨世界传送、Level 规则、资源方块抽象、CE 测试资产、基础显示配置。世界生成、房间拼接、基地 claim、组织系统和真实 loot 刷新仍属于后续 MVP 任务，因为它们依赖已稳定的 Level/资源/切层基础设施。先做小而稳的闭环，能降低后续 WorldEdit/CE/Multiverse 实机集成时的排错成本。

### 12.3 Step 006 完成状态

- 已新增第一批 CraftEngine 26.6 `backrooms` 资源包配置，包含杏仁水、电池、废料、电线、发电机部件、MEG 芯片、钥匙卡、物资箱、尸体缓存、楼梯井标记等测试资产。
- 已将外部插件配置版本化到 `server-configs/`，并同步到 `devserver/plugins/`。
- 已为 Level 配置增加 `spawn.points`，当前 `/br level tp <id>` 会在多个配置点中随机选择，未来 Transition/撤离点系统也复用这一能力。
- 已增加 `deployDevServer` Gradle task，用于把 BackroomsCore jar 部署到本地测试服 `plugins` 目录。
- 地图/房间生成仍未实现：当前只完成测试服资源和随机 spawn 基础设施，下一步最高优先级是 Transition/撤离点系统，然后再做房间模板和生成原型。

### 12.4 Step 007 完成状态

- 已新增 `transitions.yml`，以单文件 `definitions` 形式配置切层/撤离点，后续可迁移到 `transitions/*.yml` 多文件结构。
- 已新增 Transition MVP：支持 region 进入触发、右键方块触发、Level 目标、world 目标、目标 Level 随机 spawn、精确 point spawn、冷却、传送后短暂无触发保护、音效、MiniMessage 消息反馈。
- 已接入 `/br reload` 热重载，Transition 会在 Level 和资源配置加载后重载，并在控制台输出定义数量。
- 已新增管理命令：`/br transitions`、`/br transition info <id>`、`/br transition trigger <id> [player]`，用于实机调试 Level0->Level1 和 Level1->lobby 的切层流程。
- 当前默认坐标只是测试占位：Level 0 入口区域为 `20,63,-22 -> 28,67,-14`，Level 1 撤离区域为 `-46,63,-28 -> -38,68,-20`。后续地图/CE 楼梯井放置完成后需要调整。
- 下一步需要把 Transition 与地图制作闭环结合：用 CE 楼梯井标记或原版占位方块做入口提示，再做房间模板/区域生成原型。

### 12.5 Step 008 完成状态

- 已新增 `/br transition guide <id>` 管理命令，默认 `op` 权限 `backrooms.command.transition.guide`。
- guide 命令会调用 TransitionService 在触发区域中心、区域角点和配置方块位置生成临时粒子，帮助管理员在没有完整地图工具链时快速找到入口/撤离点区域。
- 已补充 help、tab completion、usage、权限和消息 key，避免 `transition-guide-shown` 缺失时显示原始 key。
- 当前仍不直接通过 Java 调用 CraftEngine API 放置 `backrooms:stairwell_marker`；CE 方块先用 `/ce item get` 或 `/ce debug setblock` 手动摆放，BackroomsCore 只负责 Transition 判定和可视化指引。
- 下一步进入房间/地图原型：需要确定 WorldEdit/FAWE schematic、marker 方块扫描和生成边界。

### 12.6 Step 009 完成状态

- 已新增 `rooms.yml`，用 `rooms.definitions` 配置最小 Room 模板，当前包含 `level0_basic_room`、`level0_corridor`、`level1_utility_room` 三个占位模板。
- 已新增 Room 生成模型和服务：`RoomShape`、`RoomDefinition`、`RoomGenerationResult`、`RoomGenerationService`。
- Room 服务已接入 `/br reload`，重载时会读取 `rooms.yml` 并在控制台输出模板数量。
- 已新增管理命令：
  - `/br rooms` / `/br room list` 查看模板列表。
  - `/br room info <id>` 查看模板尺寸、适用 Level 和材质 palette。
  - `/br room generate <id> [level]` 在玩家当前位置生成简单 room/corridor 原型。
- 当前生成器只使用 Bukkit 原生 `Block#setType`，不引入 WorldEdit/FAWE API；这是为了先验证 Level/权限/配置/材质闭环，再升级到 schematic 粘贴和 marker 扫描。
- 生成命令默认要求玩家站在目标 Level 对应世界内，避免把 Level 0 模板误生成到 lobby 或其他世界。
- 下一步需要重启测试服加载新 jar，并在 `level_0`、`level_1` 世界实机测试 `/br rooms`、`/br room info level0_basic_room`、`/br room generate level0_basic_room level_0`。

### 12.7 Step 010 完成状态

- 已新增 `/br debug config` 管理命令，默认 `op` 权限 `backrooms.command.debug.config`。
- 该命令会输出当前运行时配置摘要：Level 总数/启用数/禁用数、缺失世界、资源方块数量、Transition 数量与问题、Room 数量与问题。
- Transition 检查会报告来源 Level 不存在、触发世界未加载、目标 Level 不存在/禁用、目标 world 未加载等常见实机问题。
- Room 检查会报告模板引用了不存在的 Level，方便修改 `rooms.yml` 后快速验证。
- 下一步继续以实机验证为核心：重启测试服后先执行 `/br reload` 和 `/br debug config`，再验证 `/br transition guide` 与 `/br room generate`。

### 12.8 Step 011 安全审查修复状态

- 已完成一次 Paper 1.21.4 插件安全审查，并先做最小安全修复，不做大块重构。
- `/br reload` 不再先清空旧 Level registry；如果 reload 后没有加载到任何 Level，会保留旧 Level registry，避免保护规则 fail-open。
- Level 规则监听器已增加 world fallback；即使玩家运行时追踪状态缺失，也会按玩家所在 world 识别已配置 Level。
- 已补充桶、火焰、爆炸、实体改方块、展示实体破坏等常见世界破坏事件拦截，降低普通玩家绕过 break/place 保护破坏地图的风险。
- Resource 方块支持 `locations` 显式坐标限制；默认测试资源已改为坐标限定样例，避免同材质地形全部变成资源点。
- Room 生成默认改为 `replace-air-only: true`，并增加世界高度边界校验、光源覆盖放置和无方块改变提示，降低管理员误生成覆盖地图或误判成功的风险。
- `/br debug config` 已增加 Transition `feedback.message-key` 缺失检查。
- `/br reload` 现在会根据实际重载结果反馈成功或失败，避免配置被拒绝时仍显示成功。
- 同步 `Player#teleport` 已迁移为 Paper `teleportAsync` 封装，`/br level tp` 和 Transition 回调都会在主线程完成后续运行时状态更新。
- 下一步最高优先级仍是重启测试服实机验证 `/br reload`、`/br debug config`、资源点坐标、房间生成和 Transition 闭环。

### 12.9 Step 012 Loot Table MVP 状态

- 已新增 `loot.yml` 独立配置文件，采用 `loot-tables.definitions` 管理命名战利品表，当前只使用 Bukkit `Material` 与 `ItemStack`，不接入 CraftEngine Java API 或 NBT。
- 已新增 Loot Table 运行时服务，支持 `/br reload` 热重载、重复 id 检查、无效 Material 跳过、rolls min/max 和按 chance 随机产出。
- 已新增 `/br loot list`、`/br loot info <id>`、`/br loot roll <id> [player]` 管理命令，用于实机测试战利品表；背包满时会把剩余物品掉落到玩家位置。
- Resource 方块新增可选 `loot-tables` 字段，现有 `drops` 保留并与命名 loot table 叠加，便于逐步从内联掉落迁移到可复用战利品池。
- `/br debug config`、启动日志和 `/br reload` 反馈已包含 Loot Table 数量。
- 下一步实机验证重点：`/br loot list`、`/br loot info level0_basic_supplies`、`/br loot roll level0_basic_supplies <player>`、资源点触发 loot table，并确认 `/br reload` 后数量稳定。

### 12.10 Step 013 Resource 调试命令状态

- 已新增 Resource 方块运行时只读列表能力，`ResourceBlockService#all()` 可供命令层检查当前加载定义。
- 已新增 `/br resources`、`/br resource list`、`/br resource info <id>` 管理命令，用于实机查看 resource block 的 Level、Material、Trigger、locations、loot tables、drops、cooldown 与替换方块配置。
- 已新增 `backrooms.command.resource.list`、`backrooms.command.resource.info` 权限，并加入 `backrooms.admin`。
- 已补充 `/br help`、tab completion、`messages.yml` 和 `paper-plugin.yml`，方便在测试服定位资源点配置与 loot table 引用问题。
- 下一步实机验证重点：`/br resources`、`/br resource info level0_loose_carpet`、`/br resource info level1_scrap_ore`，然后把默认 `locations` 替换为真实地图坐标。

### 12.11 Step 014 CraftEngine Backrooms 资产扩展状态

- 已根据本地 CraftEngine wiki 与默认资源示例继续扩展 `backrooms` 资源包配置，不接入 CE Java API。
- `materials.yml` 新增保险丝、布料、管道、手电框架、工具箱等 Backrooms 生存/基地升级材料。
- `mvp_blocks.yml` 新增维护门标记、撤离口标记、基地 claim 终端、发电机核心、闪烁荧光灯等 block item 与 block 定义。
- 新增方块继续复用原版 model/texture 生成，不引入自定义美术资源，保持 MVP 阶段低成本可测试。
- 项目内 `server-configs/CraftEngine/resources/backrooms/**` 已同步到测试服 `D:\dev\backrooms\devserver\plugins\CraftEngine\resources\backrooms/**`。
- 下一步实机验证重点：执行 CraftEngine `/ce reload all`，再用 `/ce item get` 或 `/ce item give` 检查新增物品/方块是否能生成、摆放、掉落和被资源包正确显示。

### 12.12 Step 015 README 测试入口状态

- 已新增 `README.md`，把 `plan.md` 简化为项目定位、已完成 TODO、未完成 TODO 和实机测试入口。
- README 覆盖构建部署、CraftEngine 资产测试、Loot/Resource 测试、Transition 测试、Room 占位生成测试、保护规则测试和常用排查命令。
- README 明确当前地图生成仍是占位 Room/走廊生成器，不是最终自动迷宫系统；默认 `replace-air-only: true`，测试时应站在空旷区域。
- 下一步继续按 README 的测试顺序重启测试服验证闭环，并把资源点与 Transition 坐标替换为真实地图坐标。

### 12.13 Step 016 Paper 命令注册修复状态

- 已修复 Paper 1.21.4 启动时报错：Paper plugin 不支持 `paper-plugin.yml` 的 YAML command 声明，也不能在启动期通过 `JavaPlugin#getCommand()` 获取该命令。
- `/br` 现在通过 Paper `JavaPlugin#registerCommand` 注册 `BasicCommand`，内部复用原有 `BrCommand` 的执行和补全逻辑。
- `paper-plugin.yml` 已移除 `commands:` 块，只保留权限声明；`/br` 和 `backrooms` 别名由 Paper command API 注册。
- 下一步实机验证重点：部署新 jar 后完整重启测试服，确认启动阶段不再出现 `Paper plugins do not support YAML-based command declarations`，再测试 `/br`、`/br help`、tab completion 和 `/br debug config`。

### 12.14 Step 017 世界创建文档补充状态

- README 已补充 Multiverse-Core 世界创建/导入命令，明确当前必须存在 `lobby`、`level_0`、`level_1` 三个 world。
- 当前 BackroomsCore 不提供自定义 world generator，也不会自动创建世界；MVP 测试建议用 Multiverse `-t FLAT` 创建平地世界，不需要指定 `-g`。
- README 已说明已有世界文件夹时使用 `/mv import`，创建或导入后执行 `/br reload` 和 `/br debug config` 检查 missing worlds。
- 下一步仍是实机重启验证 `/br` 启动修复，然后按 README 创建世界、检查配置、测试 Transition/Room/Resource 闭环。

### 12.15 Step 018 synthetic enum switch 运行时修复状态

- 已修复 `/br transition info level0_to_level1_stairwell` 触发的 `NoClassDefFoundError: TransitionDefinition$1`。
- 已移除 Transition/Room 运行路径中的 enum switch 表达式，避免生成 `TransitionDefinition$1`、`TransitionService$1`、`RoomGenerationService$1` 这类 synthetic helper class。
- 已通过 `clean build` 验证新 jar 中不再包含这些 `$1.class` 文件。
- 下一步实机验证重点：部署新 jar 并完整重启后，测试 `/br transition info level0_to_level1_stairwell`、`/br transition guide level0_to_level1_stairwell` 和 `/br room generate ...`。

### 12.16 Step 019 FAWE schematic 有限区域 worldgen 状态

- 已新增 `worldgen.yml`，用于配置 Level 0 schematic 模板元数据、`16x16x6` cell 默认尺寸、主路径长度、分支率、loop 参数、vanilla marker 材质和 generated-regions 持久化文件。
- 已新增 `WorldGenerationService` 与 schematic 模板模型，支持读取模板 connectors、tags、weight、rotations、footprint、unique、min-distance-from-spawn-cells 与 paste-air 配置。
- 已新增有限区域生成命令 `/br worldgen generate <level> <size> [seed]`，先生成有限 N x N cell 图，再按连接口和权重选择模板，最后通过 WorldEdit/FAWE API 粘贴 schematic。
- 已新增 `/br worldgen templates` 调试命令，用于查看当前加载的模板文件、连接口、footprint、tags 与权重。
- 已新增 marker 扫描 MVP：粘贴后扫描配置的 vanilla marker block 数量，并把 region 元数据写入 `generated-regions.yml`，避免同一区域重复生成。
- 已新增 `backrooms.command.worldgen.templates`、`backrooms.command.worldgen.generate` 权限，并接入 `/br help`、tab completion、`/br debug config`、README 和插件启动/重载日志。
- WorldEdit 依赖使用 `compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.3.10")`，这是为 Paper 1.21.4 / Java 21 选择的可编译版本；运行时仍需要测试服安装 WorldEdit 或 FAWE。
- 下一步重点：制作真实 `plugins/BackroomsCore/templates/level_0/*.schem`，实机测试 `/br worldgen templates` 与 `/br worldgen generate level_0 9 seed123`，再继续做房间编辑框和 schematic 保存流程。

### 12.17 Step 020 Faithful Level 0 建图资产导入状态

- 已从 `D:\dev\backrooms\faithfulbackrooms` 筛选适合 Level 0 建图的模型资产，导入到 CraftEngine `backrooms` 资源包的 `assets/backrooms/models/*/faithful/` 与 `assets/backrooms/textures/block/faithful/`。
- 已导入 47 个 block model、28 个 custom model、47 个 item wrapper model 和 51 张 block texture，用于墙纸、地毯、天花板、灯具、crate、柜子、管道、踢脚线、标牌、门和监控等 Level 0 建图素材。
- 已新增 `configuration/blocks/faithful_level0_blocks.yml`，将导入模型配置为 `backrooms:faithful_*` CraftEngine 方块与 block item。
- 配置原则：完整墙体/地面/天花板/箱子使用 `auto_state: note_block`，避免 `solid` 自动分配到 mushroom 系列透明/遮挡不合适的状态；灯具、管道、牌子、插座、CCTV 等装饰使用 `auto_state: lower_tripwire` 并关闭 suffocation、view blocking 与 occlusion；crate 系列临时使用 `simple_storage_block`。
- 已新增 `docs/level0-cell-guide.md`，系统说明 `16x16x6` cell 规格、门洞位置、模板类型、marker 放置和真/假楼梯井区分。
- 已新增 `docs/faithful-assets-ce.md`，说明本次 Faithful 资产导入路径、CE 配置原则、方块 ID 和实机验证命令。
- README 已补充 Faithful Level 0 CE 建图资产状态和实机验证 TODO。
- 下一步重点：同步资源到测试服后执行 `/ce reload all`，验证 `backrooms:faithful_yellow_wallpaper`、`backrooms:faithful_old_carpet`、`backrooms:faithful_ceiling_light`、`backrooms:faithful_crate`、`backrooms:faithful_exit_sign` 的模型、碰撞、灯光和 storage 行为。

### 12.18 Step 021 CraftEngine 纹理路径警告修复状态

- 已根据测试服 CraftEngine reload 日志修复 vanilla 纹理路径错误。
- `backrooms:fuse` 已从 `minecraft:item/redstone_torch` 改为 `minecraft:block/redstone_torch`。
- `backrooms:pipe_segment` 已从 `minecraft:item/iron_bars` 改为 `minecraft:block/iron_bars`。
- `backrooms:evacuation_hatch_marker` 已从 `minecraft:item/iron_trapdoor` 改为 `minecraft:block/iron_trapdoor`。
- 项目内 `server-configs` 与测试服 `devserver/plugins/CraftEngine/resources/backrooms` 的对应配置均已同步修复。
- 日志中提到的 Faithful item model 文件已确认存在于项目资源包和测试服资源包；如果再次出现，优先执行 `/ce clean-cache` 后再 `/ce reload all` 排除缓存或旧 pack 状态。
- 下一步重点：重新执行 `/ce reload all`，确认上述三个 vanilla texture 警告消失，并继续验证 `backrooms:faithful_*` 建图方块模型、碰撞、灯光和 storage 行为。

### 12.19 Step 022 CraftEngine resourcepack 资产目录修复状态

- 已确认 `backrooms:faithful_*` item model 文件本身存在，但之前放在 `resources/backrooms/assets/...` 下，CraftEngine reload 仍提示缺失，说明该路径没有被资源包打包流程扫描。
- 已将项目内 Faithful 资源包文件移动到 `server-configs/CraftEngine/resources/backrooms/resourcepack/assets/...`。
- 已将测试服 CraftEngine 资源目录同步移动到 `D:\dev\backrooms\devserver\plugins\CraftEngine\resources\backrooms\resourcepack\assets/...`。
- 已更新 `docs/faithful-assets-ce.md`，明确 CE 资源包文件必须位于资源目录的 `resourcepack/assets/...` 下。
- 已确认项目与测试服旧 `assets` 目录均不存在，新目录下存在 `resourcepack/assets/backrooms/models/item/faithful/manilla_wallpaper.json`。
- 下一步重点：执行 `/ce clean-cache` 后再 `/ce reload all`，确认 Faithful item model 缺失警告消失；如果仍缺失，检查 CE 最终生成资源包 zip 中是否包含 `assets/backrooms/models/item/faithful/*.json`。

### 12.20 Step 023 CraftEngine solid 状态改为 note_block 状态

- 已根据实机观察修正 CraftEngine 透明/遮挡状态选择：`solid` 自动状态可能分配到 mushroom 系列 block state，当前资源包下透明/遮挡表现不适合 Level 0 建图方块。
- 已将项目内 `mvp_blocks.yml` 与 `faithful_level0_blocks.yml` 中完整方块的 `auto_state: solid` 改为 `auto_state: note_block`。
- 非完整装饰方块继续使用 `auto_state: lower_tripwire`，并保留关闭 suffocation、view blocking、occlusion 的设置。
- 已同步测试服 CraftEngine 配置到 `D:\dev\backrooms\devserver\plugins\CraftEngine\resources\backrooms\configuration\blocks\`。
- 已更新 `docs/faithful-assets-ce.md` 与 `docs/level0-cell-guide.md`，明确完整方块不要使用 `solid`，优先使用 `note_block`。
- 下一步重点：执行 `/ce reload all`，验证墙体、地毯、天花板、crate 等完整方块不再出现 mushroom 相关透明/遮挡问题；如果 CE 提示 note_block 状态不足，再按方块类别拆分到其他稳定状态池。

### 12.21 Step 024 Backrooms Item 与理智 HUD MVP 状态

- 已新增 `src/main/java/org/monday/backrooms/items/` 模块，包含 Backrooms 物品定义、物品服务、右键监听、理智效果与理智服务。
- 已新增 `items.yml`，统一配置 Backrooms Item、消耗行为、冷却、替换物、理智恢复、稳定时间和 VectorDisplays HUD 格式。
- 已实现理智值 MVP：玩家在 Backrooms Level 中按配置持续降低理智，Level 1 衰减高于 Level 0；低理智和危急状态会定期发送提示。
- 杏仁水已作为第一批核心物品接入：右键饮用恢复理智并给予稳定时间；同时新增皇家杏仁水、记忆盐等可扩展物品设定。
- HUD 不走 Paper 动作栏，当前默认接入 VectorDisplays 世界内悬浮终端；后续可继续扩展 Item Display、TAB/scoreboard 或 display entity 做更强玩家 UI。
- 已新增 `/br items`、`/br item info <id>`、`/br item give <id> [player] [amount]`，并加入 help、tab completion、messages 和 `paper-plugin.yml` 权限。
- Loot Table 与 Resource Drop 已支持 `item: backrooms:<id>`，继续兼容旧的 `material:` 产出；Level 0/1 默认 loot 已接入 Backrooms 自定义物品。
- 当前暂不硬依赖 CraftEngine Java API，BackroomsCore 生成的物品使用 PDC 识别；对外部插件生成的同名物品提供显示名 fallback。下一步需要实机确认 `/ce item get backrooms:almond_water` 是否能被稳定识别，必要时再引入 CE API 或更可靠的 namespace 标记识别。
- 下一步重点：部署新 jar 后测试 `/br reload`、`/br debug config`、`/br items`、`/br item give backrooms:almond_water`、杏仁水右键恢复理智、VectorDisplays HUD，以及 `/br loot roll level0_basic_supplies` 是否产出配置物品。

### 12.22 Step 026 VectorDisplays 理智 HUD 接入状态

- 已学习 VectorDisplays API 用法，并在 `src/main/java/org/monday/backrooms/hud/` 新增 HUD 抽象与 VectorDisplays provider。
- 理智系统不再发送 Paper 动作栏；`SanityService` 只生成 `SanityHudSnapshot`，由 HUD provider 决定怎么显示。
- 默认 HUD provider 为 `VECTOR_DISPLAYS`，使用 `SimpleTerminal`、`Label`、`Line`、`TerminalManager` 在玩家前方生成世界内悬浮终端面板和理智进度条。
- `paper-plugin.yml` 已声明 VectorDisplays 软依赖，`build.gradle` 已添加 `top.mrxiaom.hologram:VectorDisplays-API:1.1.1:for-plugin` compileOnly 依赖。
- 如果服务器没有安装 VectorDisplays 或前置 packetevents，HUD 会 Noop 降级并只输出一次警告；理智衰减、低理智提示和杏仁水物品逻辑仍可运行。

### 12.23 Step 027 staged reload 与异步传送收敛状态

- 已确认当前代码中 `/br level tp` 和 Transition 都通过 `PaperTeleports.teleportAsync(...)` 调用 Paper 异步传送 API，未发现同步 `.teleport(...)` 调用残留。
- 已将 `Backrooms#reloadRuntimeConfig()` 改为 staged runtime reload：配置文件、消息、Level registry、Item、Sanity HUD、Sanity、Loot、Resource、Transition、Room、Worldgen 都先装入临时 runtime snapshot。
- staged reload 成功后才统一提交到 live 字段；如果中途抛出异常或 Level 全部加载失败，会丢弃 staged runtime，并保留旧 live runtime。
- `SanityService` 新增运行时状态复制，reload 替换新实例时保留玩家理智值、稳定时间和低理智提示冷却；旧 task 会在提交后停止，新 service 会启动自己的 tick task。
- 已运行 `.\gradlew.bat build`，构建通过；已运行 `.\gradlew.bat deployDevServer`，最新 jar 已部署到测试服 `plugins` 目录。
- 下一步重点：完整重启测试服加载新 jar，执行 `/br reload`、`/br debug config`、`/br level tp level_0`、`/br transition trigger level0_to_level1_stairwell <player>`，确认 staged reload 与异步传送在实机运行路径中正常。

### 12.24 Step 028 原版容器 Loot Source MVP 状态

- 已新增 `LootSourceService`、`LootSourceListener`、`LootSourceDefinition`、`LootSourceType` 和 `LootSourcePosition`。
- `loot.yml` 新增 `loot-sources.definitions`，当前支持 `type: vanilla_container`，通过 Level、Material、可选坐标和 loot table 列表匹配容器。
- 玩家打开匹配的原版 `CHEST` / `BARREL` 时，系统会向容器注入对应 Loot Table 产物；背包格不足时，多余物品掉落到容器上方。
- `one-time: true` 会在 TileState PDC 中写入生成标记，避免容器重复刷 loot；非 TileState 容器有运行时 fallback 标记。
- `/br reload` staged runtime 已接入 Loot Source，`/br debug config` 已显示 Loot sources 数量。
- 默认配置提供两个占位容器源：`level0_supply_container` 与 `level1_scrap_container`，坐标均为 `x=4 y=64 z=0`，后续真实地图制作完成后需要替换。
- 已运行 `.\gradlew.bat build`，构建通过；已运行 `.\gradlew.bat deployDevServer`，最新 jar 已部署到测试服 `plugins` 目录。
- 已同步 `loot.yml` 与 `messages.yml` 到测试服，并用 Java 21 重启测试服验证：BackroomsCore 日志显示 `Loaded loot sources: enabled=true, definitions=2, skipped=0` 和 `lootSources=2`。
- 下一步重点：完整重启测试服，在 `level_0` 和 `level_1` 的占位坐标放置空 `CHEST` 或 `BARREL`，执行 `/br reload` 后打开容器，确认首次生成与 one-time 防重复生效。

### 12.25 Step 029 测试服配置同步任务状态

- 已新增 Gradle `syncDevServerConfig`，将 `src/main/resources` 中的 BackroomsCore 运行时 YAML 同步到 `D:\dev\backrooms\devserver\plugins\backrooms`。
- 已新增 Gradle `deployDevServerAll`，同时执行 jar 部署和 BackroomsCore YAML 同步。
- README 构建与部署章节已补充 `syncDevServerConfig` 和 `deployDevServerAll`。
- 已运行 `.\gradlew.bat syncDevServerConfig build`，任务与构建均通过。
- 下一步重点：后续每次修改 `src/main/resources/*.yml` 后，使用 `syncDevServerConfig` 或 `deployDevServerAll` 保证测试服实际加载的 YAML 与仓库一致。

### 12.26 Step 030 Loot Source 调试命令状态

- 已新增 `/br loot sources`，列出当前加载的 Loot Source id、type、levels、materials、loot tables 和 enabled 状态。
- 已新增 `/br loot source info <id>`，显示单个 Loot Source 的 type、levels、materials、locations、loot tables、one-time 和 fill-empty-only。
- 已新增权限 `backrooms.command.loot.source.list` 与 `backrooms.command.loot.source.info`，并加入 `backrooms.admin`。
- README 的 Loot / Resource 测试流程已补充 Loot Source 调试命令。
- 已运行 `.\gradlew.bat deployDevServerAll build`，部署、配置同步和构建均通过；测试服用 Java 21 重启后，BackroomsCore 启动日志显示 `lootSources=2`、监听器注册成功、`/br` 命令注册成功。
- 下一步重点：玩家进服后实际执行 `/br loot sources`、`/br loot source info level0_supply_container`，再打开占位容器验证 one-time 生成。

### 12.27 Step 031 Loot Source direct reward 状态

- 已扩展 Loot Source 类型：`event_reward` 与 `command_reward`，用于事件系统、任务脚本、管理员命令直接向玩家发放命名 Loot Table。
- 已新增 `LootSourceService#triggerReward(String id, Player player)`，返回结构化状态：not found、disabled、unsupported type、level mismatch、already generated、empty 和 success。
- direct reward source 支持按玩家所在 Level 过滤；`one-time: true` 会写入玩家 PDC，避免同一玩家重复领取一次性事件奖励。
- 已新增 `/br loot source trigger <id> [player]`，并加入 tab completion、help、messages 和权限 `backrooms.command.loot.source.trigger`。
- 默认 `loot.yml` 新增 `level0_event_supply_reward` 与 `admin_scrap_reward`，分别覆盖一次性 Level 0 事件补给与可重复管理员/脚本奖励。
- 已运行 `.\gradlew.bat deployDevServerAll build` 并用 Java 21 重启测试服；BackroomsCore 启动日志显示 `lootSources=4`。
- 下一步重点：把 CE 尸体/容器交互或未来事件系统接到 `triggerReward(...)`，继续减少手动命令和占位坐标。

### 12.28 Step 032 尸体缓存 MVP 状态

- 已新增 `corpses.yml`，配置尸体系统启用状态、适用 Level、原版容器材料、保险物品堆数量、死亡点附近搜索半径和剩余物品处理策略。
- 已新增 `CorpseService` 与 `CorpseListener`：监听 `PlayerDeathEvent`，在 Backrooms Level 中把普通死亡掉落转移到死亡点附近的原版容器；监听 `PlayerRespawnEvent`，把保险物品堆返还给玩家。
- 尸体容器写入 PDC：owner uuid、owner name、created at，后续可用于权限、衰变、搜刮状态和 CE 尸体方块替换。
- `/br reload` staged runtime 已接入 `CorpseService`，并复制 pending insurance 状态，避免 reload 后丢失等待重生返还的保险物品。
- `/br debug config` 已显示 `Pending insurance`，用于检查等待返还保险物品的玩家数量。
- 已运行 `.\gradlew.bat deployDevServerAll build` 并用 Java 21 重启测试服；BackroomsCore 启动日志显示 corpse config 加载成功、`pendingInsurance=0`、`CorpseListener` 注册成功。
- 下一步重点：实机让玩家在 `level_0` / `level_1` 死亡，确认尸体 `CHEST` 生成、掉落物转移、保险物品重生返还和容器无空间时的 fallback。

### 12.29 Step 033 基地 claim MVP 状态

- 已新增 `bases.yml`，配置 base 系统启用状态、claim 数据文件、玩家 claim 上限和固定可 claim 区域。
- 默认提供两个 Level 1 占位区域：`level1_utility_room_a` 与 `level1_storage_room_a`，后续真实地图制作后替换为实际房间坐标。
- 已新增 `BaseService`，加载 base definitions，读写 `base-claims.yml`，并提供 owner build 判定。
- 已新增 `/br bases`、`/br base info <id>`、`/br base claim <id>`，并加入 help、tab completion、messages 和权限。
- `LevelRuleListener` 已接入 `plugin.bases().canBuild(...)`：Level 禁止普通建造时，已 claim 区域内 owner 可以破坏/放置，区域外继续拦截。
- `/br debug config` 已显示 base definitions 与 claims 数量。
- 已运行 `.\gradlew.bat deployDevServerAll build` 并用 Java 21 重启测试服；BackroomsCore 启动日志显示 `bases=2`、`baseClaims=0`。
- 下一步重点：实机在 Level 1 占位区域执行 claim，确认 `base-claims.yml` 持久化、owner 区域内可建造、非 owner 和区域外仍被保护。
