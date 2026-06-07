# 开发记录

## Step 018 - synthetic enum switch 运行时修复

### 本次完成

- 修复 `/br transition info level0_to_level1_stairwell` 触发的 `NoClassDefFoundError: org/monday/backrooms/transition/TransitionDefinition$1`。
- 将 `TransitionDefinition#triggerDescription()` 中的 enum switch 改为普通 `if` 判断。
- 将 `TransitionService#resolveLocation()` 中的 enum switch 改为普通 `if` 判断。
- 将 `RoomGenerationService#generate()` 中的 enum switch 改为普通 `if` 判断。
- 通过 `clean build` 清理旧 class 输出，确认新 jar 不再包含相关 `$1.class` synthetic helper class。

### 修改文件

- `plan.md`
- `step.md`
- `src/main/java/org/monday/backrooms/transition/TransitionDefinition.java`
- `src/main/java/org/monday/backrooms/transition/TransitionService.java`
- `src/main/java/org/monday/backrooms/room/RoomGenerationService.java`

### 设计原因

- 当前测试服在执行 Transition 详情命令时运行时找不到 enum switch 生成的 synthetic class，导致主线程异常。
- 这些 switch 只负责小型分支选择，改成 `if` 不改变业务行为，但能避免依赖额外 `$1.class`。
- 同时处理 Transition 目标解析和 Room 生成中的同类 enum switch，降低后续命令再遇到同类问题的风险。

### 下一步建议

- 部署新 jar 后完整重启测试服。
- 测试 `/br transition info level0_to_level1_stairwell`。
- 继续测试 `/br transition guide level0_to_level1_stairwell` 和 `/br room generate level0_basic_room level_0`，确认同类 synthetic class 错误不再出现。

### 测试与验证

- 已运行 `./gradlew.bat clean build`，构建通过。
- 已检查 `build/classes` 和 `build/libs/untitled-1.0-SNAPSHOT.jar`，不再包含 `TransitionDefinition$1.class`、`TransitionService$1.class`、`RoomGenerationService$1.class`。
- 构建仍提示 `TransitionService` 中传送 API 过时，当前不影响 Paper 1.21.4 编译运行，后续可集中迁移到现代传送 API。

## Step 017 - README 世界创建说明补充

### 本次完成

- 补充 README 的世界创建章节，明确当前插件不会自动创建世界。
- 写明 BackroomsCore 配置依赖的 world 名称：`lobby`、`level_0`、`level_1`。
- 补充 Multiverse-Core 首次创建命令：`/mv create <world> NORMAL -t FLAT`。
- 补充已有世界文件夹的导入命令：`/mv import <world> NORMAL`。
- 说明当前不需要指定自定义 `-g` generator；只有安装 VoidGen 等外部生成器并主动想用空世界时才需要指定。
- 补充创建后运行 `/br reload`、`/br debug config` 检查 missing worlds 的流程。

### 修改文件

- `README.md`
- `plan.md`
- `step.md`

### 设计原因

- 当前配置已经引用 `lobby`、`level_0`、`level_1`，但 README 没有告诉测试服如何创建这些世界，实机测试会卡在 missing world。
- MVP 阶段还没有自动迷宫 world generator，直接让 Multiverse 创建平地世界最稳定，方便先验证命令、切层、资源点和 Room 占位生成。
- 明确不需要 `-g` 可以避免管理员误填不存在的 generator 导致 Multiverse 创建失败。

### 下一步建议

- 测试服完整重启后，先确认 `/br` 启动报错已消失。
- 用 README 的 Multiverse 命令创建或导入 `lobby`、`level_0`、`level_1`。
- 执行 `/br reload` 和 `/br debug config`，确认 missing worlds 为 `none`。

### 测试与验证

- 本步是文档补充；仍会运行 `./gradlew.bat build` 验证项目状态。

## Step 016 - Paper 命令注册修复

### 本次完成

- 修复测试服启动时报错：Paper plugin 不支持 `paper-plugin.yml` 的 YAML command 声明，也不允许在启动期对该命令调用 `JavaPlugin#getCommand()`。
- 将 `/br` 注册迁移到 Paper `JavaPlugin#registerCommand`。
- 新增 Paper `BasicCommand` 适配层，复用现有 `BrCommand` 的执行逻辑和 tab completion，不重写命令分发。
- 从 `paper-plugin.yml` 移除 `commands:` 块，保留权限声明；`/br` 与 `backrooms` 别名由 Paper command API 注册。

### 修改文件

- `README.md`
- `plan.md`
- `step.md`
- `src/main/java/org/monday/backrooms/Backrooms.java`
- `src/main/java/org/monday/backrooms/command/BrCommand.java`
- `src/main/resources/paper-plugin.yml`

### 设计原因

- Paper plugin 模式下，命令不走 Bukkit YAML command 声明路径；继续保留 `getCommand("br")` 会导致插件启用失败。
- 只给 `BrCommand` 增加 `execute` 和 `complete` 委托方法，保留现有权限、消息、reload、debug、level、transition、room、loot、resource 命令行为。
- `paper-plugin.yml` 继续只负责插件元数据和权限，避免后续维护时误以为 `/br` 仍由 YAML 声明。

### 下一步建议

- 部署新 jar 后完整重启测试服，确认启动日志不再出现 `Paper plugins do not support YAML-based command declarations`。
- 启动后测试 `/br`、`/br help`、`/br debug config` 和 `/br` tab completion。
- 继续按 README 的顺序验证 CraftEngine、Loot/Resource、Transition、Room 和保护规则闭环。

### 测试与验证

- 已运行 `./gradlew.bat build`，构建通过。
- 构建仍提示 `TransitionService` 中传送 API 过时，当前不影响 Paper 1.21.4 编译运行，后续可集中迁移到现代传送 API。

## Step 020 - Faithful Level 0 CE 建图资产导入

### 本次完成

- 从 `D:\dev\backrooms\faithfulbackrooms` 筛选并导入适合 Level 0 建图的资源包资产。
- 导入到 CraftEngine `backrooms` 资源包：
  - 47 个 block model：墙纸、地毯、天花板、灯具、箱子、柜子、管道、标牌、门等。
  - 28 个 custom model：复杂装饰模型依赖，如 crate、vent、pipe、door top、wall light 等。
  - 47 个 item wrapper model：用于 CraftEngine block item 展示。
  - 51 张 block texture：导入模型实际引用的贴图。
- 新增 `faithful_level0_blocks.yml`，将导入资产配置为 `backrooms:faithful_*` CraftEngine 方块和可放置物品。
- 根据 CraftEngine 26.6 本地文档确认并采用：
  - item `behavior: block_item` 绑定对应 block。
  - 已有 pre-made model file 时使用 `state.model.path`，不让 CE 重新 generation。
  - 完整墙体/地面/天花板/crate 使用 `auto_state: note_block`，避免 `solid` 自动分配到 mushroom 系列状态。
  - 灯具、管道、踢脚线、牌子、CCTV、插座等非完整装饰使用 `auto_state: lower_tripwire` 并关闭 suffocation、view blocking、occlusion。
  - crate 系列临时使用 `simple_storage_block`，方便后续接入 loot container。
