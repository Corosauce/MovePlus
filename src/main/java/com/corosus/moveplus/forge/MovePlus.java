package com.corosus.moveplus.forge;

import com.corosus.chestorganizer.input.Keybinds;
import com.corosus.moveplus.config.MovePlusCfgForge;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


// The value here should match an entry in the META-INF/mods.toml file
@Mod(MovePlus.MODID)
public class MovePlus
{
    public static final Logger LOGGER = LogManager.getLogger();

    public static final String MODID = "moveplus";

    public MovePlus() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);

        //TODO: off for 1.15
        /*FMLJavaModLoadingContext.get().getModEventBus().addListener(MovePlusCfgForge::onLoad);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(MovePlusCfgForge::onFileChange);*/

        MinecraftForge.EVENT_BUS.register(new EventHandlerForge());

        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, MovePlusCfgForge.CLIENT_CONFIG);

        ClientRegistry.registerKeyBinding(Keybinds.sort);
        ClientRegistry.registerKeyBinding(Keybinds.cycle);

        //TODO: off for 1.15
        /*eventBus.addListener(MovePlusCfgForge::onLoad);
        eventBus.addListener(MovePlusCfgForge::onFileChange);*/
    }

    private void setup(final FMLCommonSetupEvent event)
    {

    }

    private void doClientStuff(final FMLClientSetupEvent event) {

    }
}
