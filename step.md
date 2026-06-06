# 开发记录

## Step 001 - 初始化计划与仓库准备

### 本次完成

- 根据当前讨论整理并创建中文 `plan.md`。
- 明确 BackroomsCore 的 MVP 方向：Paper 1.21.4、Level 0 + Level 1、PVE 生存恐怖、开放世界式 Level 穿梭。
- 确认世界结构：`lobby` 作为 MEG 主基地，`level_0` 作为阈限体验层，`level_1` 作为基地、资源竞争和撤离层。
- 确认基地系统只在 Level 1 内实现，玩家在可改造房间 claim，组织名称方向为 Survivor Cell。
- 确认 CraftEngine 后续用于自定义物品、方块、资源点、容器和尸体表现。

### 修改文件

- `plan.md`
- `step.md`

### 设计原因

- `plan.md` 用于记录长期设计，避免后续开发偏离目标。
- `step.md` 用于记录每次修改总结，方便回溯每一步开发决策。
- 当前阶段先确定长期架构和玩法边界，再逐步实现代码与配置。

### 下一步建议

- 创建 GitHub 仓库并完成 first commit。
- 后续开始搭建基础插件框架：配置加载、MiniMessage 消息工具、主命令 `/br`、Level 注册表。
- 等服务端环境准备好后，再编写 CraftEngine 测试物品和方块配置。

### 测试与验证

- 本步骤主要是文档和仓库初始化准备，尚未运行代码测试。

## Step 002 - 基础插件框架

### 本次完成

- 查询并核对本地 CraftEngine 文档，确认第一阶段只做可选集成预留，不直接依赖 CE 非稳定 API。
- 查询并核对当前 Paper 项目基础实现方式，确认第一阶段使用 Bukkit 经典命令 API、JavaPlugin 配置 API、Adventure MiniMessage 和自定义 Level 注册表。
- 新增默认 `config.yml`，包含基础消息配置与 `level_0`、`level_1` 两个 MVP Level 配置。
- 新增 MiniMessage 消息服务，支持 prefix、单行/多行消息、文本占位符、MiniMessage 占位符和布尔值占位符。
- 新增 Level 数据模型、配置加载器和注册表。
- 新增 `/br` 主命令，支持：
  - `/br help`
  - `/br levels`
  - `/br level <id>`
  - `/br reload`
- 在 `paper-plugin.yml` 中声明 `/br` 命令、`backrooms` 别名和基础权限。
- 将 run-paper 测试服务端版本从 `1.21` 调整为 `1.21.4`，保持与 Paper API 依赖一致。

### 修改文件

- `build.gradle`
- `src/main/resources/paper-plugin.yml`
- `src/main/resources/config.yml`
- `src/main/java/org/monday/backrooms/Backrooms.java`
- `src/main/java/org/monday/backrooms/command/BrCommand.java`
- `src/main/java/org/monday/backrooms/message/MessageService.java`
- `src/main/java/org/monday/backrooms/level/BackroomsLevel.java`
- `src/main/java/org/monday/backrooms/level/LevelConfigLoader.java`
- `src/main/java/org/monday/backrooms/level/LevelRegistry.java`
- `step.md`

### 设计原因

- 第一阶段先做稳定基础设施，不提前接入 CraftEngine、MythicMobs、WorldGuard 等外部插件，避免在服务器环境未确定前引入不必要的类加载和版本风险。
- `/br` 使用 Bukkit `TabExecutor` 与 `paper-plugin.yml` 声明命令，避免过早使用 Paper Brigadier/lifecycle 命令 API。
- Level 使用插件内部 `Map<String, BackroomsLevel>` 注册表，不使用 Paper/Minecraft Registry，因为 Backrooms Level 是业务概念，不是原版注册项。
- 消息统一使用 Adventure MiniMessage，方便后续 Title、ActionBar、GUI 文本和 lore 复用同一套格式。
- 默认配置先把 `level_0`、`level_1` 放在 `config.yml`，后续内容复杂后再拆分到 `levels/`、`messages/` 等独立文件。

### 下一步建议