- 新增 `docs/level0-cell-guide.md`，系统记录 `16x16x6` cell、门洞、模板类型、marker 放置、真/假楼梯井区分和第一批模板制作建议。
- 新增 `docs/faithful-assets-ce.md`，记录 Faithful 资产导入位置、CE 配置原则、方块 id 和实机验证命令。
- README 与 plan 已补充 Faithful Level 0 建图资产状态和后续实机验证事项。

### 修改文件

- `README.md`
- `plan.md`
- `step.md`
- `docs/level0-cell-guide.md`
- `docs/faithful-assets-ce.md`
- `server-configs/CraftEngine/resources/backrooms/configuration/blocks/faithful_level0_blocks.yml`
- `server-configs/CraftEngine/resources/backrooms/resourcepack/assets/backrooms/models/block/faithful/*.json`
- `server-configs/CraftEngine/resources/backrooms/resourcepack/assets/backrooms/models/custom/faithful/*.json`
- `server-configs/CraftEngine/resources/backrooms/resourcepack/assets/backrooms/models/item/faithful/*.json`
- `server-configs/CraftEngine/resources/backrooms/resourcepack/assets/backrooms/textures/block/faithful/*.png`

### 设计原因

- Level 0 的阈限感需要大量相似但略有差异的墙纸、地毯、天花板、灯具和办公/维护装饰；只靠原版材质很难快速建出稳定风格。
- 先把 Faithful Backrooms 中“建图必需、低风险、可复用”的模型/纹理导入 CE，避免导入无关垃圾文件或完整外部包。
- 结构方块和装饰方块分开选择 `note_block` / `lower_tripwire`，减少装饰模型造成完整方块碰撞、窒息或遮挡的问题，并避免 `solid` 自动分配到 mushroom 系列状态。
- 当前仍不假设 CE 方块一定能稳定进入 schematic；`docs/level0-cell-guide.md` 继续保留 vanilla marker 方案，用于 FAWE/WorldEdit 兼容测试。

### 下一步建议

- 同步 `server-configs/CraftEngine/resources/backrooms/**` 到测试服 CraftEngine 资源目录。
- 在测试服执行 `/ce reload all`。
- 用以下命令抽样验证：
  - `/ce item get backrooms:faithful_yellow_wallpaper`
  - `/ce item get backrooms:faithful_old_carpet`
  - `/ce item get backrooms:faithful_ceiling_light`
  - `/ce item get backrooms:faithful_crate`
  - `/ce item get backrooms:faithful_exit_sign`
- 重点检查模型是否显示、碰撞是否合理、灯具是否发光、crate storage 是否可打开。
- 确认 CE 方块能否被 WorldEdit/FAWE 保存和粘贴；如果不稳定，继续使用 vanilla marker 扫描再替换 CE 方块。

### 测试与验证

- 已统计导入资产：textures=51，blockModels=47，customModels=28，itemModels=47。
- 后续仍需在测试服通过 CraftEngine `/ce reload all` 做实机验证。

## Step 021 - CraftEngine 纹理路径警告修复

### 本次完成

- 根据测试服 CraftEngine reload 日志修复缺失 vanilla 纹理路径警告。
- 将 `backrooms:fuse` 的纹理从不存在的 `minecraft:item/redstone_torch` 改为 `minecraft:block/redstone_torch`。
- 将 `backrooms:pipe_segment` 的纹理从不存在的 `minecraft:item/iron_bars` 改为 `minecraft:block/iron_bars`。
- 将 `backrooms:evacuation_hatch_marker` 的 item model 纹理从不存在的 `minecraft:item/iron_trapdoor` 改为 `minecraft:block/iron_trapdoor`。
- 同步修复测试服 CraftEngine 资源目录中的对应配置，便于直接执行 `/ce reload all` 验证。
- 已确认日志中提到的 Faithful item model 文件在项目资源包和测试服资源包中均存在，相关警告更可能来自 reload/pack 缓存或当次 reload 前文件状态。

### 修改文件

- `step.md`
- `server-configs/CraftEngine/resources/backrooms/configuration/items/materials.yml`
- `server-configs/CraftEngine/resources/backrooms/configuration/blocks/mvp_blocks.yml`
- `D:\dev\backrooms\devserver\plugins\CraftEngine\resources\backrooms\configuration\items\materials.yml`
- `D:\dev\backrooms\devserver\plugins\CraftEngine\resources\backrooms\configuration\blocks\mvp_blocks.yml`

### 设计原因

- `redstone_torch`、`iron_bars`、`iron_trapdoor` 在 vanilla 资源包里对应 block texture，不是 item texture；继续引用 `minecraft:item/...` 会导致 CraftEngine 打包时提示缺失纹理。
- 优先修正配置路径，不复制 vanilla 纹理，避免资源包膨胀和维护重复资源。

### 下一步建议

- 在测试服执行 `/ce reload all`。
- 如果仍提示 Faithful item model 缺失，先执行 `/ce clean-cache` 后再 `/ce reload all`。
- 如果缺失提示仍存在，检查 CraftEngine 当前实际加载的资源根目录是否为 `plugins/CraftEngine/resources/backrooms`，以及 `assets/backrooms/models/item/faithful/*.json` 是否被打包进生成资源包。

### 测试与验证

- 已运行 `./gradlew.bat build`，构建通过。
- 已确认警告中列出的 Faithful item model 文件在项目资源包和 devserver 资源包中存在。

## Step 022 - CraftEngine resourcepack 资产目录修复

### 本次完成

- 根据 `/ce reload all` 仍提示 `backrooms:faithful_*` item model 缺失的问题，确认模型文件虽然存在，但放置目录不符合 CraftEngine 资源包结构。
- 将项目内 Faithful 资源包文件从：
  - `server-configs/CraftEngine/resources/backrooms/assets/...`
  移动到：
  - `server-configs/CraftEngine/resources/backrooms/resourcepack/assets/...`
- 将测试服 CraftEngine 资源目录同步做同样移动：
  - `D:\dev\backrooms\devserver\plugins\CraftEngine\resources\backrooms\resourcepack\assets/...`
- 更新 `docs/faithful-assets-ce.md`，明确 CraftEngine 资源包文件必须位于 `resourcepack/assets/...`，不能直接放在资源根目录的 `assets/...`。
- 已确认旧 `assets` 目录不存在，新 `resourcepack/assets/backrooms/models/item/faithful/manilla_wallpaper.json` 在项目与测试服中均存在。

### 修改文件

- `docs/faithful-assets-ce.md`
- `server-configs/CraftEngine/resources/backrooms/resourcepack/assets/**`
- `D:\dev\backrooms\devserver\plugins\CraftEngine\resources\backrooms\resourcepack\assets/**`

### 设计原因

