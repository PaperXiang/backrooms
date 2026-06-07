package org.monday.backrooms.worldgen;

import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockTypes;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

final class WorldEditSchematicScaffolder {

    private static final int CONNECTOR_MIN = 6;
    private static final int CONNECTOR_MAX = 9;

    private WorldEditSchematicScaffolder() {
    }

    static void write(SchematicTemplateDefinition template) throws IOException {
        File parent = template.file().getParentFile();
        if (parent != null && !parent.isDirectory() && !parent.mkdirs()) {
            throw new IOException("Could not create template directory: " + parent.getPath());
        }

        int maxX = template.cellSize() * template.footprintX() - 1;
        int maxY = template.footprintY() - 1;
        int maxZ = template.cellSize() * template.footprintZ() - 1;
        BlockArrayClipboard clipboard = new BlockArrayClipboard(new CuboidRegion(BlockVector3.ZERO, BlockVector3.at(maxX, maxY, maxZ)));
        clipboard.setOrigin(BlockVector3.ZERO);

        fillLevelZeroShell(clipboard, template, maxX, maxY, maxZ);
        placeMarkers(clipboard, template, maxY);

        try (ClipboardWriter writer = BuiltInClipboardFormat.SPONGE_SCHEMATIC.getWriter(new FileOutputStream(template.file()))) {
            writer.write(clipboard);
        }
    }

    private static void fillLevelZeroShell(BlockArrayClipboard clipboard, SchematicTemplateDefinition template, int maxX, int maxY, int maxZ) {
        BlockState floor = BlockTypes.SMOOTH_SANDSTONE.getDefaultState();
        BlockState ceiling = BlockTypes.SMOOTH_STONE.getDefaultState();
        BlockState wall = BlockTypes.YELLOW_TERRACOTTA.getDefaultState();
        BlockState trim = BlockTypes.STRIPPED_BIRCH_LOG.getDefaultState();

        for (int x = 0; x <= maxX; x++) {
            for (int z = 0; z <= maxZ; z++) {
                set(clipboard, x, 0, z, floor);
                set(clipboard, x, maxY, z, ceiling);
            }
        }

        for (int y = 1; y < maxY; y++) {
            for (int x = 0; x <= maxX; x++) {
                if (!isConnectorOpening(template, TemplateConnector.NORTH, x, y, 0, maxX, maxZ)) {
                    set(clipboard, x, y, 0, wall);
                }
                if (!isConnectorOpening(template, TemplateConnector.SOUTH, x, y, maxZ, maxX, maxZ)) {
                    set(clipboard, x, y, maxZ, wall);
                }
            }
            for (int z = 0; z <= maxZ; z++) {
                if (!isConnectorOpening(template, TemplateConnector.WEST, 0, y, z, maxX, maxZ)) {
                    set(clipboard, 0, y, z, wall);
                }
                if (!isConnectorOpening(template, TemplateConnector.EAST, maxX, y, z, maxX, maxZ)) {
                    set(clipboard, maxX, y, z, wall);
                }
            }
        }

        for (int y = 1; y < maxY; y++) {
            set(clipboard, 0, y, 0, trim);
            set(clipboard, maxX, y, 0, trim);
            set(clipboard, 0, y, maxZ, trim);
            set(clipboard, maxX, y, maxZ, trim);
        }
    }

    private static boolean isConnectorOpening(SchematicTemplateDefinition template, TemplateConnector connector,
                                              int x, int y, int z, int maxX, int maxZ) {
        if (!template.connectors().contains(connector) || y <= 1) {
            return false;
        }
        return switch (connector) {
            case NORTH, SOUTH -> z == (connector == TemplateConnector.NORTH ? 0 : maxZ)
                    && x >= CONNECTOR_MIN && x <= Math.min(CONNECTOR_MAX, maxX);
            case EAST, WEST -> x == (connector == TemplateConnector.WEST ? 0 : maxX)
                    && z >= CONNECTOR_MIN && z <= Math.min(CONNECTOR_MAX, maxZ);
        };
    }

    private static void placeMarkers(BlockArrayClipboard clipboard, SchematicTemplateDefinition template, int maxY) {
        set(clipboard, 2, 1, 2, BlockTypes.YELLOW_CARPET.getDefaultState());
        set(clipboard, 7, maxY, 7, BlockTypes.SEA_LANTERN.getDefaultState());

        if (template.hasTag("exit")) {
            set(clipboard, 7, 1, 7, BlockTypes.LODESTONE.getDefaultState());
            set(clipboard, 8, 1, 7, BlockTypes.CHISELED_STONE_BRICKS.getDefaultState());
            return;
        }

        if (template.hasTag("common") || template.hasTag("liminal")) {
            set(clipboard, 4, 1, 4, BlockTypes.BARREL.getDefaultState());
        }
    }

    private static void set(BlockArrayClipboard clipboard, int x, int y, int z, BlockState block) {
        clipboard.setBlock(BlockVector3.at(x, y, z), block);
    }
}
