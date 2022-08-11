package com.corosus.ltmoveplus.forge;

import com.corosus.ltmoveplus.command.CommandReloadConfig;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

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
                if (!event.player.level.isClientSide()) {


                    //if (lookupPlayerUUIDToCrawlActive.containsKey(event.player.getUniqueID())) {
                    if (lookupPlayerUUIDToCrawlActive_Server.containsKey(event.player.getUUID()) && lookupPlayerUUIDToCrawlActive_Server.get(event.player.getUUID())) {
                        if (event.player.getPose() != Pose.SWIMMING) {
                            event.player.setForcedPose(Pose.SWIMMING);
                        }
                        //System.out.println("forcing crawl for " + event.player);
                        //event.player.setSwimming(true);
                    } else {
                        //dont need to do anything if its off
                        //do in 1.18 with forges handy additions now
                        if (event.player.getPose() != null) {
                            event.player.setForcedPose(null);
                        }
                    }
                } else {

                    if (playerCrawlingClient) {
                        //System.out.println("player crawling");
                        if (ClientTicker.isMainClientPlayer(event.player)) {
                            if (event.player.getPose() != Pose.SWIMMING) {
                                event.player.setForcedPose(Pose.SWIMMING);
                            }
                            //event.player.setSwimming(true);
                        }
                    } else {
                        if (ClientTicker.isMainClientPlayer(event.player)) {
                            if (event.player.getPose() != null) {
                                event.player.setForcedPose(null);
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void setPlayerCrawlStateServer(Player player, boolean isCrawling) {
        lookupPlayerUUIDToCrawlActive_Server.put(player.getUUID(), isCrawling);
    }

    public static void forcePlayerCrawling(Player player) {
        player.setForcedPose(Pose.SWIMMING);
        //player.getEntityData().set(ObfuscationReflectionHelper.getPrivateValue(Entity.class, null, "field_213330_X"), Pose.SWIMMING);
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