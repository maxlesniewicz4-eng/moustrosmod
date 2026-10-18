package com.monstruos;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.Heightmap;

import java.util.ArrayList;
import java.util.List;

public class MonstruosMod implements ModInitializer {
    public static final String MOD_ID = "monstruosmod";
    public static final Identifier JUMPSCARE_PACKET = new Identifier(MOD_ID, "jumpscare");

    public static final int COUNT = 4;
    // Tamaño de la hitbox (bloques)
    public static final float[] WIDTH  = {1.2f, 0.7f, 0.6f, 0.6f};
    public static final float[] HEIGHT = {1.5f, 2.8f, 2.7f, 3.0f};
    // Tamaño en píxeles de las texturas del modelo (entity/monstruo_N.png)
    public static final int[] MODEL_W = {256, 126, 77, 81};
    public static final int[] MODEL_H = {229, 256, 256, 256};
    // Tamaño en píxeles de las imágenes del susto (gui/jumpscare_N.png)
    public static final int[] GUI_W = {512, 209, 155, 162};
    public static final int[] GUI_H = {458, 423, 512, 512};

    public static final List<EntityType<MonstruoEntity>> TYPES = new ArrayList<>();

    @Override
    public void onInitialize() {
        for (int v = 1; v <= COUNT; v++) {
            final int variant = v;

            EntityType<MonstruoEntity> type = Registry.register(
                    Registries.ENTITY_TYPE,
                    new Identifier(MOD_ID, "monstruo_" + v),
                    FabricEntityTypeBuilder.<MonstruoEntity>create(SpawnGroup.MONSTER,
                                    (t, w) -> new MonstruoEntity(t, w, variant))
                            .dimensions(EntityDimensions.fixed(WIDTH[v - 1], HEIGHT[v - 1]))
                            .trackRangeBlocks(80)
                            .build());

            FabricDefaultAttributeRegistry.register(type, MonstruoEntity.createAttributes());

            // Aparecen de noche / en oscuridad en el Overworld
            SpawnRestriction.register(type, SpawnRestriction.Location.ON_GROUND,
                    Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, HostileEntity::canSpawnInDark);
            BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(),
                    SpawnGroup.MONSTER, type, 6, 1, 1);

            Item egg = Registry.register(Registries.ITEM,
                    new Identifier(MOD_ID, "monstruo_" + v + "_spawn_egg"),
                    new SpawnEggItem(type, 0x111111, 0xAA0000, new FabricItemSettings()));
            ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register(entries -> entries.add(egg));

            TYPES.add(type);
        }
    }
}