- CraftEngine 会从资源目录的 `resourcepack/assets/...` 收集资源包文件；之前直接放在 `assets/...` 下，文件本身存在，但不会被 CE 打包扫描到，因此 reload 时仍提示缺少 item model。
- 保持配置里的 `backrooms:item/faithful/...`、`backrooms:block/faithful/...` 路径不变，只修正物理目录结构，符合 Minecraft 资源包解析规则。

### 下一步建议

- 在测试服执行 `/ce clean-cache`。
- 再执行 `/ce reload all`。
- 如果仍有缺失，检查 CE 生成的最终资源包 zip 内是否包含 `assets/backrooms/models/item/faithful/*.json`。

### 测试与验证

- 已确认项目旧目录 `server-configs/CraftEngine/resources/backrooms/assets` 不存在。
- 已确认测试服旧目录 `plugins/CraftEngine/resources/backrooms/assets` 不存在。
- 已确认项目与测试服新目录下存在 `resourcepack/assets/backrooms/models/item/faithful/manilla_wallpaper.json`。

## Step 019 - FAWE schematic 有限区域 worldgen MVP

### 本次完成

- 新增 WorldEdit/FAWE compileOnly 依赖与 EngineHub Maven 仓库，使用 Java 21 可编译的 `worldedit-bukkit:7.3.10`。
- 新增 `worldgen.yml`，用于配置：
  - `16x16x6` cell 默认参数。
  - Level 0 schematic 模板文件路径。
  - footprint、connectors、tags、weight、rotations、unique、min-distance-from-spawn-cells、paste-air。
  - vanilla marker 材质与 `generated-regions.yml` 持久化文件。
- 新增 `org.monday.backrooms.worldgen` 模块：
  - `TemplateConnector`
  - `TemplateMarkerType`
  - `SchematicTemplateDefinition`
  - `WorldGenerationResult`
  - `WorldGenerationService`
  - `WorldEditSchematicPaster`
- 新增有限区域生成服务：先生成有限 N x N cell 图，再按连接口、权重、tag 与旋转选择 schematic 模板，最后通过 WorldEdit/FAWE API 粘贴到目标 Level world。
- 新增 marker 扫描 MVP：生成后扫描配置的 vanilla marker block 数量，并将 region 元数据写入 `generated-regions.yml`，避免同一 region/seed 重复生成。
- 新增管理/调试命令：
  - `/br worldgen templates`
  - `/br worldgen generate <level> <size> [seed]`
- 新增权限：
  - `backrooms.command.worldgen.templates`
  - `backrooms.command.worldgen.generate`
- 将 worldgen 模块接入 `/br reload`、启动/重载日志、`/br debug config`、help、tab completion、`messages.yml`、`paper-plugin.yml` 和 README。

### 修改文件

- `README.md`
- `build.gradle`
- `plan.md`
- `step.md`
- `src/main/java/org/monday/backrooms/Backrooms.java`
- `src/main/java/org/monday/backrooms/command/BrCommand.java`
- `src/main/java/org/monday/backrooms/config/ConfigFileService.java`
- `src/main/java/org/monday/backrooms/worldgen/TemplateConnector.java`
- `src/main/java/org/monday/backrooms/worldgen/TemplateMarkerType.java`
- `src/main/java/org/monday/backrooms/worldgen/SchematicTemplateDefinition.java`
- `src/main/java/org/monday/backrooms/worldgen/WorldGenerationResult.java`
- `src/main/java/org/monday/backrooms/worldgen/WorldGenerationService.java`
- `src/main/java/org/monday/backrooms/worldgen/WorldEditSchematicPaster.java`
- `src/main/resources/messages.yml`
- `src/main/resources/paper-plugin.yml`
- `src/main/resources/worldgen.yml`

### 设计原因

- Level 0 先采用“有限区域生成”，避免在 `PlayerMoveEvent` 或 chunk generator 中做重型粘贴，降低测试服卡顿和排错风险。
- `worldgen.yml` 只记录模板元数据，真实房间仍由建图/WorldEdit schematic 提供，这样可以让房间美术与生成逻辑分离。
- MVP 先扫描 vanilla marker block，而不是直接依赖 CraftEngine Java API；如果 CE 方块无法稳定进入 schematic，后续可通过 marker 替换方案接入 CE block id。
- 生成记录写入 `generated-regions.yml`，优先防止管理员重复执行命令覆盖同一区域。

### 下一步建议

- 安装/启用 WorldEdit 或 FastAsyncWorldEdit。
- 用 WorldEdit/FAWE 制作并保存第一批模板：
  - `plugins/BackroomsCore/templates/level_0/basic_01.schem`
  - `plugins/BackroomsCore/templates/level_0/straight_corridor_01.schem`
  - `plugins/BackroomsCore/templates/level_0/corner_corridor_01.schem`
  - `plugins/BackroomsCore/templates/level_0/stairwell_exit_01.schem`
- 重启测试服后执行 `/br reload`、`/br worldgen templates`、`/br worldgen generate level_0 9 seed123`。
- 继续实现 `/br room edit start/save`、粒子边框、schematic 保存和自动 connector/marker 扫描写配置。

### 测试与验证

- 已运行 `./gradlew.bat build`，构建通过。
- 构建仍提示 `TransitionService` 中传送 API 过时，当前不影响 Paper 1.21.4 编译运行，后续可集中迁移到现代传送 API。

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

## Step 006 - 测试服配置、CraftEngine 资产与随机出生点

### 本次完成

- 将开发流程推进到 pursue-goal 模式，并在 `plan.md` 中记录持续推进规则：按最高优先级任务实现、检查、更新文档、commit 并 push。
- 根据本地 CraftEngine 26.6 文档与默认资源示例，新增版本化 `server-configs/CraftEngine/resources/backrooms/`：
  - `pack.yml` 定义 `backrooms` namespace。
  - `materials.yml` 定义第一批测试物品：杏仁水、电池、废料、电线、发电机部件、MEG 芯片、钥匙卡、现场笔记。
  - `mvp_blocks.yml` 定义第一批测试方块/容器：松动地毯缓存、废料堆、物资箱、尸体缓存、楼梯井标记。
- CraftEngine 测试资产暂时复用 Minecraft 原版材质/model 生成方式，不引入自定义美术资源，方便后续替换。
- 将 CraftEngine `backrooms` 资源包同步到测试服：`D:\dev\backrooms\devserver\plugins\CraftEngine\resources\backrooms`。
- 新增版本化 TAB 配置 `server-configs/TAB/`，并同步到测试服：
  - 移除默认演示 header/footer、动画占位符和不相关 Placeholder。
  - 启用简洁的 Backrooms 测试服 header/footer 与 scoreboard。
  - 使用静态测试前缀，避免早期过度依赖 LuckPerms 前后缀。
