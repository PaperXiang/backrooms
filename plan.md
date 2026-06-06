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

- 完成第一批 CraftEngine 26.6 Backrooms 测试物品/方块配置，模型先复用 Minecraft 原版 model/texture，后续再替换美术资源。
- 完成 TAB 测试服基础显示配置，减少演示占位符和无关动画。
- 增加 Level 随机 spawn 点配置，先用于 `/br level tp` 和未来切层入口，避免所有玩家永远落在单一坐标。
- 下一阶段实现正式 Transition/撤离点系统：Level 0 -> Level 1，Level 1 -> lobby。

### 12.2 为什么当前仍是第一阶段 MVP

当前阶段重点是把服务器跑起来并形成可测试闭环：Level 配置、跨世界传送、Level 规则、资源方块抽象、CE 测试资产、基础显示配置。世界生成、房间拼接、尸体背包保存、基地 claim、组织系统和真实 loot 刷新仍属于后续 MVP 任务，因为它们依赖已稳定的 Level/资源/切层基础设施。先做小而稳的闭环，能降低后续 WorldEdit/CE/Multiverse 实机集成时的排错成本。

### 12.3 Step 006 完成状态

- 已新增第一批 CraftEngine 26.6 `backrooms` 资源包配置，包含杏仁水、电池、废料、电线、发电机部件、MEG 芯片、钥匙卡、物资箱、尸体缓存、楼梯井标记等测试资产。
- 已将外部插件配置版本化到 `server-configs/`，并同步到 `devserver/plugins/`。
- 已为 Level 配置增加 `spawn.points`，当前 `/br level tp <id>` 会在多个配置点中随机选择，未来 Transition/撤离点系统也复用这一能力。
- 已增加 `deployDevServer` Gradle task，用于把 BackroomsCore jar 部署到本地测试服 `plugins` 目录。
- 地图/房间生成仍未实现：当前只完成测试服资源和随机 spawn 基础设施，下一步最高优先级是 Transition/撤离点系统，然后再做房间模板和生成原型。
