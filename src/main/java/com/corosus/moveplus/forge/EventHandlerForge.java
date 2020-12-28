package com.corosus.moveplus.forge;

import com.corosus.moveplus.command.CommandReloadConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;

import java.awt.*;
import java.util.HashMap;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = MovePlus.MODID)
public class EventHandlerForge {

    public static HashMap<UUID, Boolean> lookupPlayerUUIDToCrawlActive_Server = new HashMap<>();
    public static boolean playerCrawlingClient = false;

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent(priority = EventPriority.LOW)
    public void tickClient(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            ClientTicker.tickClientGame();
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent(priority = EventPriority.LOW)
    public void tickClient(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            ClientTicker.tickClientRender();
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void playerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            try {
                if (!event.player.world.isRemote) {


                    //if (lookupPlayerUUIDToCrawlActive.containsKey(event.player.getUniqueID())) {
                    if (lookupPlayerUUIDToCrawlActive_Server.containsKey(event.player.getUniqueID()) && lookupPlayerUUIDToCrawlActive_Server.get(event.player.getUniqueID())) {
                        forcePlayerCrawling(event.player);
                        System.out.println("forcing crawl for " + event.player);
                        //event.player.setSwimming(true);
                    } else {
                        //dont need to do anything if its off
                    }
                } else {

                    if (playerCrawlingClient) {
                        //System.out.println("player crawling");
                        if (ClientTicker.isMainClientPlayer(event.player)) {
                            forcePlayerCrawling(event.player);
                            //event.player.setSwimming(true);
                        }

                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void setPlayerCrawlStateServer(PlayerEntity player, boolean isCrawling) {
        lookupPlayerUUIDToCrawlActive_Server.put(player.getUniqueID(), isCrawling);
    }

    public static void forcePlayerCrawling(PlayerEntity player) {
        player.getDataManager().set(ObfuscationReflectionHelper.getPrivateValue(Entity.class, null, "field_213330_X"), Pose.SWIMMING);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent(priority = EventPriority.LOW)
    public void tickClient(ClientChatEvent event) {
        ClientTicker.clientChatEvent(event);
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void clientChat(ClientChatEvent event) {
        String msg = event.getMessage();

        if (msg.equals("/" + CommandReloadConfig.getCommandName() + " client")) {
            ConfigTracker.INSTANCE.loadConfigs(ModConfig.Type.CLIENT, FMLPaths.CONFIGDIR.get());
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void drawScreen(RenderGameOverlayEvent.Pre event) {
        ClientTicker.tickClientRenderScreen(event);
    }


}