package org.monday.backrooms.worldgen;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.session.ClipboardHolder;
import java.io.FileInputStream;
import java.io.IOException;
import org.bukkit.World;

final class WorldEditSchematicPaster {

    private WorldEditSchematicPaster() {
    }

    static void paste(World world, SchematicTemplateDefinition template, int rotation, int x, int y, int z) {
        ClipboardFormat format = ClipboardFormats.findByFile(template.file());
        if (format == null) {
            throw new IllegalStateException("Unknown schematic format for template '" + template.id() + "': " + template.file().getName());
        }

        try (ClipboardReader reader = format.getReader(new FileInputStream(template.file()))) {
            Clipboard clipboard = reader.read();
            com.sk89q.worldedit.world.World adaptedWorld = BukkitAdapter.adapt(world);
            try (EditSession editSession = WorldEdit.getInstance().newEditSession(adaptedWorld)) {
                ClipboardHolder holder = new ClipboardHolder(clipboard);
                if (rotation != 0) {
                    holder.setTransform(new AffineTransform().rotateY(rotation));
                }
                Operation operation = holder.createPaste(editSession)
                        .to(BlockVector3.at(x, y, z))
                        .ignoreAirBlocks(!template.pasteAir())
                        .build();
                Operations.complete(operation);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read schematic for template '" + template.id() + "': " + exception.getMessage(), exception);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not paste schematic for template '" + template.id() + "': " + exception.getMessage(), exception);
        }
    }
}
