# Faithful Backrooms 资产导入说明

本次从 `D:/dev/backrooms/faithfulbackrooms` 选择适合 Level 0 建图的模型和纹理，导入到 CraftEngine `backrooms` 资源包。

## 导入位置

```text
server-configs/CraftEngine/resources/backrooms/resourcepack/assets/backrooms/models/block/faithful/
server-configs/CraftEngine/resources/backrooms/resourcepack/assets/backrooms/models/custom/faithful/
server-configs/CraftEngine/resources/backrooms/resourcepack/assets/backrooms/models/item/faithful/
server-configs/CraftEngine/resources/backrooms/resourcepack/assets/backrooms/textures/block/faithful/
server-configs/CraftEngine/resources/backrooms/configuration/blocks/faithful_level0_blocks.yml
```

## CraftEngine 配置原则

- item 使用 `behavior: block_item` 绑定同名 block。
- 已有 pre-made model file 的 block 只配置 `state.model.path`，不再让 CE 重新 generation。
- 墙体、地毯、天花板、crate 等完整方块使用 `auto_state: note_block`，不要使用 `solid`，因为 `solid` 可能自动分配到 mushroom 系列状态，透明/遮挡表现不适合当前资源包。
- 灯具、管道、踢脚线、牌子、CCTV、插座等非完整装饰使用 `auto_state: lower_tripwire`，并关闭 suffocation / view blocking / occlusion。
- crate 系列临时使用 `simple_storage_block`，方便后续接 loot container。
- 贴图路径统一改写为 `backrooms:block/faithful/<texture>`，模型路径统一改写为 `backrooms:block/faithful/<model>` 或 `backrooms:custom/faithful/<model>`。
- 注意：CraftEngine 资源包文件需要放在资源目录的 `resourcepack/assets/...` 下；直接放在 `assets/...` 会导致 `/ce reload all` 提示缺少模型文件。

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