- 增加玩家进入 Level 的基础运行时状态，例如当前 Level、进入时间、是否位于 Backrooms 世界。
- 实现 `/br level tp <id>` 管理命令，用于快速传送测试 Level 世界。
- 增加进入 Level 时的 Title/Subtitle 展示，复用配置中的 `title` 与 `subtitle`。
- 预留外部插件检测层，但仍不直接调用 CraftEngine API，等服务器和 CE 版本确认后再写 Adapter。

### 测试与验证

- 已运行 `./gradlew.bat build`，构建通过。
- 已读取 IDE lints，当前新增 Java 文件未报告诊断问题。

## Step 003 - Level 传送与运行时状态

### 本次完成

- 并行调研 MCIO PluginBase 与 VectorDisplays 文档，确认它们适合作为后续架构/沉浸式 UI 参考，但当前阶段不作为硬依赖引入。
- 确认服务器目标版本为 Paper 1.21.4，CraftEngine 目标版本为 26.6；本步骤仍不直接调用 CE API，避免服务器环境未落地前引入类加载风险。
- 为 Level 增加可选 spawn 配置，支持 `x`、`y`、`z`、`yaw`、`pitch`。
- 新增 `/br level tp <id>` 管理命令，用于传送到已配置且已加载的 Level 世界。
- 保留 `/br level <id>` 查看详情，并新增 `/br level info <id>` 作为更明确的详情命令。
- 新增 Level Title 服务，进入 Level 时使用 Adventure Title 显示配置中的 `title` 与 `subtitle`。
- 新增玩家当前 Level 运行时状态追踪，按 UUID 记录玩家当前 Level、所在世界和进入时间。
- 监听玩家进服、退服和跨世界事件，同步玩家当前 Level 状态。
- 配置重载后会重新协调在线玩家的 Level 状态。
- 在 `paper-plugin.yml` 中增加 `backrooms.command.level.tp` 权限。

### 修改文件

- `src/main/resources/config.yml`
- `src/main/resources/paper-plugin.yml`
- `src/main/java/org/monday/backrooms/Backrooms.java`
- `src/main/java/org/monday/backrooms/command/BrCommand.java`
- `src/main/java/org/monday/backrooms/level/BackroomsLevel.java`
- `src/main/java/org/monday/backrooms/level/LevelConfigLoader.java`
- `src/main/java/org/monday/backrooms/level/LevelRegistry.java`
- `src/main/java/org/monday/backrooms/level/LevelSpawn.java`
- `src/main/java/org/monday/backrooms/level/LevelTitleService.java`
- `src/main/java/org/monday/backrooms/player/PlayerLevelState.java`
- `src/main/java/org/monday/backrooms/player/PlayerLevelTracker.java`
- `src/main/java/org/monday/backrooms/player/PlayerLevelListener.java`
- `step.md`

### 设计原因

- `/br level tp <id>` 只传送到已经加载的世界，不自动创建世界，避免提前和 Multiverse-Core 或后续世界管理模块产生职责冲突。
- 进入提示使用 Adventure `Title` API，而不是旧式字符串 title 或 NMS 发包，保持 Paper 1.21.4 下的稳定实现方式。
- 玩家状态只保存在运行时内存中，且以 UUID 作为 key，避免保存 `Player` 对象导致退出后引用残留。
- Level 的世界识别暂时采用一个 Level 对应一个世界的方式，满足 MVP；后续如果做区域级 Level 或实例世界，再扩展识别规则。
- MCIO PluginBase 与 VectorDisplays 暂不接入，当前先保留轻量 Paper-only 核心；VectorDisplays 后续可作为 MEG 终端、基地控制台、楼梯井面板等沉浸式 UI 的可选方案。

### 下一步建议

- 实现基础 Level 规则监听，例如根据当前 Level 禁止普通建筑破坏、按配置控制 PVP。
- 增加资源方块/可交互方块的抽象，先做原版方块占位，后续接入 CraftEngine 26.6 Adapter。
- 增加 `/br debug current` 或 `/br player state`，方便查看玩家当前 Level 状态。
- 等测试服务器和 Level 世界创建好后，实机验证 `/br level tp level_0`、`/br level tp level_1` 和跨世界 Title 触发。

