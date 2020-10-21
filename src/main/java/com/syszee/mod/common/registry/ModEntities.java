package com.syszee.mod.common.registry;

import com.syszee.mod.ModMain;
import io.github.ocelot.sonar.common.item.SpawnEggItemBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = ModMain.MOD_ID)
public class ModEntities
{
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, ModMain.MOD_ID);

    // public static final RegistryObject<EntityType<ExampleEntity>> EXAMPLE = register("example", EntityType.Builder.create(ExampleEntity::new, EntityClassification.MISC).size(1.0F, 1.0F).trackingRange(10), 0xff00ff, 0xff00ff);

    @SubscribeEvent
    public static void onEvent(BiomeLoadingEvent event)
    {
        // if (event.getCategory() == Biome.Category.PLAINS)
        // event.getSpawns().getSpawner(EntityClassification.MISC).add(new MobSpawnInfo.Spawners(EXAMPLE.get(), 8, 4, 4));
    }

    /* Registry Methods */

    /**
     * Registers a new entity with an egg under the specified id.
     *
     * @param id             The id of the entity
     * @param builder        The entity builder
     * @param primaryColor   The egg color of the egg item
     * @param secondaryColor The spot color for the egg item
     * @param <T>            The type of entity being created
     */
    private static <T extends Entity> RegistryObject<EntityType<T>> register(String id, EntityType.Builder<T> builder, int primaryColor, int secondaryColor)
    {
        RegistryObject<EntityType<T>> object = register(id, builder);
        ModItems.ITEMS.register(id + "_spawn_egg", () -> new SpawnEggItemBase<>(object, primaryColor, secondaryColor, true, new Item.Properties().group(ItemGroup.MISC)));
        return object;
    }

    /**
     * Registers a new entity under the specified id.
     *
     * @param id      The id of the entity
     * @param builder The entity builder
     * @param <T>     The type of entity being created
     */
    private static <T extends Entity> RegistryObject<EntityType<T>> register(String id, EntityType.Builder<T> builder)
    {
        return ENTITIES.register(id, () -> builder.build(id));
    }
}