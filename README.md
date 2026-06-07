# BackroomsCore

BackroomsCore 是面向 Paper 1.21.4 的后室主题服务器核心插件。当前目标是先做可运行、可热重载、可实机验证的 MVP 闭环：Level 0、Level 1、基础切层、资源点、战利品表、房间占位生成和 CraftEngine 测试资产。

## 当前状态

### 已完成

- [x] Paper 1.21.4 插件基础框架。
- [x] `/br` 主命令、help、tab completion 和权限声明。
- [x] `/br` 已改为 Paper `JavaPlugin#registerCommand` 注册，避免 Paper plugin 启动期调用 `JavaPlugin#getCommand()`。
- [x] 配置拆分：`messages.yml`、`settings/config.yml`、`levels/*.yml`、`resources.yml`、`transitions.yml`、`rooms.yml`、`loot.yml`、`corpses.yml`、`bases.yml`、`worldgen.yml`。
- [x] Level registry：支持配置化 Level id、world、随机 spawn、规则、标题。
- [x] `/br level tp <id>` 和 `/br level info <id>`。
- [x] 玩家当前 Level 运行时追踪。
- [x] Level 规则保护：禁止普通破坏/放置、禁止 PVP、拦截桶、火、爆炸、实体改方块等常见绕过。
- [x] `/br reload` 热重载，带基础失败保留旧 Level registry 的安全保护。
- [x] Resource 方块 MVP：Material + 可选坐标 `locations` + cooldown + drops + loot table。
- [x] `/br resources`、`/br resource info <id>` 调试命令。
- [x] Loot Table MVP：命名掉落池、rolls、chance、手动抽取测试。
- [x] `/br loot list`、`/br loot info <id>`、`/br loot roll <id> [player]`。
- [x] Loot Source MVP：原版 `CHEST` / `BARREL` 容器打开时可按配置注入 Loot Table，并用 TileState PDC 标记 one-time 生成状态；`event_reward` / `command_reward` 可直接对玩家发放奖励。
- [x] 尸体缓存 MVP：玩家在 Backrooms Level 死亡时，普通掉落进入原版容器尸体缓存，配置的保险物品堆会在重生后返还。
- [x] 基地 claim MVP：`bases.yml` 配置 Level 1 可 claim 区域，`/br base claim <id>` 持久化占领，owner 可在已 claim 区域内建造/破坏。
- [x] Transition MVP：region 触发、右键方块触发预留、Level/world 目标、冷却、传送后免触发保护。
- [x] `/br transitions`、`/br transition info <id>`、`/br transition trigger <id> [player]`、`/br transition guide <id>`。
- [x] Room 占位生成 MVP：`room` / `corridor` 模板、材质 palette、单次方块上限、默认只替换空气。
- [x] `/br rooms`、`/br room info <id>`、`/br room generate <id> [level]`。
- [x] WorldEdit/FAWE schematic 有限区域生成 MVP：`16x16x6` cell、模板元数据、连接口匹配、有限网格计划、marker 扫描和 generated-regions 持久化。
- [x] `/br worldgen templates`、`/br worldgen scaffold [missing|all]` 和 `/br worldgen generate <level> <size> [seed]`。
- [x] `/br debug current` 和 `/br debug config`。
- [x] Backrooms Item MVP：新增 `items.yml`、`src/main/java/.../items/` 物品模块、`/br items`、`/br item info <id>`、`/br item give <id> [player] [amount]`。
- [x] 理智值 MVP：玩家在 Level 中按配置持续降低理智，杏仁水/皇家杏仁水/记忆盐等物品可恢复理智并短时间稳定，HUD 默认接入 VectorDisplays 世界内悬浮终端，不需要客户端 mod。
- [x] 测试服已安装 PacketEvents 2.12.2 与 VectorDisplays 1.1.1，`/br verify runtime` 的 HUD 依赖链检查已通过。
- [x] Loot / Resource 已支持 `item: backrooms:<id>`，可产出 BackroomsCore 配置物品，同时继续兼容原 `material:` 掉落。
- [x] `/br level tp` 与 Transition 传送已迁移到 Paper `teleportAsync`，替换同步 `Player#teleport` 调用。
- [x] `/br reload` 已改为 staged runtime reload：Level、Item、Loot、Resource、Transition、Room、Worldgen 全量临时加载成功后统一提交，失败时保留 live runtime。
- [x] `/br verify loot` 已接入实机验证流程，可检查 Loot/Resource 配置加载数量、loot table 条目、loot source 引用、resource reward 引用和 loot table 覆盖率。
- [x] `/br loot sample <id> [rolls]` 与 `/br resource sample <id> [rolls]` 已接入，用于无在线玩家时抽样验证实际产物 ItemStack。
- [x] `/br loot source fill <id>` 已接入，可在无在线玩家时填充配置坐标的 vanilla 容器 Loot Source，并验证 one-time PDC 标记。
- [x] `/br verify items` 已接入实机验证流程，可检查 Backrooms item 加载、ItemStack 创建、右键消耗物品、理智配置、消息 key 和 HUD provider 前置依赖。
- [x] `/br verify bases` 已接入实机验证流程，可检查 Base 定义加载、Level/world/region/terminal、region overlap 和 claim 数据文件。
- [x] `/br verify transitions` 已接入实机验证流程，可检查 Transition 定义加载、source/target、trigger world、region/block trigger、message key、cooldown 和 point spawn。
- [x] `/br verify rooms` 已接入实机验证流程，可检查 Room 模板加载、Level/world 引用、palette 材质、尺寸、max blocks 和世界高度边界。
- [x] `/br verify worldgen` 已接入实机验证流程，可检查 Worldgen 模板加载、defaults、marker 材质、schematic 文件、footprint、connector 和 Level 覆盖。
- [x] CraftEngine `backrooms` 测试资源包配置：基础材料、资源点方块、容器/尸体缓存、楼梯井/维护门/撤离口/基地终端/发电机/荧光灯 marker。
- [x] 导入 Faithful Backrooms 中适合 Level 0 建图的模型/纹理，并配置为 CraftEngine `faithful_*` 方块资产。
- [x] 修正 CraftEngine Faithful 物品迁移：恢复原始 item model display / `item/generated` 图标，补齐 item textures，清理旧占位 texture key，确认模型引用不再指向 `faithfulbackrooms:`。
- [x] CraftEngine 已新增 `categories.yml`、`lang.yml`、`translations.yml`，为 Backrooms 物品/方块增加描述、分类和 i18n/l10n。
- [x] 新增 `docs/level0-cell-guide.md` 与 `docs/faithful-assets-ce.md`，用于指导 Level 0 cell/schematic 与 CE 资产制作。
- [x] 测试服 CraftEngine 配置已同步到 `D:\dev\backrooms\devserver\plugins\CraftEngine\resources\backrooms`。

