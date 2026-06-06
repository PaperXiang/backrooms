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