- 为 Level 配置新增 `spawn.points`，支持多个随机出生点。
- 调整 `LevelSpawn` 与 `LevelConfigLoader`：
  - 兼容旧单点 `spawn.x/y/z/yaw/pitch`。
  - 如果配置了 `spawn.points`，会加载所有有效点并在传送时随机选择。
  - 启动/重载时输出随机出生点数量，便于实机确认配置是否生效。
- `/br level info <id>` 新增随机出生点数量显示。
- `/br level tp <id>` 增加控制台传送坐标日志，便于确认随机点选择。
- 新增 Gradle `deployDevServer` task，用于将构建出的插件 jar 复制到 `../devserver/plugins`。

### 修改文件

- `build.gradle`
- `plan.md`
- `server-configs/README.md`
- `server-configs/CraftEngine/resources/backrooms/pack.yml`
- `server-configs/CraftEngine/resources/backrooms/configuration/items/materials.yml`
- `server-configs/CraftEngine/resources/backrooms/configuration/blocks/mvp_blocks.yml`
- `server-configs/TAB/config.yml`
- `server-configs/TAB/groups.yml`
- `src/main/java/org/monday/backrooms/command/BrCommand.java`
- `src/main/java/org/monday/backrooms/level/LevelConfigLoader.java`
- `src/main/java/org/monday/backrooms/level/LevelSpawn.java`
- `src/main/resources/levels/level_0.yml`
- `src/main/resources/levels/level_1.yml`
- `src/main/resources/messages.yml`
- `step.md`
- 同步修改测试服外部配置：
  - `D:\dev\backrooms\devserver\plugins\CraftEngine\resources\backrooms/**`
  - `D:\dev\backrooms\devserver\plugins\TAB\config.yml`
  - `D:\dev\backrooms\devserver\plugins\TAB\groups.yml`

### 设计原因

- 先写 CraftEngine 配置而不接入 CE Java API，是为了验证 CE 资源加载、物品/方块定义和资源包发送流程；BackroomsCore 的 CE Adapter 需要等实机行为稳定后再写，避免臆造 API。
- 测试资产全部用原版材质/model，符合当前“后面再替换模型”的目标，也能降低资源包调试成本。
- `spawn.points` 是切层系统的前置能力：玩家从楼梯井/维护门进入 Level 时应分散到多个入口或安全点，而不是固定堆叠在一个坐标。
- 地图生成没有在本步骤实现，因为它依赖 Transition、marker 扫描、房间模板格式与 WorldEdit/FAWE schematic 验证；当前仍处于第一阶段 MVP 的基础设施闭环。
- 外部插件配置保存在 `server-configs/`，是为了让 devserver 的配置可追踪、可回滚、可复用，而不是只改本地测试服文件。

### 下一步建议

- 在服务器控制台/游戏内执行：
  - `/ce reload all`
  - `/tab reload`
  - `/br reload`
- 若 jar 已被服务端锁定或代码未生效，重启测试服后再验证。
- 用 CraftEngine 命令测试物品/方块，例如 `/ce item give <player> backrooms:almond_water 1`、`/ce item give <player> backrooms:supply_crate 1`。
- 用 `/br level tp level_0` 与 `/br level tp level_1` 多次测试随机出生点。
- 下一步最高优先级：实现 Transition/撤离点配置与交互系统，让 Level 0 可以进入 Level 1，Level 1 可以返回 lobby。

### 测试与验证

- 已运行 `./gradlew.bat build`，构建通过。
- 已运行 `./gradlew.bat deployDevServer`，插件 jar 已复制到测试服 `plugins` 目录。
- 已检查 TAB/项目配置中未残留 `%animation:`、`essentials_`、`vault_prefix`、`rel_factions` 等早期测试占位符。
- 已读取 IDE lints，当前新增 Java 文件未报告诊断问题。

## Step 007 - Transition 与撤离点 MVP

### 本次完成

- 新增 `transitions.yml`，使用 `transitions.definitions` 配置切层/撤离点。
- 新增 Transition 核心模型与服务：
  - `TransitionDefinition`
  - `TransitionTarget`
  - `TransitionService`
  - `TransitionListener`
  - `CuboidRegion`
  - `BlockPosition`
  - `TransitionTriggerType`
  - `TransitionTargetType`
  - `TransitionSpawnMode`
- 支持两种触发方式：
  - 玩家进入配置区域。
  - 玩家右键配置方块或坐标。
- 支持两种目标：
  - 目标 Backrooms Level，默认复用目标 Level 的随机 `spawn.points`。
  - 目标 Bukkit world，例如返回 `lobby` 世界出生点。
- 支持 `point` 精确坐标目标、冷却、传送后短暂无触发保护、音效和 MiniMessage 消息反馈。
- 将 Transition 接入 `/br reload`，加载顺序为：配置文件 -> 消息 -> Level -> 资源方块 -> Transition。
- 新增 Transition 管理/调试命令：
  - `/br transitions`
  - `/br transition info <id>`
  - `/br transition trigger <id> [player]`
- 新增 Transition 权限：
  - `backrooms.transition.use`
  - `backrooms.transition.bypass.cooldown`
  - `backrooms.command.transitions`
  - `backrooms.command.transition.info`
  - `backrooms.command.transition.trigger`
- 默认配置了两个 MVP 通道：
  - `level0_to_level1_stairwell`：Level 0 区域进入 -> Level 1 随机出生点。
  - `level1_to_lobby_evacuation`：Level 1 区域进入 -> lobby 世界出生点。

### 修改文件

- `plan.md`
- `step.md`
- `src/main/java/org/monday/backrooms/Backrooms.java`
- `src/main/java/org/monday/backrooms/command/BrCommand.java`
- `src/main/java/org/monday/backrooms/config/ConfigFileService.java`
- `src/main/java/org/monday/backrooms/transition/BlockPosition.java`
- `src/main/java/org/monday/backrooms/transition/CuboidRegion.java`
- `src/main/java/org/monday/backrooms/transition/TransitionDefinition.java`
- `src/main/java/org/monday/backrooms/transition/TransitionListener.java`
- `src/main/java/org/monday/backrooms/transition/TransitionService.java`
- `src/main/java/org/monday/backrooms/transition/TransitionSpawnMode.java`
- `src/main/java/org/monday/backrooms/transition/TransitionTarget.java`
- `src/main/java/org/monday/backrooms/transition/TransitionTargetType.java`
- `src/main/java/org/monday/backrooms/transition/TransitionTriggerType.java`
- `src/main/resources/messages.yml`
- `src/main/resources/paper-plugin.yml`
- `src/main/resources/transitions.yml`

### 设计原因

- 切层是跨 Level 的关系，因此先放在独立 `transitions.yml`，避免污染每个 `levels/*.yml`；后续如果数量变多，再迁移到 `transitions/*.yml`。
- Region 触发先满足 Level 0 -> Level 1、Level 1 -> lobby 的核心闭环，右键方块触发则为 CE 楼梯井标记、维护门、终端等交互预留入口。
- 传送目标复用现有 `LevelSpawn`，这样 Transition 天然支持随机出生点，不需要重复实现一套随机逻辑。
- Transition 不自动创建或加载世界，只在目标世界已加载时传送，继续把世界管理职责留给 Paper/Multiverse。
- `PlayerMoveEvent` 只在方块坐标变化时检查，并按来源 Level 索引 Transition，降低每 tick 移动事件的性能压力。
- 传送后加入短暂无触发保护，避免入口/出口区域重叠时出现循环传送。