### 未完成 TODO

- [x] 实机运行 `/ce reload all` 验证 CraftEngine 新增物品/方块是否加载成功；测试服日志显示 `backrooms` 包加载、items/categories/blocks 加载和资源包生成上传成功。
- [ ] 把 `resources.yml` 中默认测试 `locations` 改成真实地图资源点坐标。
- [ ] 用真实地图坐标调整 `transitions.yml` 的 Level 0 -> Level 1 和 Level 1 -> lobby 区域。
- [ ] 制作并保存真实 `plugins/backrooms/templates/level_0/*.schem` 房间模板。
- [ ] 实机运行 `/ce reload all` 验证 `backrooms:faithful_*` 建图方块模型、碰撞、灯光和 storage 行为。
- [x] 实机验证 WorldEdit/FAWE schematic 粘贴和 marker 扫描；旋转仍需后续用真实模板人工观察。
- [ ] 实机验证 `/br items`、`/br item give backrooms:almond_water`、杏仁水右键恢复理智、VectorDisplays 理智 HUD 和 loot/resource 自定义物品产出。
- [ ] 完善房间编辑框、schematic 保存命令、旧区域回收和 CE marker 替换。
- [ ] CraftEngine block id / item id 与 BackroomsCore resource/loot 的更完整适配，包括 CE item 的 PDC/API 识别与容器接入。
- [ ] CE 容器、CE 尸体方块表现和实体掉落接入 Loot Table。
- [ ] Survivor Cell、基地升级、成员权限和基地设施。
- [ ] MythicMobs 怪物、事件、实体掉落。

