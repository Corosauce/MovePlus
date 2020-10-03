package com.syszee.mod.common.registry;

import com.syszee.mod.ModMain;
import net.minecraft.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModItems
{
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ModMain.MOD_ID);

    // public static final RegistryObject<Item> EXAMPLE_ITEM = ITEMS.register("example", () -> new Item(new Item.Properties().group(ItemGroup.MISC)));
}