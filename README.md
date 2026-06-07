# BackroomsCore

BackroomsCore 是面向 Paper 1.21.4 的后室主题服务器核心插件。当前目标是先做可运行、可热重载、可实机验证的 MVP 闭环：Level 0、Level 1、基础切层、资源点、战利品表、房间占位生成和 CraftEngine 测试资产。

## 当前状态

### 已完成

- [x] Paper 1.21.4 插件基础框架。
- [x] `/br` 主命令、help、tab completion 和权限声明。
- [x] `/br` 已改为 Paper `JavaPlugin#registerCommand` 注册，避免 Paper plugin 启动期调用 `JavaPlugin#getCommand()`。
- [x] 配置拆分：`messages.yml`、`settings/config.yml`、`levels/*.yml`、`resources.yml`、`transitions.yml`、`rooms.yml`、`loot.yml`、`corpses.yml`、`worldgen.yml`。
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
- [x] Transition MVP：region 触发、右键方块触发预留、Level/world 目标、冷却、传送后免触发保护。
- [x] `/br transitions`、`/br transition info <id>`、`/br transition trigger <id> [player]`、`/br transition guide <id>`。
- [x] Room 占位生成 MVP：`room` / `corridor` 模板、材质 palette、单次方块上限、默认只替换空气。
- [x] `/br rooms`、`/br room info <id>`、`/br room generate <id> [level]`。
- [x] WorldEdit/FAWE schematic 有限区域生成 MVP：`16x16x6` cell、模板元数据、连接口匹配、有限网格计划、marker 扫描和 generated-regions 持久化。
- [x] `/br worldgen templates` 和 `/br worldgen generate <level> <size> [seed]`。
- [x] `/br debug current` 和 `/br debug config`。
- [x] Backrooms Item MVP：新增 `items.yml`、`src/main/java/.../items/` 物品模块、`/br items`、`/br item info <id>`、`/br item give <id> [player] [amount]`。
- [x] 理智值 MVP：玩家在 Level 中按配置持续降低理智，杏仁水/皇家杏仁水/记忆盐等物品可恢复理智并短时间稳定，HUD 默认接入 VectorDisplays 世界内悬浮终端，不需要客户端 mod。
- [x] Loot / Resource 已支持 `item: backrooms:<id>`，可产出 BackroomsCore 配置物品，同时继续兼容原 `material:` 掉落。
- [x] `/br level tp` 与 Transition 传送已迁移到 Paper `teleportAsync`，替换同步 `Player#teleport` 调用。
- [x] `/br reload` 已改为 staged runtime reload：Level、Item、Loot、Resource、Transition、Room、Worldgen 全量临时加载成功后统一提交，失败时保留 live runtime。
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
- [ ] 制作并保存真实 `plugins/BackroomsCore/templates/level_0/*.schem` 房间模板。
- [ ] 实机运行 `/ce reload all` 验证 `backrooms:faithful_*` 建图方块模型、碰撞、灯光和 storage 行为。
- [ ] 实机验证 WorldEdit/FAWE schematic 粘贴、旋转和 marker 扫描。
- [ ] 实机验证 `/br items`、`/br item give backrooms:almond_water`、杏仁水右键恢复理智、VectorDisplays 理智 HUD 和 loot/resource 自定义物品产出。
- [ ] 完善房间编辑框、schematic 保存命令、旧区域回收和 CE marker 替换。
- [ ] CraftEngine block id / item id 与 BackroomsCore resource/loot 的更完整适配，包括 CE item 的 PDC/API 识别与容器接入。
- [ ] CE 容器、CE 尸体方块表现和实体掉落接入 Loot Table。
- [ ] 基地 claim、Survivor Cell、基地升级和权限。
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
/br loot list
/br loot info level0_basic_supplies
/br loot roll level0_basic_supplies <player>
/br loot info level1_scrap_cache
/br loot roll level1_scrap_cache <player>
/br loot sources
/br loot source info level0_supply_container
/br loot source info level1_scrap_container
/br loot source info level0_event_supply_reward
/br loot source trigger admin_scrap_reward <player>
/br loot source trigger level0_event_supply_reward <player>

/br resources
/br resource info level0_loose_carpet
/br resource info level1_scrap_ore
```

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

实机测试时，在对应 Level 世界的占位坐标放置空 `CHEST` 或 `BARREL`，执行 `/br reload` 后第一次打开容器，应自动填入对应 Loot Table 产物。`one-time: true` 会在 TileState PDC 中标记已生成，后续打开不会重复刷。

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
plugins/BackroomsCore/templates/level_0/*.schem
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
/br worldgen generate level_0 15 seed123
```

注意：

- `size` 会被限制为奇数范围，建议先用 `9` 或 `15` 测试。
- 生成记录会写入 `plugins/BackroomsCore/generated-regions.yml`，同一个 region/seed 重复生成会被拒绝，避免误覆盖。
- marker 扫描 MVP 先使用 vanilla marker 材质；后续确认 CraftEngine API 后再替换为 CE block id 或粘贴后替换流程。
- 如果提示找不到 schematic 文件，先用 WorldEdit/FAWE 手动制作并保存对应 `.schem`。

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