## 构建与部署

```powershell
.\gradlew.bat build
.\gradlew.bat deployDevServer
.\gradlew.bat syncDevServerConfig
```

`deployDevServer` 会把插件 jar 复制到本地测试服：

```text
D:\dev\backrooms\devserver\plugins
```

`syncDevServerConfig` 会把 `src/main/resources` 中的 BackroomsCore 运行时 YAML 同步到：

```text
D:\dev\backrooms\devserver\plugins\backrooms
```

如果同时改了 jar 和 BackroomsCore YAML，可以执行：

```powershell
.\gradlew.bat deployDevServerAll
```

## 世界创建

当前 BackroomsCore 不会自动创建世界，也没有自定义 world generator。需要先用 Multiverse-Core 手动创建或加载这三个世界，名字必须和配置一致：

```text
lobby
level_0
level_1
```

首次测试建议创建平地世界，不需要指定 `-g` generator：

```text
/mv create lobby NORMAL -t FLAT
/mv create level_0 NORMAL -t FLAT
/mv create level_1 NORMAL -t FLAT
```

如果世界文件夹已经存在，用导入而不是创建：

```text
/mv import lobby NORMAL
/mv import level_0 NORMAL
/mv import level_1 NORMAL
```

创建或导入后执行：

```text
/br reload
/br debug config
```

`/br debug config` 里的 missing worlds 应为 `none`。如果仍有缺失，先用 `/mv list` 确认世界是否已加载。

`level_0` 和 `level_1` 的插件出生点来自 `levels/*.yml`，默认在 `y=64` 附近。`level_1 -> lobby` 的撤离目标使用 lobby 的 world spawn，所以需要在 lobby 站到希望返回的位置后设置出生点：

```text
/mv tp lobby
/mv set spawn
```

如果你安装了 VoidGen 等空岛/虚空生成器，也可以自己用 `-g VoidGen` 创建空世界；BackroomsCore 本身不依赖这个 generator。当前 Room 生成器只是手动占位生成，不是最终自动迷宫生成器。

如果只改 CraftEngine 配置，需要同步项目内配置到测试服后，在服务器内执行：

```text
/ce reload all
```

## 基础测试流程

服务器启动并加载 `lobby`、`level_0`、`level_1` 三个世界后，按顺序测试：

```text
/br reload
/br debug config
/br verify runtime
/br verify craftengine
/br verify map
/br verify loot
/br verify items
/br verify bases
/br verify transitions
/br verify rooms
/br verify worldgen
/br levels
/br level tp level_0
/br debug current
```

如果服务器启动时曾出现 `Paper plugins do not support YAML-based command declarations`，需要部署新 jar 后完整重启服务器；`/br` 不再依赖 `paper-plugin.yml` 的 `commands:` 声明。

`/br debug config` 应重点检查：

- 缺失 Level world 是否为 `none`。
- Transition issues 是否为 `none`。
- Room issues 是否为 `none`。
- Loot Table 和 Resource 数量是否符合配置。
- Pending insurance 是否符合当前等待重生返还的玩家数量。

`/br verify runtime` 会额外检查测试服依赖和资产目录，包括 CraftEngine、FAWE/WorldEdit、Multiverse、PlaceholderAPI、VectorDisplays、PacketEvents、CraftEngine `backrooms` 资源包目录、categories/translations/lang、资源包模型/贴图数量、schematic 模板文件和当前 runtime 模块数量。

当前测试服已开启本地 RCON，便于在隐藏窗口服务器上执行验证命令：

```text
rcon.port=25575
rcon.password=backrooms-dev
```

当前测试服 runtime verifier 已全 PASS：CraftEngine 26.6、FAWE 2.15.2、Multiverse-Core 5.6.2、PlaceholderAPI 2.12.2、PacketEvents 2.12.2、VectorDisplays 1.1.1、CraftEngine 资源目录、Worldgen schematic 模板和 Sanity HUD provider 依赖链均已通过。

