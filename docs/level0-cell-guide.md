# Level 0 Cell 制作指南

本文用于制作 BackroomsCore Level 0 的 FAWE / WorldEdit schematic 房间模板。当前 Level 0 不做真正无限迷宫，而是采用有限区域拼接：

```text
一个生成区域 = N x N 个 cell
一个 cell = 16 x 16 x 6
一个房间模板 = 一个或多个 cell 的 schematic
生成器 = 先生成连通图，再根据连接口、权重、标签选择模板并粘贴
```

## 1. 标准 cell 尺寸

第一阶段所有模板优先做 `1x1`：

```text
尺寸：16 x 16 x 6
地板：y=0
内部可用高度：y=1 到 y=4
天花板：y=5
墙厚：1
门洞：3 宽 x 3 高
```

门洞固定在四边中点：

```text
north 门：z=0 墙上，x=6,7,8，y=1,2,3
south 门：z=15 墙上，x=6,7,8，y=1,2,3
west 门：x=0 墙上，z=6,7,8，y=1,2,3
east 门：x=15 墙上，z=6,7,8，y=1,2,3
```

所有模板必须严格遵守门洞位置，否则旋转和拼接后会错位。

## 2. marker / 占位方块

当前 CraftEngine 方块是否能稳定保存进 schematic 还需要实机验证，所以第一阶段使用原版方块作为 marker。这些 marker 是给插件扫描用的，不一定是最终玩家看到的方块。

| 原版 marker | 配置 key | 当前意义 | 后续目标 |
|---|---|---|---|
| `YELLOW_CARPET` | `resource` | Level 0 地毯资源点 | 替换为 `backrooms:loose_carpet_cache` |
| `SEA_LANTERN` | `light` | 荧光灯 marker | 替换为 `backrooms:flickering_fluorescent_light` |
| `BARREL` | `loot` | 物资箱 / loot 点 | 替换或注册为 `backrooms:supply_crate` |
| `LODESTONE` | `transition` | 真出口 / 切层触发点 | 注册 transition，可能替换为空气或 CE 标记 |
| `IRON_TRAPDOOR` | `maintenance-door` | 维护门 marker | 替换为 `backrooms:maintenance_door_marker` |
| `CHISELED_STONE_BRICKS` | `stairwell` | 楼梯井视觉 marker | 替换为 `backrooms:stairwell_marker` |

关键区分：

```text
假楼梯井：可以有 CHISELED_STONE_BRICKS，但不要放 LODESTONE
真楼梯井：必须有 CHISELED_STONE_BRICKS + LODESTONE
```

## 3. Level 0 推荐 cell 类型

| 类型 | 建议数量 | 主要功能 | 出现频率 |
|---|---:|---|---|
| `common_room` | 8-12 | Level 0 主体黄墙纸房间 | 最高 |
| `straight_corridor` | 4-6 | 拉长空间，制造走廊感 | 高 |
| `corner_corridor` | 4-6 | 打断视线，制造迷路 | 中高 |
| `t_junction` | 3-4 | 路线选择，形成分支 | 中 |
| `cross_junction` | 2-3 | 少量开放交叉路口 | 低 |
| `dead_end` | 4-6 | 资源、假线索、恐怖点 | 中 |
| `office_cluster` | 3-5 | 办公区变化 | 中低 |
| `maintenance_room` | 2-4 | 维护间、工具资源、路线暗示 | 低 |
| `false_stairwell` | 2-4 | 假出口，欺骗玩家 | 很低 |
| `stairwell_exit` | 1-2 | 真正通往 Level 1 的出口 | 极低 |
| `anomaly_room` | 2-4 | 稀有异常空间 | 极低 |
| `supply_room` | 2-3 | 小补给间 | 低 |
| `dark_room` | 2-3 | 暗房、恐怖氛围 | 低 |

第一阶段最低推荐制作：

```text
common_room_01
common_room_02
straight_corridor_01
corner_corridor_01
t_junction_01
dead_end_cache_01
false_stairwell_01
stairwell_exit_01
```

更理想的第一批模板：

```text
common_room_01
common_room_02
common_room_03
common_room_04
straight_corridor_01
straight_corridor_02
corner_corridor_01
corner_corridor_02
t_junction_01
cross_junction_01
dead_end_cache_01
dead_end_dark_01
maintenance_room_01
false_stairwell_01
stairwell_exit_01
```

## 4. 各 cell 制作要点

### common_room 普通黄墙纸房间

