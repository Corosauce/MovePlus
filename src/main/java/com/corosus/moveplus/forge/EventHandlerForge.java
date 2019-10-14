package com.corosus.moveplus.forge;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MovePlus.MODID)
public class EventHandlerForge {

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
            ClientTicker.tickClientRenderScreen();
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void playerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            /**TODO: implement this
             * 1. make move plus runnable server side
             * 2. listen for crouch+sprint keys to enable crawl mode
             * 3. packet system to tell server a player is in crawl mode
             * 4. when in crawl mode, run this code on both client and server side
             *    - event.player.getDataManager().set(ObfuscationReflectionHelper.getPrivateValue(Entity.class, null, "field_213330_X"), Pose.SWIMMING);
             * 5. decide on an intuitive way to end crawl mode (pressing same combo again? jumping? crouch key? sprint key? all or any of them?)
             */
        }
    }


}