`/br verify craftengine` 会检查 CraftEngine `backrooms` 配置里的 item/block 定义、BackroomsCore item 是否都有 CE 镜像、`/ce menu` 分类引用、server-side l10n、client-side lang、预制模型引用和旧 `faithfulbackrooms:` 命名空间残留。当前测试服该命令已全 PASS。

`/br verify map` 会检查资源点、原版容器 loot source、Transition region world 和 Base terminal 坐标。当前测试服已在占位坐标放置原版测试锚点，命令已全 PASS；真实地图完成后仍需把这些坐标替换为正式位置。

`/br verify loot` 会检查 `loot.yml` / `resources.yml` 的配置定义是否全部加载、loot table 条目是否引用有效 Backrooms item 或 Bukkit item material、loot source/resource 是否引用存在的 loot table、数值范围是否合理，以及启用的 loot table 是否被 source 或 resource 覆盖。当前测试服该命令已全 PASS。

`/br verify items` 会检查 `items.yml` 中 Backrooms item 定义是否全部加载、每个 item 是否能创建 ItemStack、右键消耗物品的 replacement/cooldown/message/sanity effect、sanity 阈值与 decay level 引用，以及 VectorDisplays HUD provider 的前置依赖。当前测试服该命令已全 PASS。

`/br verify bases` 会检查 `bases.yml` 中 Base 定义是否全部加载、Level/world 引用、terminal 是否位于 region 内、terminal block 是否为空、同世界 base region 是否重叠，以及 `base-claims.yml` 中 claim 数据是否引用有效 Base 和 UUID。当前测试服该命令已全 PASS。

`/br verify transitions` 会检查 `transitions.yml` 中 Transition 定义是否全部加载、source Level、trigger world、region/block trigger、target Level/world、message key、cooldown、sound 和 point spawn 高度。当前测试服该命令已全 PASS。

`/br verify rooms` 会检查 `rooms.yml` 中 Room 模板是否全部加载、适用 Level/world、palette 是否为可放置方块、size 是否合理、估算方块数是否超过 `max-blocks-per-generate`，以及按当前 world spawn 高度生成是否会越界。当前测试服该命令已全 PASS。

`/br verify worldgen` 会检查 `worldgen.yml` 中 schematic 模板是否全部加载、默认 cell/路径/branch/loop 参数、WorldEdit/FAWE 可用性、模板目录、generated-regions 写入路径、marker 材质、template 文件/footprint/weight/rotation/connector/exit tag，以及已配置 Level 的模板覆盖。当前测试服该命令已全 PASS。

## CraftEngine 测试

先在服务器执行：

```text
/ce reload all
```

然后测试新增物品/方块：

```text
/ce item get backrooms:almond_water
/ce item get backrooms:fuse
/ce item get backrooms:toolbox
/ce item get backrooms:stairwell_marker
/ce item get backrooms:maintenance_door_marker
/ce item get backrooms:evacuation_hatch_marker
/ce item get backrooms:base_claim_terminal
/ce item get backrooms:generator_core
/ce item get backrooms:flickering_fluorescent_light
/ce item get backrooms:faithful_yellow_wallpaper
/ce item get backrooms:faithful_old_carpet
/ce item get backrooms:faithful_skirting_board
/ce item get backrooms:faithful_crate
/ce item get backrooms:faithful_exit_sign
```

也建议打开 `/ce menu`，确认 `Backrooms Assets` 分类下的 materials、MVP blocks、Level 0 structure、lighting、props、storage/doors 子分类能区分 blocks 和 items。

需要确认：

- 物品能拿到。
- 方块能摆放。
- 物品 tooltip 有描述，客户端语言切换时 l10n/lang 显示正常。
- 破坏后掉落符合 CE 配置。
- 客户端资源包显示正常。

如果仍看到旧模型或缺失模型，先执行 `/ce clean-cache` 后再 `/ce reload all`，并重新接收客户端资源包。

## Loot / Resource 测试