- 功能：Level 0 主体。
- 数量：8-12 个变体。
- connectors：四通、三通、两通都可以做。
- marker：`SEA_LANTERN` 1-3 个；`YELLOW_CARPET` 0-1 个；`BARREL` 0-1 个；不要放 `LODESTONE` 和 `CHISELED_STONE_BRICKS`。
- 变体：墙纸破损、灯更少、柱子错位、地毯颜色偏差、假门痕迹、低矮隔断。

### straight_corridor 直走廊

- 功能：拉长空间，让玩家产生走了很久的感觉。
- connectors：`north, south`，旋转后可变成 `east, west`。
- marker：`SEA_LANTERN` 2-4 个；`YELLOW_CARPET` 0-1 个；`IRON_TRAPDOOR` 0-1 个。
- 不建议频繁放 `BARREL`，不要放 `LODESTONE` 或 `CHISELED_STONE_BRICKS`。

### corner_corridor 转角

- 功能：打断视线，制造迷路。
- connectors：`north, east`，旋转后可变成其他转角。
- marker：`SEA_LANTERN` 1-2 个；`IRON_TRAPDOOR` 0-1 个；`YELLOW_CARPET` 0-1 个；`BARREL` 0-1 个。

### t_junction / cross_junction 路口

- 功能：路线选择和迷失感。
- T 字 connectors 示例：`north, east, west` 或 `north, east, south`。
- 十字 connectors：`north, east, south, west`。
- marker：中心灯、少量维护门或地毯资源点；不要放出口 marker。

### dead_end 死路

- 功能：奖励、恐怖、假线索。
- connectors：只有一个方向，例如 `south`。
- marker：`YELLOW_CARPET` 1-2 个；`BARREL` 0-1 个；`IRON_TRAPDOOR` 0-1 个；`SEA_LANTERN` 0-1 个。
- 普通死路不要放 `LODESTONE`。

### maintenance_room 维护间

- 功能：放维修工具、fuse、toolbox、路线暗示。
- connectors：一个入口或两个入口。
- marker：`IRON_TRAPDOOR` 1-2 个；`BARREL` 1-2 个；`SEA_LANTERN` 1 个；`YELLOW_CARPET` 0-1 个。
- `LODESTONE` 只在未来机器事件点中谨慎使用。

### false_stairwell 假楼梯井

- 功能：欺骗玩家，让玩家以为找到出口。
- connectors：通常一个入口。
- marker：`CHISELED_STONE_BRICKS` 1-3 个；`IRON_TRAPDOOR` 1 个；`SEA_LANTERN` 0-1 个；`BARREL` 0-1 个。
- 绝对不要放 `LODESTONE`。

### stairwell_exit 真出口

- 功能：真正通往 Level 1。
- connectors：通常一个入口，例如 `south`。
- 必放：`CHISELED_STONE_BRICKS` 1-3 个 + `LODESTONE` 1 个。
- 建议额外放：`IRON_TRAPDOOR` 1 个，`SEA_LANTERN` 1 个。

### anomaly_room 异常房

- 功能：稀有恐怖空间。
- marker：可用错位 `SEA_LANTERN`、少量特殊 `BARREL`、异常 `IRON_TRAPDOOR`。
- `LODESTONE` 只作为未来事件点谨慎使用。

## 5. 保存目录和配置

schematic 保存到：

```text
plugins/BackroomsCore/templates/level_0/
```

当前 `worldgen.yml` 默认只配置了：

```text
basic_01.schem
straight_corridor_01.schem
corner_corridor_01.schem
stairwell_exit_01.schem
```

新增更多 schematic 后，需要同步添加到 `worldgen.yml` 的 `worldgen.templates` 下。

## 6. 建图原则

Level 0 的重点不是复杂结构，而是：

```text
重复
轻微差异
低频功能房
稀有异常
真出口极少
假出口存在
```

推荐比例：

```text
70% 普通房间 / 走廊
15% 轻微变化
10% 功能房 / 资源房
4% 异常房
1% 真出口
```

不要每个房间都放资源或箱子。Level 0 应该是探索、迷路、紧张，而不是刷箱子地图。
# Faithful Backrooms 资产导入说明

本次从 `D:/dev/backrooms/faithfulbackrooms` 选择适合 Level 0 建图的模型和纹理，导入到 CraftEngine `backrooms` 资源包。

## 导入位置

```text
server-configs/CraftEngine/resources/backrooms/assets/backrooms/models/block/faithful/
server-configs/CraftEngine/resources/backrooms/assets/backrooms/models/custom/faithful/
server-configs/CraftEngine/resources/backrooms/assets/backrooms/models/item/faithful/
server-configs/CraftEngine/resources/backrooms/assets/backrooms/textures/block/faithful/
server-configs/CraftEngine/resources/backrooms/configuration/blocks/faithful_level0_blocks.yml
```