### 下一步建议

- 重启测试服加载新 jar 后执行 `/br reload`，再用 `/br transitions` 和 `/br transition info level0_to_level1_stairwell` 检查配置是否加载。
- 如果测试服已有 `lobby`、`level_0`、`level_1` 世界，可用 `/br transition trigger level0_to_level1_stairwell <player>` 和 `/br transition trigger level1_to_lobby_evacuation <player>` 测试传送闭环。
- 根据实际地图把 `transitions.yml` 中两个 region 坐标改到楼梯井/撤离点位置。
- 下一步将 Transition 与 CE 楼梯井标记结合，做可视化入口/撤离点；之后开始房间模板和生成原型。

### 测试与验证

- 已运行 `./gradlew.bat build`，构建通过。
- 构建提示 `TransitionService` 使用了过时 API，来源是 `Player#teleport(Location, TeleportCause)`；当前 Paper 1.21.4 可编译运行，后续可再迁移到 Paper 推荐的异步/现代传送 API。

## Step 008 - Transition 触发指引与 CE 标记摆放辅助

### 本次完成

- 新增 `/br transition guide <id>` 管理命令，用于临时显示 Transition 的触发区域或触发方块位置。
- 在 `TransitionService` 中新增 `showGuide`：
  - 对 region trigger 在区域中心生成 `PORTAL` 粒子。
  - 对 region 八个角生成 `END_ROD` 粒子。
  - 对 block trigger 的配置坐标生成 `END_ROD` 粒子。
- 为 guide 命令新增独立权限 `backrooms.command.transition.guide`，默认 `op`。
- 重构 `/br transition` 分支，使 `info`、`trigger`、`guide` 的缺参反馈更明确，避免 `guide` 被旧的宽泛 `info` 分支吞掉。
- 补充 tab completion：
  - 顶层 `/br transition` 会在拥有 info / trigger / guide 任一权限时出现。
  - `/br transition <tab>` 会按权限补全 `info`、`trigger`、`guide`。
  - `/br transition guide <tab>` 会补全 Transition id。
- 补充 `messages.yml` 中 guide 相关文案，避免成功显示后回退成原始 key。
- 更新 `paper-plugin.yml` 的 `/br` usage，让 usage 覆盖 `transitions` 与 `transition` 子命令。

### 修改文件

- `plan.md`
- `step.md`
- `src/main/java/org/monday/backrooms/command/BrCommand.java`
- `src/main/java/org/monday/backrooms/transition/TransitionService.java`
- `src/main/resources/messages.yml`
- `src/main/resources/paper-plugin.yml`

### 设计原因

- 当前最小路线不直接从 Java 调用 CraftEngine API 放置 `backrooms:stairwell_marker`，避免 CE API 版本耦合和区块/重载边界问题。
- guide 命令只负责把 Transition 触发范围可视化，管理员再用 CraftEngine `/ce item get` 或 `/ce debug setblock` 手动摆放楼梯井标记，职责更清晰、风险更低。
- guide 权限单独拆出，是因为它会在世界内生成粒子提示，影响比纯文本 `info` 更大，不应复用普通玩家可用的 transition use 权限。
- 使用粒子而不是真实方块，是为了避免 debug 辅助命令污染地图，也便于反复调整 `transitions.yml` 坐标。

### 下一步建议

- 重启测试服加载新 jar 后执行 `/br reload`。
- 使用 `/br transition guide level0_to_level1_stairwell` 和 `/br transition guide level1_to_lobby_evacuation` 显示触发区域。
- 在粒子显示的位置使用 CraftEngine 命令或物品摆放 `backrooms:stairwell_marker`，再用 `/ce debug target-block` 确认 CE 方块状态。
- 下一步开始房间/地图生成原型：先确认 WorldEdit/FAWE schematic 依赖与 marker 扫描方案，再做最小房间模板粘贴或占位生成。

### 测试与验证

- 已运行 `./gradlew.bat build`，构建通过。
- 构建仍提示 `TransitionService` 中传送 API 过时，当前不影响 Paper 1.21.4 编译运行，后续可集中迁移到现代传送 API。

## Step 015 - README 测试入口与 TODO 整理

### 本次完成

- 新增 `README.md`，作为 `plan.md` 的简化执行入口。
- README 中整理了：
  - 当前项目定位。
  - 已完成 TODO。
  - 未完成 TODO。
  - 构建与部署命令。
  - CraftEngine 测试流程。
  - Loot / Resource 测试流程。
  - Transition 测试流程。
  - Room / 地图占位生成测试流程。
  - 保护规则测试清单。
  - 常用排查命令。
- README 明确当前地图生成仍是 `/br room generate` 的占位房间/走廊生成器，不是最终自动迷宫系统。
- README 记录了默认 Transition region、默认 Resource 测试坐标和 `rooms.defaults.replace-air-only: true` 的测试注意事项。

### 修改文件

- `README.md`
- `plan.md`
- `step.md`

### 设计原因

- `plan.md` 已经适合作为长期设计文档，但不适合每次实机测试时快速阅读。
- README 作为仓库入口，需要直接告诉维护者当前完成了什么、还缺什么、如何构建、如何测试、如何生成测试地图。
- 地图生成能力目前仍是安全的占位生成器，README 中明确边界，避免误以为已经有完整自动世界生成。

### 下一步建议

- 重启测试服后按 README 的顺序执行 `/ce reload all`、`/br reload`、`/br debug config`。
- 在 `level_0` / `level_1` 分别测试 Room 生成、Transition guide、Resource locations 和保护规则。
- 根据真实地图位置更新 `resources.yml` 与 `transitions.yml` 坐标。

### 测试与验证

- 已运行 `./gradlew.bat build`，构建通过。
- README 为文档入口，尚需由测试服实机流程验证其中命令是否符合当前部署状态。

## Step 014 - CraftEngine Backrooms 资产扩展

### 本次完成

- 根据本地 CraftEngine wiki 与测试服默认资源示例，继续扩展 `backrooms` CraftEngine 资源包配置。
- 在 `materials.yml` 中新增第二批 Backrooms 材料物品：
  - `backrooms:fuse`
  - `backrooms:cloth_scrap`
  - `backrooms:pipe_segment`
  - `backrooms:flashlight_frame`
  - `backrooms:toolbox`
- 在 `mvp_blocks.yml` 中新增地图制作和后续系统预留用的 block item + block：
  - `backrooms:maintenance_door_marker`
  - `backrooms:evacuation_hatch_marker`
  - `backrooms:base_claim_terminal`
  - `backrooms:generator_core`
  - `backrooms:flickering_fluorescent_light`