```text
/br verify loot
/br loot list
/br loot info level0_basic_supplies
/br loot sample level0_basic_supplies 10
/br loot roll level0_basic_supplies <player>
/br loot info level1_scrap_cache
/br loot sample level1_scrap_cache 10
/br loot roll level1_scrap_cache <player>
/br loot sources
/br loot source info level0_supply_container
/br loot source fill level0_supply_container
/br loot source info level1_scrap_container
/br loot source fill level1_scrap_container
/br loot source info level0_event_supply_reward
/br loot source trigger admin_scrap_reward <player>
/br loot source trigger level0_event_supply_reward <player>

/br resources
/br resource info level0_loose_carpet
/br resource sample level0_loose_carpet 10
/br resource info level1_scrap_ore
/br resource sample level1_scrap_ore 10
```

`sample` 命令不会发放物品，也不会修改世界；它只调用实际 loot/resource 产物创建路径并汇总 Backrooms item id 或 Bukkit material，适合通过 RCON 在没有在线玩家时确认配置产出链路。

`/br loot source fill <id>` 会修改测试服容器：它只支持 `vanilla_container` 类型，并要求 source 配置了固定 `locations`。命令会按配置坐标读取真实容器、调用实际 Loot Table roll、遵守 `fill-empty-only`，并在 `one-time: true` 时写入 TileState PDC。当前测试服已验证 Level 0/1 两个容器 source 的首次执行和二次防重复路径。

默认 `loot.yml` 已配置两个原版容器 loot source，占位坐标如下：

```text
level0_supply_container: x=4 y=64 z=0, material=CHEST/BARREL, loot=level0_basic_supplies
level1_scrap_container:  x=4 y=64 z=0, material=CHEST/BARREL, loot=level1_scrap_cache
```

同时提供两个 direct reward source，供事件系统或管理员命令复用：

```text
level0_event_supply_reward: type=event_reward, level=level_0, loot=level0_basic_supplies, one-time=true
admin_scrap_reward:         type=command_reward, level=any, loot=level1_scrap_cache, one-time=false
```

实机测试时，在对应 Level 世界的占位坐标放置空 `CHEST` 或 `BARREL`，执行 `/br reload` 后第一次打开容器，应自动填入对应 Loot Table 产物。`one-time: true` 会在 TileState PDC 中标记已生成，后续打开不会重复刷。无在线玩家时可先用 `/br loot source fill <id>` 验证同一条注入链路。

当前 `resources.yml` 里的默认资源点坐标仍是测试占位：

```text
level0_loose_carpet: x=0 y=64 z=0
level1_scrap_ore:    x=0 y=64 z=0
```

实际地图制作后，需要把这些坐标改成真实资源点位置，然后执行：

```text
/br reload
/br debug config
```

## Death / Corpse 测试

默认 `corpses.yml` 会在 `level_0` 和 `level_1` 启用尸体缓存：

```text
container-material: CHEST
insurance-slots: 1
placement-search-radius: 2
placement-search-height: 2
```

测试步骤：

```text
/br level tp level_0
/br item give backrooms:almond_water <player> 1
```

让玩家在 `level_0` 或 `level_1` 死亡后，应在死亡点附近生成一个 `CHEST` 尸体缓存，普通掉落进入容器；配置的 1 个保险物品堆会在重生后返还给玩家。如果死亡点没有空气可放置容器，默认保留原版掉落。

## Base Claim 测试

默认 `bases.yml` 提供两个 Level 1 占位 claim 区域：

```text
level1_utility_room_a: x=32..47 y=63..70 z=32..47
level1_storage_room_a: x=52..67 y=63..70 z=32..47
```

测试命令：

```text
/br bases
/br base info level1_utility_room_a
/br level tp level_1
/tp <player> 40 64 40
/br base claim level1_utility_room_a
```

claim 成功后，owner 可在该区域内破坏/放置方块；区域外仍受 Level 规则保护。claim 数据写入测试服 `plugins/backrooms/base-claims.yml`，后续 `/br reload` 和重启会保留。

`bases.yml` 中的 `terminal` 坐标也已接入右键交互；在终端坐标放置 `backrooms:base_claim_terminal` 或临时原版方块后，玩家右键该方块会尝试 claim 对应 Base。