### 测试与验证

- 已运行 `./gradlew.bat build`，构建通过。
- 已读取 IDE lints，当前新增 Java 文件未报告诊断问题。

## Step 004 - Level 规则与资源方块抽象

### 本次完成

- 并行核对 Paper/Bukkit 1.21.4 事件 API 与 CraftEngine 26.6 文档，确认本阶段使用 Bukkit 稳定事件实现 Level 规则，不直接接入 CE API。
- 新增 `LevelRules` 配置模型，支持：
  - `allow-block-break`
  - `allow-block-place`
  - `resource-interaction`
- 新增 Level 规则监听器，按玩家当前 Level 控制：
  - 普通建筑破坏。
  - 普通方块放置。
  - PVP 伤害。
  - 资源方块右键交互。
- 新增 `backrooms.bypass.build` 权限，方便管理员绕过建筑保护进行地图测试。
- 新增资源方块抽象，当前仅支持原版 `Material` 匹配，为后续 CraftEngine Adapter 预留边界。
- 新增资源触发类型：
  - `BREAK`
  - `RIGHT_CLICK`
- 新增基础资源掉落配置，支持 `material`、`chance`、`min`、`max`。
- 新增资源点冷却提示与简单冷却记录。
- 新增 `/br debug current`，用于查看玩家当前运行时 Level 状态、世界识别 Level 与规则状态。
- 在默认配置中加入 Level 0/1 的规则配置与两个测试资源点：
  - Level 0：右键 `YELLOW_CARPET` 概率掉落 `PAPER`。
  - Level 1：破坏 `IRON_ORE` / `COPPER_ORE` 掉落废料占位物。

### 修改文件

- `src/main/resources/config.yml`
- `src/main/resources/paper-plugin.yml`
- `src/main/java/org/monday/backrooms/Backrooms.java`
- `src/main/java/org/monday/backrooms/command/BrCommand.java`
- `src/main/java/org/monday/backrooms/level/BackroomsLevel.java`
- `src/main/java/org/monday/backrooms/level/LevelConfigLoader.java`
- `src/main/java/org/monday/backrooms/level/LevelRules.java`
- `src/main/java/org/monday/backrooms/resource/ResourceBlockDefinition.java`
- `src/main/java/org/monday/backrooms/resource/ResourceBlockService.java`
- `src/main/java/org/monday/backrooms/resource/ResourceDrop.java`
- `src/main/java/org/monday/backrooms/resource/ResourceTrigger.java`
- `src/main/java/org/monday/backrooms/rule/LevelRuleListener.java`
- `step.md`

### 设计原因

- Level 规则使用 Bukkit 事件层取消行为，而不是依赖 `World#setPVP` 或 Multiverse 世界级设置，方便后续扩展区域 PVP、基地规则和事件规则。
- 普通建筑保护与资源方块处理分离，符合“普通建筑不可破坏，资源方块可交互”的服务器设计。
- 资源方块先用原版 `Material` 做业务闭环，后续接入 CraftEngine 26.6 时只需要增加识别 Adapter，不需要重写规则监听层。
- CraftEngine 文档建议通过 Bukkit 原生事件与 BlockData 识别自定义方块，因此当前不监听 CE 专属事件，不调用 CE 内部 API。
- `/br debug current` 同时显示 tracker 状态和世界识别状态，便于后续实机测试时排查配置、传送或跨世界同步问题。

### 下一步建议

- 实机验证 Level 0/1 世界中的破坏、放置、PVP 和资源方块交互行为。
- 增加资源方块坐标级注册或 marker 机制，避免仅靠 Material 导致同材质建筑方块被误判为资源点。
- 增加基础交互方块抽象，例如楼梯井、维护门、撤离点，为 Level 0 -> Level 1 与 Level 1 -> lobby 的正式切层做准备。
- 后续在服务器与 CraftEngine 26.6 环境稳定后，实现 CraftEngine Adapter：按 CE block id 匹配资源方块、尸体方块和特殊容器。

### 测试与验证