## CraftEngine 配置原则

- item 使用 `behavior: block_item` 绑定同名 block。
- 已有 pre-made model file 的 block 只配置 `state.model.path`，不再让 CE 重新 generation。
- 墙体、地毯、天花板、crate 等完整方块使用 `auto_state: solid`。
- 灯具、管道、踢脚线、牌子、CCTV、插座等非完整装饰使用 `auto_state: lower_tripwire`，并关闭 suffocation / view blocking / occlusion。
- crate 系列临时使用 `simple_storage_block`，方便后续接 loot container。
- 贴图路径统一改写为 `backrooms:block/faithful/<texture>`，模型路径统一改写为 `backrooms:block/faithful/<model>` 或 `backrooms:custom/faithful/<model>`。

## 本次配置的 CE 方块

- `backrooms:faithful_yellow_wallpaper` - Level 0 Yellow Wallpaper
- `backrooms:faithful_formless_yellow_wallpaper` - Formless Yellow Wallpaper
- `backrooms:faithful_moist_wallpaper` - Moist Wallpaper
- `backrooms:faithful_strange_wallpaper` - Strange Wallpaper
- `backrooms:faithful_manilla_wallpaper` - Manilla Wallpaper
- `backrooms:faithful_beige_wall` - Beige Wall
- `backrooms:faithful_beige_drywall` - Beige Drywall
- `backrooms:faithful_yellow_wallpaper_half_wall` - Yellow Wallpaper Half Wall
- `backrooms:faithful_old_carpet` - Old Carpet
- `backrooms:faithful_moist_carpet` - Moist Carpet
- `backrooms:faithful_office_carpet` - Office Carpet
- `backrooms:faithful_pale_carpet` - Pale Carpet
- `backrooms:faithful_old_ceiling` - Old Ceiling Tile
- `backrooms:faithful_modern_ceiling` - Modern Ceiling Tile
- `backrooms:faithful_remodelled_ceiling` - Remodelled Ceiling Tile
- `backrooms:faithful_fallen_ceiling_tile` - Fallen Ceiling Tile
- `backrooms:faithful_ceiling_light` - Ceiling Light
- `backrooms:faithful_broken_ceiling_light` - Broken Ceiling Light
- `backrooms:faithful_modern_ceiling_light` - Modern Ceiling Light
- `backrooms:faithful_modern_ceiling_lightbar` - Modern Ceiling Lightbar
- `backrooms:faithful_industrial_wall_light` - Industrial Wall Light
- `backrooms:faithful_industrial_wall_light_small` - Small Industrial Wall Light
- `backrooms:faithful_crate` - Crate
- `backrooms:faithful_box_crate` - Box Crate
- `backrooms:faithful_plastic_crate` - Plastic Crate
- `backrooms:faithful_reinforced_crate` - Reinforced Crate
- `backrooms:faithful_wide_crate` - Wide Crate
- `backrooms:faithful_cabinet` - Cabinet
- `backrooms:faithful_filing_cabinet` - Filing Cabinet
- `backrooms:faithful_bookshelf` - Empty Bookshelf
- `backrooms:faithful_bulletin_board` - Bulletin Board
- `backrooms:faithful_wall_power_outlet` - Wall Power Outlet
- `backrooms:faithful_modern_vent` - Modern Vent
- `backrooms:faithful_plumbing_pipe` - Plumbing Pipe
- `backrooms:faithful_rusty_plumbing_pipe` - Rusty Plumbing Pipe
- `backrooms:faithful_drain_pipe` - Drain Pipe
- `backrooms:faithful_skirting_board` - Skirting Board
- `backrooms:faithful_white_skirting_board` - White Skirting Board
- `backrooms:faithful_black_mould` - Black Mould
- `backrooms:faithful_exit_sign` - Exit Sign
- `backrooms:faithful_warning_sign` - Warning Sign
- `backrooms:faithful_window_board` - Window Board
- `backrooms:faithful_cctv_camera` - CCTV Camera
- `backrooms:faithful_cardboard_box` - Cardboard Box
- `backrooms:faithful_metal_employee_door` - Metal Employee Door
- `backrooms:faithful_employee_door` - Employee Door
- `backrooms:faithful_locked_door` - Locked Door

## 实机验证

同步到测试服后执行：

```text
/ce reload all
/ce item get backrooms:faithful_yellow_wallpaper
/ce item get backrooms:faithful_old_carpet
/ce item get backrooms:faithful_ceiling_light
/ce item get backrooms:faithful_crate
/ce item get backrooms:faithful_exit_sign
```

需要重点确认：模型是否显示、碰撞是否符合预期、灯是否发光、storage 方块能否打开。