## Transition 测试

默认配置：

```text
level0_to_level1_stairwell:
  world: level_0
  region: 20,63,-22 -> 28,67,-14

level1_to_lobby_evacuation:
  world: level_1
  region: -46,63,-28 -> -38,68,-20
```

测试命令：

```text
/br transitions
/br transition info level0_to_level1_stairwell
/br transition guide level0_to_level1_stairwell
/br transition trigger level0_to_level1_stairwell <player>

/br transition info level1_to_lobby_evacuation
/br transition guide level1_to_lobby_evacuation
/br transition trigger level1_to_lobby_evacuation <player>
```

地图摆放建议：

- 用 `/br transition guide <id>` 显示触发区域。
- 在区域入口附近摆放 `backrooms:stairwell_marker`、`backrooms:maintenance_door_marker` 或 `backrooms:evacuation_hatch_marker`。
- 如果实际入口位置变了，修改 `transitions.yml` 的 region 坐标后执行 `/br reload`。

## Worldgen / Schematic 测试

当前 worldgen 是管理员命令驱动的有限区域生成，不在 `PlayerMoveEvent` 或 chunk generator 中自动生成。它会按 `worldgen.yml` 读取 schematic 模板元数据，生成一个有限 `N x N` cell 网格计划，然后通过 WorldEdit/FAWE 粘贴模板并扫描 vanilla marker 方块。

需要先安装并启用 WorldEdit 或 FastAsyncWorldEdit，并把真实模板放到：

```text
plugins/backrooms/templates/level_0/*.schem
```

默认配置示例引用：

```text
level_0/basic_01.schem
level_0/straight_corridor_01.schem
level_0/corner_corridor_01.schem
level_0/stairwell_exit_01.schem
```

测试命令：

```text
/br reload
/br worldgen templates
/br worldgen scaffold missing
/br verify runtime
/br worldgen generate level_0 15 seed123
```

注意：

- `size` 会被限制为奇数范围，建议先用 `9` 或 `15` 测试。
- 生成记录会写入 `plugins/backrooms/generated-regions.yml`，同一个 region/seed 重复生成会被拒绝，避免误覆盖。
- marker 扫描 MVP 先使用 vanilla marker 材质；后续确认 CraftEngine API 后再替换为 CE block id 或粘贴后替换流程。
- 如果提示找不到 schematic 文件，可先用 `/br worldgen scaffold missing` 生成测试用 vanilla placeholder `.schem`，真实建图模板后续再覆盖。

## Room / 地图生成测试

当前不是完整自动地图生成，只是占位房间/走廊生成器。它用于实机验证 Room 配置、材质、marker 和安全边界。

默认模板：

```text
level0_basic_room
level0_corridor
level1_utility_room
```

测试步骤：

```text
/br level tp level_0
/br rooms
/br room info level0_basic_room
/br room generate level0_basic_room level_0
/br room generate level0_corridor level_0

/br level tp level_1
/br room info level1_utility_room
/br room generate level1_utility_room level_1
```

注意：

- `rooms.yml` 默认 `replace-air-only: true`，不会覆盖已有方块。
- 建议站在空旷区域测试；如果地面或墙体已有方块，生成器会跳过这些位置。
- 如果需要临时覆盖测试，把 `rooms.defaults.replace-air-only` 改成 `false` 后执行 `/br reload`，测试完再改回 `true`。
- 现在生成的是简单占位房间，不是最终迷宫系统。

## 保护规则测试

用普通玩家账号在 `level_0` / `level_1` 测试：

- 破坏普通方块应被拦截。
- 放置普通方块应被拦截。
- 倒水/倒岩浆应被拦截。
- 点火应被拦截。
- 爆炸不应破坏 Level 地图。
- PVP 应被拦截。
- 资源点坐标处的配置方块应能按 `resources.yml` 触发。

管理员如需临时建图，可授予：

```text
backrooms.bypass.build
```

## 常用排查命令

```text
/br debug current
/br debug config
/br reload
/br level info level_0
/br level info level_1
/br resources
/br loot list
/br transitions
/br rooms
/br worldgen templates
```
