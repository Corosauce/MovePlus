package com.syszee.mod.common.registry;

import com.syszee.mod.ModMain;
import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class ModItems
{
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ModMain.MOD_ID);

    // public static final RegistryObject<Item> EXAMPLE_ITEM = ITEMS.register("example", () -> new Item(new Item.Properties().group(ItemGroup.MISC)));

    /* Registry Methods */

    /**
     * Registers a new item under the specified name.
     *
     * @param name The name of the item
     * @param item The item to register
     * @return The object created when registering the item
     */
    public static <T extends Item> RegistryObject<T> register(String name, Supplier<? extends T> item)
    {
        RegistryObject<T> object = ITEMS.register(name, item);
        // If there is a tab
        // ModMain.TAB.getOrderedItems().add(object);
        return object;
    }
}