- 新增模型继续使用 CraftEngine 的配置生成能力复用 Minecraft 原版材质，不引入自定义美术资源文件。
- 已将项目内 `server-configs/CraftEngine/resources/backrooms/**` 同步到测试服 `D:\dev\backrooms\devserver\plugins\CraftEngine\resources\backrooms/**`。

### 修改文件

- `plan.md`
- `step.md`
- `server-configs/CraftEngine/resources/backrooms/configuration/items/materials.yml`
- `server-configs/CraftEngine/resources/backrooms/configuration/blocks/mvp_blocks.yml`
- 同步修改测试服外部配置：
  - `D:\dev\backrooms\devserver\plugins\CraftEngine\resources\backrooms\configuration\items\materials.yml`
  - `D:\dev\backrooms\devserver\plugins\CraftEngine\resources\backrooms\configuration\blocks\mvp_blocks.yml`

### 设计原因

- 当前 BackroomsCore 仍不直接调用 CraftEngine Java API，CE 配置先作为地图制作、资源点、切层标记和后续基地设施的资产层。
- 维护门、撤离口、基地终端、发电机核心和荧光灯是后续 Transition、Room marker、基地 claim 和电力系统会反复用到的实体地图对象，先用 CE 方块配置落地能支持实机摆放验证。
- 第二批材料物品服务于 Level 0/1 探索、资源点掉落和基地升级成本，为后续把 Bukkit Material loot 迁移到 CE item 预留内容。
- 继续复用原版材质是为了先验证 CE reload、物品生成、方块摆放、掉落和资源包显示链路，后续再替换正式美术资源。

### 下一步建议

- 在测试服执行 `/ce reload all`。
- 使用 `/ce item get backrooms:fuse`、`/ce item get backrooms:maintenance_door_marker`、`/ce item get backrooms:flickering_fluorescent_light` 等命令验证新增资产。
- 在 Level 0/1 地图中摆放维护门、撤离口和荧光灯 marker，再结合 `/br transition guide <id>` 调整 `transitions.yml` 坐标。
- 后续需要把 BackroomsCore 的 loot/resource 配置逐步接入 CE item id，而不是长期使用 Bukkit Material 占位。

### 测试与验证

- 已运行 `./gradlew.bat build`，构建通过。
- 已对比项目内 `server-configs` 与测试服 CraftEngine `backrooms` 资源包配置，两边内容一致。
- 尚未执行 `/ce reload all`，需要在测试服运行时验证 CE 是否接受这些新增配置。

## Step 013 - Resource 方块调试命令

### 本次完成

- 为 `ResourceBlockService` 新增只读 `all()` 方法，供命令层查看当前运行时已加载的 Resource 方块定义。
- 新增 Resource 方块管理/调试命令：
  - `/br resources`
  - `/br resource list`
  - `/br resource info <id>`
- Resource 列表会显示 id、适用 Level、Material、Trigger、Loot Table、内联 drops 数量和显式 location 数量。
- Resource 详情会显示 Level、Material、locations 数量、Trigger、Loot Table、drops、是否移除方块、替换方块和冷却时间。
- 新增 Resource 命令权限：
  - `backrooms.command.resource.list`
  - `backrooms.command.resource.info`
- 补充 `/br help`、tab completion、`messages.yml` 和 `paper-plugin.yml`，方便在测试服定位资源点坐标和 loot table 引用问题。

### 修改文件

- `plan.md`
- `step.md`
- `src/main/java/org/monday/backrooms/command/BrCommand.java`
- `src/main/java/org/monday/backrooms/resource/ResourceBlockService.java`
- `src/main/resources/messages.yml`
- `src/main/resources/paper-plugin.yml`

### 设计原因

- Step 012 已把资源点与命名 Loot Table 连接起来，下一步最需要的是实机排查工具，确认当前加载的资源点是否真的引用了正确 Level、Material、坐标和 loot table。
- 命令只读运行时状态，不修改配置、不生成物品、不改变世界，适合作为测试服验证资源点配置的安全入口。
- `/br resources` 与现有 `/br rooms`、`/br transitions`、`/br loot list` 保持同类命令风格，降低后续管理命令扩展成本。

### 下一步建议

- 重启测试服加载新 jar 后执行 `/br reload`。
- 使用 `/br resources`、`/br resource info level0_loose_carpet`、`/br resource info level1_scrap_ore` 检查资源点配置是否符合预期。
- 将 `resources.yml` 中默认 `locations` 替换为真实地图资源点坐标，再验证右键/破坏触发与 Loot Table 掉落。
- 后续可继续做原版容器 loot source 或 marker 扫描，把 loot table 从资源点扩展到房间/尸体/物资箱。

### 测试与验证

- 已运行 `./gradlew.bat build`，构建通过。
- 构建仍提示 `TransitionService` 中传送 API 过时，当前不影响 Paper 1.21.4 编译运行，后续可集中迁移到现代传送 API。

## Step 012 - Loot Table MVP 与资源点掉落池

### 本次完成

- 新增 `loot.yml`，使用 `loot-tables.definitions` 配置命名 Loot Table。
- 新增 Loot Table 基础模型与服务：
  - `LootEntry`
  - `LootTableDefinition`
  - `LootTableService`
- `LootTableService` 支持：
  - `/br reload` 热重载。
  - 重复 id 检查。
  - 无效 Material 跳过并输出 warning。
  - `rolls.min/max` 随机 roll 次数。
  - entry `chance/min/max` 随机产出 Bukkit `ItemStack`。
- `ConfigFileService` 新增 `loot.yml` 默认释放、加载和 legacy `loot-tables` section 提醒。
- `Backrooms` 主类新增 `LootTableService` 生命周期，启动日志、重载日志和 `/br reload` 文案都显示 Loot Table 数量。
- Resource 方块定义新增可选 `loot-tables` 字段：
  - 资源点触发时会 roll 引用的命名 Loot Table。
  - 原有内联 `drops` 保持兼容，并与 `loot-tables` 叠加。
  - 配置引用不存在的 Loot Table 时会在控制台 warning。
- 新增 Loot Table 管理/测试命令：
  - `/br loot list`
  - `/br loot info <id>`
  - `/br loot roll <id> [player]`
- `/br loot roll` 会把物品加入目标玩家背包，背包满时把剩余物品自然掉落在玩家位置，避免静默吞物品。
- 新增 Loot 命令权限：
  - `backrooms.command.loot.list`
  - `backrooms.command.loot.info`
  - `backrooms.command.loot.roll`
- `/br debug config` 新增 Loot Table 数量显示。

### 修改文件