- 已运行 `./gradlew.bat build`，构建通过。
- 已读取 IDE lints，当前新增 Java 文件未报告诊断问题。

## Step 005 - 拆分运行时配置与增强控制台日志

### 本次完成

- 新增 `ConfigFileService`，统一负责插件运行时配置文件的默认生成、加载和重载。
- 将原本集中在 `config.yml` 的运行时配置拆分为更便于维护的独立 YAML：
  - `messages.yml`：聊天消息、帮助文本、调试文本与命令反馈。
  - `settings/config.yml`：Level Title 等通用运行时设置。
  - `resources.yml`：资源方块总开关与资源点定义。
  - `levels/level_0.yml`：Level 0 配置。
  - `levels/level_1.yml`：Level 1 配置。
- 将根 `config.yml` 改为保留用的 legacy/global stub，避免后续根配置继续膨胀。
- 调整消息服务，使 `MessageService` 从 `messages.yml` 读取所有 MiniMessage 文本。
- 调整 Level Title 服务，使标题开关和时间参数从 `settings/config.yml` 读取。
- 调整 Level 配置加载器，使其扫描 `levels/*.yml` 并加载每个独立 Level 文件。
- 调整资源方块服务，使其从 `resources.yml` 读取资源点定义。
- 增加更多控制台日志，覆盖：
  - 插件启用版本。
  - 默认配置文件创建。
  - Level 配置文件加载数量。
  - Level 总数、启用数、禁用数。
  - 资源方块定义数、跳过数和掉落条目数。
  - Listener 与 `/br` 命令注册。
  - 管理员触发 `/br reload`。
  - 重载耗时、在线玩家数与运行时配置摘要。
- 对旧 `config.yml` 中仍存在的 `messages`、`level-title`、`resource-blocks`、`levels` section 增加忽略警告，方便旧配置迁移时发现问题。

### 修改文件

- `src/main/resources/config.yml`
- `src/main/resources/messages.yml`
- `src/main/resources/settings/config.yml`
- `src/main/resources/resources.yml`
- `src/main/resources/levels/level_0.yml`
- `src/main/resources/levels/level_1.yml`
- `src/main/java/org/monday/backrooms/Backrooms.java`
- `src/main/java/org/monday/backrooms/config/ConfigFileService.java`
- `src/main/java/org/monday/backrooms/command/BrCommand.java`
- `src/main/java/org/monday/backrooms/message/MessageService.java`
- `src/main/java/org/monday/backrooms/level/LevelConfigLoader.java`
- `src/main/java/org/monday/backrooms/level/LevelRegistry.java`
- `src/main/java/org/monday/backrooms/level/LevelTitleService.java`
- `src/main/java/org/monday/backrooms/resource/ResourceBlockService.java`
- `step.md`

### 设计原因

- 消息、Level、资源点和通用设置的编辑频率不同，拆分后更适合实机调参，也更适合后续让不同模块独立扩展。
- `levels/*.yml` 的结构可以让每个 Backrooms Level 独立维护，后续新增 Level 不需要反复编辑一个巨大的根配置文件。
- `resources.yml` 先保持原版 `Material` 配置格式，继续为 CraftEngine Adapter 预留识别边界。
- 根 `config.yml` 保留为空壳，兼容 Bukkit 默认配置生命周期，也为未来真正全局开关预留位置。
- 增强控制台摘要日志后，服务端启动和 `/br reload` 时能快速确认配置是否加载成功、世界是否缺失、资源定义是否生效。

### 下一步建议

- 实机启动服务器，确认首次运行会自动释放新的拆分配置文件。
- 将旧服已有 `config.yml` 内容迁移到新的拆分文件后，删除旧 section，避免控制台重复出现 legacy 警告。
- 增加基础交互点抽象，例如楼梯井、维护门、撤离点，为 Level 0 -> Level 1 与 Level 1 -> lobby 的正式切层做准备。
- 后续增加配置校验命令，例如 `/br debug config`，输出缺失世界、重复 Level id、无效 Material 与资源点统计。

### 测试与验证

- 已运行 `./gradlew.bat build`，构建通过。
