package com.syszee.mod;

import com.syszee.mod.common.registry.ModBlocks;
import com.syszee.mod.common.registry.ModEntities;
import com.syszee.mod.common.registry.ModItems;
import com.syszee.mod.datagen.RecipeGen;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ModMain.MOD_ID)
public class ModMain
{
    public static final String MOD_ID = "example";

    public ModMain() 
    {
    	IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::setup);
        bus.addListener(this::clientSetup);
        bus.addListener(this::setupDataGens);

        ModItems.ITEMS.register(bus);
        ModBlocks.BLOCKS.register(bus);
        ModEntities.ENTITIES.register(bus);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(FMLCommonSetupEvent event)
    {
    }

    private void clientSetup(FMLClientSetupEvent event)
    {
    }

    private void setupDataGens(GatherDataEvent event)
    {
        DataGenerator dataGenerator = event.getGenerator();
        dataGenerator.addProvider(new RecipeGen(dataGenerator));
    }
}