- `plan.md`
- `step.md`
- `src/main/java/org/monday/backrooms/Backrooms.java`
- `src/main/java/org/monday/backrooms/command/BrCommand.java`
- `src/main/java/org/monday/backrooms/config/ConfigFileService.java`
- `src/main/java/org/monday/backrooms/loot/LootEntry.java`
- `src/main/java/org/monday/backrooms/loot/LootTableDefinition.java`
- `src/main/java/org/monday/backrooms/loot/LootTableService.java`
- `src/main/java/org/monday/backrooms/resource/ResourceBlockDefinition.java`
- `src/main/java/org/monday/backrooms/resource/ResourceBlockService.java`
- `src/main/resources/loot.yml`
- `src/main/resources/messages.yml`
- `src/main/resources/paper-plugin.yml`
- `src/main/resources/resources.yml`

### 设计原因

- 战利品是后室探索循环的核心，但第一版只做 Bukkit `Material` + `ItemStack`，避免过早耦合 CraftEngine Java API、NBT 或数据库。
- 命名 Loot Table 能让资源方块、未来容器、尸体和事件奖励复用同一套掉落池，而不是在每个 source 内重复写掉落列表。
- 现有 Resource `drops` 继续保留，减少配置迁移风险；`loot-tables` 作为可选叠加字段先进入实机验证。
- `/br loot roll` 作为管理员测试入口，能在不破坏地图资源点的情况下验证战利品表概率、物品和背包溢出行为。

### 下一步建议

- 重启测试服加载新 jar 后执行 `/br reload`，确认聊天和控制台都显示 Loot Table 数量。
- 执行 `/br loot list`、`/br loot info level0_basic_supplies`、`/br loot roll level0_basic_supplies <player>` 验证命令和权限。
- 在真实 Level 0/1 资源点坐标更新后，验证 `resources.yml` 中 `loot-tables` 会随资源触发掉落。
- 后续再把 Loot Table 接入原版容器、CE 容器方块、尸体方块和事件奖励，不在当前步骤引入额外监听器或外部 API。

### 测试与验证

- 已运行 `./gradlew.bat build`，构建通过。
- 构建仍提示 `TransitionService` 中传送 API 过时，当前不影响 Paper 1.21.4 编译运行，后续可集中迁移到现代传送 API。

## Step 011 - 运行时安全审查与保护加固

### 本次完成

- 对当前 Paper 1.21.4 插件实现做了一轮最小安全审查修复，优先处理会导致测试服地图被误破坏或配置重载 fail-open 的问题。
- `/br reload` 改为根据 `reloadRuntimeConfig()` 的真实结果反馈成功或失败；如果重载被拒绝或抛出运行时异常，会发送 `reload-failed` 文案而不是误报成功。
- `reloadRuntimeConfig()` 在已有 Level registry 的情况下，如果新配置没有加载到任何 Level，会保留旧 registry 并中止本次切换，避免保护规则因为 Level 为空而直接失效。
- Level 规则监听器增加 world fallback：当玩家追踪状态缺失或过期时，仍会按玩家所在 world 识别已启用 Level。
- 补充桶、火焰、燃烧、爆炸、实体改方块、展示实体破坏/放置等常见绕过路径的保护事件。
- 爆炸事件现在只清空 `blockList()` 阻止地形破坏，不直接取消整个爆炸事件，降低对其他插件或实体效果的干扰。
- Resource 方块定义新增 `locations` 坐标限制，可把资源点限定到显式坐标，避免同材质普通地形全部变成资源点。
- Room 生成默认改为 `replace-air-only: true`，并增加世界高度边界检查、0 改动失败提示和光源覆盖放置，降低管理员误覆盖地图或误判生成成功的风险。
- `/br debug config` 增加 Transition `feedback.message-key` 缺失检查，便于实机发现消息配置问题。

### 修改文件

- `plan.md`
- `step.md`
- `src/main/java/org/monday/backrooms/Backrooms.java`
- `src/main/java/org/monday/backrooms/command/BrCommand.java`
- `src/main/java/org/monday/backrooms/message/MessageService.java`
- `src/main/java/org/monday/backrooms/resource/ResourceBlockDefinition.java`
- `src/main/java/org/monday/backrooms/resource/ResourceBlockPosition.java`
- `src/main/java/org/monday/backrooms/resource/ResourceBlockService.java`
- `src/main/java/org/monday/backrooms/room/RoomGenerationService.java`
- `src/main/java/org/monday/backrooms/rule/LevelRuleListener.java`
- `src/main/resources/messages.yml`
- `src/main/resources/resources.yml`
- `src/main/resources/rooms.yml`

### 设计原因

- 当前 MVP 即将进入测试服实机验证，最重要的是先避免明显的地图破坏绕过和配置错误导致保护失效。
- 不在本 step 做大规模事务化重构，先用最小改动保证 Level 识别与保护规则在常见异常状态下尽量 fail-closed。
- Resource `locations` 保留空列表时按材质匹配的旧行为，避免破坏后续想做整类资源矿脉的配置方式；默认示例则改为坐标限定，减少新手误用风险。
- Room 生成默认只替换空气，更适合作为测试服管理员命令；之后引入 WorldEdit/FAWE schematic 或 marker 扫描时再做更细的覆盖策略。

### 下一步建议

- 重启测试服加载新 jar 后先执行 `/br reload` 和 `/br debug config`，确认失败反馈、消息 key 检查和配置摘要正常。
- 在 `level_0` / `level_1` 世界验证普通玩家无法通过放桶、点火、爆炸、实体方块变化、展示实体操作绕过保护。
- 把 `resources.yml` 中示例 `locations` 改成真实地图资源点坐标，再验证右键/破坏触发资源掉落。
- 在空旷区域测试 `/br room generate level0_basic_room level_0`，再在非空气区域测试无改动提示。
- 后续集中处理更完整的 reload 事务化、异步传送 API 迁移和实机验证发现的问题。

### 测试与验证

- 已运行 `./gradlew.bat build`，构建通过。
- 已运行 `./gradlew.bat deployDevServer`，jar 已部署到本地测试服插件目录。
- 构建仍提示 `TransitionService` 中传送 API 过时，当前不影响 Paper 1.21.4 编译运行，后续可集中迁移到现代传送 API。

## Step 010 - 运行时配置摘要调试命令

### 本次完成

- 新增 `/br debug config` 管理命令，用于输出当前 Backrooms 运行时配置摘要。
- 新增权限 `backrooms.command.debug.config`，默认 `op`，并加入 `backrooms.admin`。
- `/br debug config` 会汇总：
  - Level 总数、启用数、禁用数。
  - 已配置但当前未加载的 Level world。
  - 资源方块定义数量。
  - Transition 总数、禁用数和引用问题。
  - Room 模板总数、禁用数和引用问题。
- Transition 引用检查覆盖：来源 Level 不存在、trigger world 未加载、目标 Level 不存在/禁用、目标 world 未加载。
- Room 引用检查覆盖：模板引用了不存在的 Level。
- 补充 `/br help`、tab completion、`messages.yml` 和 `paper-plugin.yml`。

### 修改文件

- `plan.md`
- `step.md`
- `src/main/java/org/monday/backrooms/command/BrCommand.java`
- `src/main/resources/messages.yml`
- `src/main/resources/paper-plugin.yml`

