# BackroomsCore

BackroomsCore 是面向 Paper 1.21.4 的后室主题服务器核心插件。当前目标是先做可运行、可热重载、可实机验证的 MVP 闭环：Level 0、Level 1、基础切层、资源点、战利品表、房间占位生成和 CraftEngine 测试资产。

## 当前状态

### 已完成

- [x] Paper 1.21.4 插件基础框架。
- [x] `/br` 主命令、help、tab completion 和权限声明。
- [x] `/br` 已改为 Paper `JavaPlugin#registerCommand` 注册，避免 Paper plugin 启动期调用 `JavaPlugin#getCommand()`。
- [x] 配置拆分：`messages.yml`、`settings/config.yml`、`levels/*.yml`、`resources.yml`、`transitions.yml`、`rooms.yml`、`loot.yml`。
- [x] Level registry：支持配置化 Level id、world、随机 spawn、规则、标题。
- [x] `/br level tp <id>` 和 `/br level info <id>`。
- [x] 玩家当前 Level 运行时追踪。
- [x] Level 规则保护：禁止普通破坏/放置、禁止 PVP、拦截桶、火、爆炸、实体改方块等常见绕过。
- [x] `/br reload` 热重载，带基础失败保留旧 Level registry 的安全保护。
- [x] Resource 方块 MVP：Material + 可选坐标 `locations` + cooldown + drops + loot table。
- [x] `/br resources`、`/br resource info <id>` 调试命令。
- [x] Loot Table MVP：命名掉落池、rolls、chance、手动抽取测试。
- [x] `/br loot list`、`/br loot info <id>`、`/br loot roll <id> [player]`。
- [x] Transition MVP：region 触发、右键方块触发预留、Level/world 目标、冷却、传送后免触发保护。
- [x] `/br transitions`、`/br transition info <id>`、`/br transition trigger <id> [player]`、`/br transition guide <id>`。
- [x] Room 占位生成 MVP：`room` / `corridor` 模板、材质 palette、单次方块上限、默认只替换空气。
- [x] `/br rooms`、`/br room info <id>`、`/br room generate <id> [level]`。
- [x] `/br debug current` 和 `/br debug config`。
- [x] CraftEngine `backrooms` 测试资源包配置：基础材料、资源点方块、容器/尸体缓存、楼梯井/维护门/撤离口/基地终端/发电机/荧光灯 marker。
- [x] 测试服 CraftEngine 配置已同步到 `D:\dev\backrooms\devserver\plugins\CraftEngine\resources\backrooms`。

### 未完成 TODO

- [ ] 实机运行 `/ce reload all` 验证 CraftEngine 新增物品/方块是否加载成功。
- [ ] 把 `resources.yml` 中默认测试 `locations` 改成真实地图资源点坐标。
- [ ] 用真实地图坐标调整 `transitions.yml` 的 Level 0 -> Level 1 和 Level 1 -> lobby 区域。
- [ ] WorldEdit/FAWE schematic 粘贴与 marker 扫描。
- [ ] 自动房间拼接、迷宫/区域生成和旧区域回收。
- [ ] CraftEngine block id / item id 与 BackroomsCore resource/loot 的正式适配。
- [ ] 原版容器、CE 容器、尸体方块和事件奖励接入 Loot Table。
- [ ] 基地 claim、Survivor Cell、基地升级和权限。
- [ ] MythicMobs 怪物、事件、实体掉落。
- [ ] 异步传送 API 迁移，替换当前可用但过时的同步 teleport 调用。
- [ ] 更完整的 reload 事务化：Level、Resource、Loot、Transition、Room 全量临时加载后统一 swap。

## 构建与部署

```powershell
.\gradlew.bat build
.\gradlew.bat deployDevServer
```

`deployDevServer` 会把插件 jar 复制到本地测试服：

```text
D:\dev\backrooms\devserver\plugins
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
```

需要确认：

- 物品能拿到。
- 方块能摆放。
- 破坏后掉落符合 CE 配置。
- 客户端资源包显示正常。

## Loot / Resource 测试

```text
/br loot list
/br loot info level0_basic_supplies
/br loot roll level0_basic_supplies <player>
/br loot info level1_scrap_cache
/br loot roll level1_scrap_cache <player>

/br resources
/br resource info level0_loose_carpet
/br resource info level1_scrap_ore
```

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
```