### 设计原因

- 实机验证阶段最常见的问题不是代码逻辑，而是世界未加载、Level id 拼错、Transition 指向错误或 Room 模板引用错误。
- `/br debug config` 提供一个快速摘要，避免每次都翻控制台启动日志或逐条执行 `/br level info`、`/br transition info`、`/br room info`。
- 命令只读运行时状态，不修改配置或世界，因此适合作为测试服排错入口。

### 下一步建议

- 重启测试服加载新 jar 后执行：
  - `/br reload`
  - `/br debug config`
- 如果出现 missing world 或 reference issue，优先检查 Multiverse 世界加载状态和 `levels/*.yml`、`transitions.yml`、`rooms.yml` 中的 id。
- 确认摘要无明显问题后，再测试 `/br transition guide <id>` 和 `/br room generate <id> [level]`。

### 测试与验证

- 已运行 `./gradlew.bat build`，构建通过。

## Step 009 - Room 模板与占位生成原型

### 本次完成

- 新增 `rooms.yml`，配置最小 room/corridor 模板：
  - `level0_basic_room`
  - `level0_corridor`
  - `level1_utility_room`
- 新增 Room 生成基础模型：
  - `RoomShape`
  - `RoomDefinition`
  - `RoomGenerationResult`
  - `RoomGenerationService`
- `ConfigFileService` 新增 `rooms.yml` 默认保存、加载、热重载和 legacy section 提醒。
- `Backrooms` 主类新增 `RoomGenerationService` 生命周期，并在启动与 `/br reload` 日志中输出 Room 模板数量。
- 新增 Room 管理/调试命令：
  - `/br rooms`
  - `/br room list`
  - `/br room info <id>`
  - `/br room generate <id> [level]`
- 新增 Room 命令权限：
  - `backrooms.command.room.list`
  - `backrooms.command.room.info`
  - `backrooms.command.room.generate`
- 补充 `messages.yml` 中 Room 列表、详情、生成成功/失败、usage 和 `/br reload` 计数文案。

### 修改文件

- `plan.md`
- `step.md`
- `src/main/java/org/monday/backrooms/Backrooms.java`
- `src/main/java/org/monday/backrooms/command/BrCommand.java`
- `src/main/java/org/monday/backrooms/config/ConfigFileService.java`
- `src/main/java/org/monday/backrooms/room/RoomDefinition.java`
- `src/main/java/org/monday/backrooms/room/RoomGenerationResult.java`
- `src/main/java/org/monday/backrooms/room/RoomGenerationService.java`
- `src/main/java/org/monday/backrooms/room/RoomShape.java`
- `src/main/resources/messages.yml`
- `src/main/resources/paper-plugin.yml`
- `src/main/resources/rooms.yml`

### 设计原因

- 先用 Bukkit 原生 `Block#setType` 做最小占位生成，不直接引入 WorldEdit/FAWE API，降低依赖和实机排错成本。
- `rooms.yml` 先采用单文件 `rooms.definitions`，方便 MVP 阶段热重载和快速调参；后续模板数量变多后可迁移到 `rooms/*.yml` 或 level 分目录。
- `/br room generate` 要求玩家位于目标 Level 对应世界，避免误把 Level 0 / Level 1 模板生成到 lobby 或其他世界。
- Room palette 暂时使用原版材质，既能和当前 resource block 材质触发闭环联动，也便于之后替换为 CraftEngine marker / schematic 方案。
- 生成器提供 `max-blocks-per-generate` 和 `replace-air-only` 默认项，为后续测试服防误操作保留基础安全阀。

### 下一步建议

- 重启测试服加载新 jar，因为本次包含 Java 代码变更。
- 重启后执行 `/br reload`，确认控制台和聊天显示 Room 数量。
- 在 `level_0` 世界测试：
  - `/br rooms`
  - `/br room info level0_basic_room`
  - `/br room generate level0_basic_room level_0`
  - `/br room generate level0_corridor level_0`
- 在 `level_1` 世界测试 `/br room generate level1_utility_room level_1`。
- 实机验证后再决定是否引入 WorldEdit/FAWE schematic 粘贴、marker 扫描和 CraftEngine 方块替换流程。

### 测试与验证

- 已运行 `./gradlew.bat build`，构建通过。
- 构建仍提示 `TransitionService` 中传送 API 过时，当前不影响 Paper 1.21.4 编译运行，后续可集中迁移到现代传送 API。
## Step 023 - CraftEngine 完整方块状态池修正

### 本次完成

- 根据实机反馈修正 CraftEngine 透明方块/完整方块的状态池选择。
- 将 `server-configs/CraftEngine/resources/backrooms/configuration/blocks/mvp_blocks.yml` 中完整方块的 `auto_state: solid` 改为 `auto_state: note_block`。
- 将 `server-configs/CraftEngine/resources/backrooms/configuration/blocks/faithful_level0_blocks.yml` 中 Faithful Level 0 完整方块的 `auto_state: solid` 改为 `auto_state: note_block`。
- 保留灯具、管道、踢脚线、牌子、CCTV、插座等非完整装饰的 `auto_state: lower_tripwire`。
- 同步测试服 CraftEngine 配置到 `D:\dev\backrooms\devserver\plugins\CraftEngine\resources\backrooms\configuration\blocks\`。
- 更新 `docs/faithful-assets-ce.md` 与 `docs/level0-cell-guide.md`，明确不要用 `solid`，因为它可能自动分配到 mushroom 系列状态，导致透明/遮挡表现不符合当前资源包。

### 修改文件

- `plan.md`
- `step.md`
- `docs/faithful-assets-ce.md`
- `docs/level0-cell-guide.md`
- `server-configs/CraftEngine/resources/backrooms/configuration/blocks/mvp_blocks.yml`
- `server-configs/CraftEngine/resources/backrooms/configuration/blocks/faithful_level0_blocks.yml`

### 设计原因

- CraftEngine 文档中 `solid` 是“任意 solid block”，包含 note block 和 mushroom block 等状态池；实机显示 mushroom 系列不适合当前透明/遮挡表现。
- `note_block` 更稳定，适合当前墙体、地毯、天花板、crate 等完整方块先作为 MVP 状态池。
- 非完整装饰仍用 `lower_tripwire`，避免占满完整方块碰撞并减少窒息、遮挡和 occlusion 问题。

### 下一步建议

- 在测试服执行 `/ce reload all`。
- 使用 `/ce item get backrooms:faithful_yellow_wallpaper`、`/ce item get backrooms:faithful_old_carpet`、`/ce item get backrooms:faithful_crate` 放置验证。
- 重点看是否还出现 mushroom 相关透明、遮挡、碰撞异常。
- 如果 CE 提示 note_block 可用状态不足，再按方块类别分配到更细的稳定状态池。

### 测试与验证

- 本次为 CraftEngine YAML 与文档变更，未修改 Java 代码。
- 已同步配置到测试服 CraftEngine 目录，等待 `/ce reload all` 实机验证。
