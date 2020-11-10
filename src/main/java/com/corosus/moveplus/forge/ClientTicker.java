package com.corosus.moveplus.forge;

import com.corosus.moveplus.config.MovePlusCfgForge;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;

import java.util.HashMap;
import java.util.Map;

public class ClientTicker {

    public static boolean needsInit = true;

    /*public static double prevMotionX;
    public static double prevMotionY;
    public static double prevMotionZ;*/

    public static Vec3d prevMotion;

    public static HashMap<KeyBinding, Long> keyTimesLastPressed = new HashMap<>();
    public static HashMap<KeyBinding, Boolean> keyLastState = new HashMap<>();

    //Vec2f used as such: forward speed, right speed
    public static HashMap<KeyBinding, Vec2f> lookupKeyToDirection = new HashMap<>();

    //spectate stuff
    public static boolean keepSpectatingPlayer = false;
    public static String lastPlayerSpectated = "";
    public static long lastTick = 0;
    public static boolean debug = true;
    public static int syncDelay = 5000;
    public static String csvSpecPlayers = "";

    public static void tickInit() {
        lookupKeyToDirection.put(Minecraft.getInstance().gameSettings.keyBindForward, new Vec2f(1, 0));
        lookupKeyToDirection.put(Minecraft.getInstance().gameSettings.keyBindBack, new Vec2f(-1, 0));
        lookupKeyToDirection.put(Minecraft.getInstance().gameSettings.keyBindLeft, new Vec2f(0, -1));
        lookupKeyToDirection.put(Minecraft.getInstance().gameSettings.keyBindRight, new Vec2f(0, 1));
        keyLastState.put(Minecraft.getInstance().gameSettings.keyBindForward, false);
        keyLastState.put(Minecraft.getInstance().gameSettings.keyBindBack, false);
        keyLastState.put(Minecraft.getInstance().gameSettings.keyBindLeft, false);
        keyLastState.put(Minecraft.getInstance().gameSettings.keyBindRight, false);
    }

    public static void tickClientRenderScreen() {

        PlayerEntity player = Minecraft.getInstance().player;
        Entity camera = Minecraft.getInstance().getRenderViewEntity();

        if (player == null || camera == null) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.currentScreen == null && (!MovePlusCfgForge.GENERAL.dontGroundDodgeIfSneaking.get() || !player.isSneaking())) {
            if (MovePlusCfgForge.GENERAL.useGroundDodge.get()) {
                tickDodging();
            }
        }
    }

    public static void tickClientRenderWorldLast() {

    }

    public static void tickClientGame() {
        PlayerEntity player = Minecraft.getInstance().player;
        Entity camera = Minecraft.getInstance().getRenderViewEntity();

        if (player == null || camera == null) return;

        if (needsInit) {
            needsInit = false;
            tickInit();
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.currentScreen == null) {
            if (MovePlusCfgForge.GENERAL.useLedgeClimb.get()) {
                tickLedgeClimb();
            }
            if (MovePlusCfgForge.GENERAL.knockbackResistAmount.get() > 0D) {
                tickKnockbackResistence();
            }
        }

        tickSpectating();
    }

    public static void tickSpectating() {
        PlayerEntity player = Minecraft.getInstance().player;
        Entity camera = Minecraft.getInstance().getRenderViewEntity();
        Minecraft mc = Minecraft.getInstance();
        long curTime = System.currentTimeMillis();

        if (player == null || camera == null) return;


        /**
         * This fixes the vanilla bug of the client spectate player not having its position updated when its spectating a player, resulting in unloaded chunks visually
         * - also now tracks players across dimensions
         */
        boolean spectateFix = true;
        if (spectateFix) {
            Entity specEnt = mc.getRenderViewEntity();
            ClientPlayerEntity clientPlayer = mc.player;

            if (specEnt != null && player != null && player.world != null && player.isSpectator()) {
                if (curTime > lastTick + syncDelay) {

                    //dbg("curTime: " + curTime);
                    //dbg("lastTick: " + lastTick);

                    lastTick = curTime;

                    //updated tracked data
                    if (specEnt != clientPlayer) {
                        lastPlayerSpectated = specEnt.getName().getFormattedText();
                        keepSpectatingPlayer = true;

                        //apply the chunk rendering out of range fix
                        dbg("syncing client player to spectator position: " + specEnt);
                        clientPlayer.setPosition(specEnt.getPosX(), specEnt.getPosY(), specEnt.getPosZ());
                    }

                    if (keepSpectatingPlayer && !lastPlayerSpectated.equals("")) {
                        boolean ghostPlayer = false;
                        boolean diffDimensionPlayer = false;

                        AbstractClientPlayerEntity foundNonSpectatingPlayerInSameDimension = null;
                        for (AbstractClientPlayerEntity otherPlayer : mc.world.getPlayers()) {
                            if (otherPlayer.getName().getFormattedText().equals(lastPlayerSpectated)) {
                                foundNonSpectatingPlayerInSameDimension = otherPlayer;
                            }
                        }

                        /**
                         * check and fix diff dimension spectating
                         */

                        ClientPlayNetHandler clientplaynethandler = mc.player.connection;
                        NetworkPlayerInfo netInfo = clientplaynethandler.getPlayerInfo(lastPlayerSpectated);
                        if (netInfo == null) {
                            //player disconnected
                            dbg("detected target player not on server");
                        } else {
                            if (foundNonSpectatingPlayerInSameDimension == null) {
                                dbg("detected player in diff dimension");
                                diffDimensionPlayer = true;
                            }
                        }

                        //edge case, mc.world.getPlayers() doesnt have spectators in it, so spectating a spectator breaks above logic
                        boolean isSpectatingASpectator = false;
                        if (specEnt != clientPlayer && specEnt.isSpectator()) {
                            isSpectatingASpectator = true;
                        }

                        if (diffDimensionPlayer && !isSpectatingASpectator) {
                            clientPlayer.sendChatMessage("/tp " + clientPlayer.getName().getFormattedText() + " " + lastPlayerSpectated);
                            clientPlayer.sendChatMessage("/spectate");
                            clientPlayer.sendChatMessage("/spectate " + lastPlayerSpectated);
                        }

                        /**
                         * check and fix invalid spectating player
                         */

                        if (foundNonSpectatingPlayerInSameDimension != null && foundNonSpectatingPlayerInSameDimension != specEnt) {
                            ghostPlayer = true;
                        }

                        if (ghostPlayer) {
                            clientPlayer.sendChatMessage("/spectate");
                            clientPlayer.sendChatMessage("/spectate " + lastPlayerSpectated);
                        }

                        boolean noPlayerToTrack = foundNonSpectatingPlayerInSameDimension == null && !diffDimensionPlayer;

                        boolean usePlayerList = true;
                        if (usePlayerList/* && noPlayerToTrack*/ && !csvSpecPlayers.equals("") && !csvSpecPlayers.equals("OFF")) {

                            dbg("spec cycle starting: " + csvSpecPlayers);

                            String[] names = csvSpecPlayers.split(",");
                            boolean abort = false;
                            for (int i = 0; i < names.length; i++) {
                                if (abort) break;
                                names[i] = names[i].trim();

                                dbg("spec cycle considering: " + names[i]);

                                //lets only look for players in same dimension, since we cant lookup cross dimension players without teleporting everywhere
                                AbstractClientPlayerEntity foundNonSpectatingPlayerInSameDimension2 = null;
                                for (AbstractClientPlayerEntity otherPlayer : mc.world.getPlayers()) {
                                    if (abort) break;
                                    if (otherPlayer.getName().getFormattedText().equals(names[i])) {
                                        foundNonSpectatingPlayerInSameDimension2 = otherPlayer;

                                        if (!lastPlayerSpectated.equals(foundNonSpectatingPlayerInSameDimension2.getName().getFormattedText())) {
                                            lastPlayerSpectated = foundNonSpectatingPlayerInSameDimension2.getName().getFormattedText();
                                            dbg("spec cycle found: " + names[i] + " in dim to spectate, setting " + lastPlayerSpectated);
                                            clientPlayer.sendChatMessage("/spectate");
                                            clientPlayer.sendChatMessage("/spectate " + lastPlayerSpectated);
                                            abort = true;
                                        } else {
                                            //found self, its a match just dont do anything and abort
                                            abort = true;
                                        }
                                    }
                                }


                            }


                        }
                    }
                }

                if (clientPlayer.isSneaking()) {
                    keepSpectatingPlayer = false;
                    lastPlayerSpectated = "";
                    dbg("setting keepSpectatingPlayer = false");
                }

            }
        }
    }

    public static void clientChatEvent(ClientChatEvent event) {
        dbg("intercepting chat event: " + event.getMessage());
        if (event.getMessage().startsWith("/mp_spec_csv")) {
            dbg("mp_spec_csv firing");
            try {
                String[] args = event.getMessage().split(" ");
                csvSpecPlayers = args[1];
                dbg("csvSpecPlayers set to: " + csvSpecPlayers);
                Minecraft.getInstance().ingameGUI.addChatMessage(ChatType.GAME_INFO, new StringTextComponent("csvSpecPlayers set to: " + csvSpecPlayers));
                event.setCanceled(true);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void dbg(Object obj) {
        if (debug) {
            System.out.println(obj);
        }
    }

    public static void tickDodging() {
        PlayerEntity player = Minecraft.getInstance().player;

        lookupKeyToDirection.forEach((k, v) -> processDodgeKey(k, v));
    }

    public static void processDodgeKey(KeyBinding key, Vec2f vec) {
        long curTime = System.currentTimeMillis();
        long lastTime = getLastKeyTime(key);

        PlayerEntity player = Minecraft.getInstance().player;

        //key.matchesKey()

        //not mouse check
        //if (key.getKeyCode() > 0) {
            //if (Keyboard.isKeyDown(key.getKeyCode()) && !keyLastState.get(key)) {
            if (key.isKeyDown() && !keyLastState.get(key)) {
                //CULog.dbg(key.getDisplayName() + ": " + (Keyboard.isKeyDown(key.getKeyCode()) ? "pressed" : "not pressed"));
                if (lastTime == -1L) {
                    setLastKeyTime(key, curTime);
                } else {
                    if (player.onGround && lastTime + MovePlusCfgForge.GENERAL.doubleTapDodgeMaxTimeInMilliseconds.get() > curTime) {
                        //CULog.dbg("dodge! " + key.getDisplayName());
                        double forceVertical = MovePlusCfgForge.GENERAL.groundDodgeForceVertical.get();
                        double forceHorizontal = MovePlusCfgForge.GENERAL.groundDodgeForceHorizontal.get();
                        setRelVel(player, vec.y, (float)forceVertical, vec.x, (float)forceHorizontal);
                        setLastKeyTime(key, -1L);


                        MovePlus.LOGGER.info(":|");
                        //ConfigTracker.INSTANCE.loadDefaultServerConfigs();
                        ConfigTracker.INSTANCE.loadConfigs(ModConfig.Type.CLIENT, FMLPaths.CONFIGDIR.get());
                    } else {
                        setLastKeyTime(key, curTime);
                    }
                }
            }

            //prevent double tapping trigger between tapping other keys
            //check last state was unpressed so we dont cancel out actively held down keys
            //if (!Keyboard.isKeyDown(key.getKeyCode()) && keyLastState.get(key)) {
            if (!key.isKeyDown() && keyLastState.get(key)) {
                for (Map.Entry<KeyBinding, Long> entry : keyTimesLastPressed.entrySet()) {
                    if (entry.getKey() != key) {
                        entry.setValue(-1L);
                    }
                }
            }

            keyLastState.put(key, key.isKeyDown());
            //keyLastState.put(key, Keyboard.isKeyDown(key.getKeyCode()));

        //}
    }

    public static void tickLedgeClimb() {
        PlayerEntity player = Minecraft.getInstance().player;
        Entity camera = Minecraft.getInstance().getRenderViewEntity();

        boolean renderDebug = true;

        if (Minecraft.getInstance().gameSettings.keyBindSprint.isKeyDown()/*Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)*/) {

            float grabDist = 0.75F;
            Vec3d lookVec = player.getLookVec().scale(grabDist);
            Vec3d lookVecBehind = player.getLookVec().scale(-0.25F);

            //from where they are grabbing along x and z
            //now we will scan from up to down, seeing if theres a spot there hands or feet could be
            //find air spot of min y space 0.25F
            //- then there must be a solid near underneath it, 2 for loop checks

            //start y at base bb + ent height (1.62?) + 0.2 or so
            //expand y bb 0.25

            //double yScanRangeAir = player.height + 0.2D;
            double yScanRangeAir = player.getHeight() + 0.2D;
            double yScanRangeSolid = 0.4D;
            double yScanRes = 0.2D;
            double yAirSize = 0.25D;
            double xzSize = 0.3D;
            double xzSizeBehind = 0.1D;

            AxisAlignedBB playerAABB = player.getBoundingBox();
            AxisAlignedBB spotForHandsAir = new AxisAlignedBB(player.getPosX() + lookVec.x, playerAABB.minY, player.getPosZ() + lookVec.z,
                    player.getPosX() + lookVec.x, playerAABB.minY, player.getPosZ() + lookVec.z)
                    .grow(xzSize, yAirSize, xzSize);

            AxisAlignedBB behindUnderFeet = new AxisAlignedBB(player.getPosX() + lookVecBehind.x, playerAABB.minY, player.getPosZ() + lookVecBehind.z,
                    player.getPosX() + lookVecBehind.x, playerAABB.minY, player.getPosZ() + lookVecBehind.z)
                    .grow(xzSizeBehind, xzSizeBehind, xzSizeBehind);

            if (renderDebug) renderOffsetAABB(behindUnderFeet.offset(-player.getPosX(), -playerAABB.minY, -player.getPosZ()), 0, 0, 0, 0, 1, 0);

            //initial air finding loop
            boolean foundGrabbableSpot = false;
            //fix it trying to climb while either on the ground or just getting over ledge
            //if (!player.onGround && player.world.getCollisionBoxes(player, behindUnderFeet).size() == 0) {
            if (!player.onGround && player.world.hasNoCollisions(player, behindUnderFeet)) {
                for (double y = yScanRangeAir; y > 0.25D && !foundGrabbableSpot; y -= yScanRes) {
                    //if found a good air spot, find a solid spot under it within a tiny range of it
                    //if (player.world.getCollisionBoxes(player, spotForHandsAir.offset(0, y, 0)).size() == 0) {
                    if (player.world.hasNoCollisions(player, spotForHandsAir.offset(0, y, 0))) {
                        //TEMP
                        //foundGrabbableSpot = true;
                        AxisAlignedBB aabbRenderAir = spotForHandsAir.offset(-player.getPosX(), -playerAABB.minY + y, -player.getPosZ());
                        if (renderDebug) renderOffsetAABB(aabbRenderAir.grow(xzSize, 0, xzSize), 0, 0, 0, 0, 0, 1);
                        for (double y2 = 0; y2 < yScanRangeSolid; y2 += yScanRes) {
                            //start the spot half intersecting the air spot and iterate down a bit
                            AxisAlignedBB aabbTry2 = spotForHandsAir.offset(0, y - (yAirSize * 1D) - y2, 0);
                            AxisAlignedBB aabbRenderSolid = aabbTry2.offset(-player.getPosX(), -playerAABB.minY, -player.getPosZ());
                            AxisAlignedBB aabb2 = new AxisAlignedBB(0, 0, 0, 0, 0, 0).grow(1, 1, 1);
                            //if (player.world.getCollisionBoxes(player, aabbTry2).size() > 0 && aabbTry2.minY + 0.15D > playerAABB.minY) {
                            if (!player.world.hasNoCollisions(player, aabbTry2) && aabbTry2.minY + 0.15D > playerAABB.minY) {
                                foundGrabbableSpot = true;
                                if (renderDebug) renderOffsetAABB(aabbRenderSolid.grow(xzSize, 0, xzSize), 0, 0, 0, 1, 0, 0);
                                //Render.renderOffsetAABB(aabbRender, -camera.getPosX(), -camera.getPosY(), -camera.getPosZ());
                                //Render.renderOffsetAABB(aabb2, 0, 0, 0);
                                break;
                            }
                        }
                    }
                }
            }

            if (foundGrabbableSpot/*nearWall(player)*/) {
                float climbSpeed = 0.08F;
                if (player.getMotion().y < climbSpeed) {
                    Vec3d speed = player.getMotion();
                    player.setMotion(speed.x, climbSpeed, speed.z);
                }
            }
        }
    }

    public static void tickKnockbackResistence() {

        PlayerEntity player = Minecraft.getInstance().player;

        float speed = (float) player.getMotion().lengthSquared();

        if (player.hurtTime > 0) {

            player.hurtTime = 0;

            if (MovePlusCfgForge.GENERAL.knockbackResistAmount.get() == 1D) {
                player.setMotion(prevMotion);
                /*player.motionX = prevMotionX;
                player.motionY = prevMotionY;
                player.motionZ = prevMotionZ;*/
            } else {
                player.setMotion
                        (prevMotion.x + player.getMotion().x * (1D - Math.min(MovePlusCfgForge.GENERAL.knockbackResistAmount.get(), 1D))
                        , prevMotion.y + player.getMotion().y > 0.1D ? 0D : (player.getMotion().y * (1D - Math.min(MovePlusCfgForge.GENERAL.knockbackResistAmount.get(), 1D)))
                        , prevMotion.z + player.getMotion().x * (1D - Math.min(MovePlusCfgForge.GENERAL.knockbackResistAmount.get(), 1D)));
                /*player.motionX = prevMotionX + (player.motionX * (1D - Math.min(MovePlusCfgForge.GENERAL.knockbackResistAmount, 1D)));
                player.motionY = prevMotionY + (prevMotionY > 0.1D ? 0D : (player.motionY * (1D - Math.min(MovePlusCfgForge.GENERAL.knockbackResistAmount, 1D))));
                player.motionZ = prevMotionZ + (player.motionZ * (1D - Math.min(MovePlusCfgForge.GENERAL.knockbackResistAmount, 1D)));*/
            }
        } else {
            prevMotion = player.getMotion().scale(1D);
            /*prevMotionX = player.motionX;
            prevMotionY = player.motionY;
            prevMotionZ = player.motionZ;*/
        }
    }

    public static long getLastKeyTime(KeyBinding keybind) {
        if (!keyTimesLastPressed.containsKey(keybind)) {
            keyTimesLastPressed.put(keybind, -1L);
        }
        return keyTimesLastPressed.get(keybind);
    }

    public static void setLastKeyTime(KeyBinding keybind, long time) {
        keyTimesLastPressed.put(keybind, time);
    }

    /**
     * taken from old moveplus, a bit messy
     *
     * setting rightSpeed to -1 means left
     * setting forwardSpeed to -1 means back
     *
     * @param entity
     * @param rightSpeed
     * @param y
     * @param forwardSpeed
     * @param horizontalMultiplier
     */
    public static void setRelVel(Entity entity, float rightSpeed, float y, float forwardSpeed, float horizontalMultiplier) {
        float var5 = 10.0F;
        float var6 = 0.0F;
        float var7 = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * var5;
        int var8 = (int)Math.floor((double)(var7 / 360.0F) + 0.5D);
        var7 = var7 - (float)var8 * 360.0F + 270.0F;

        if(forwardSpeed <= 0.0F && forwardSpeed < 0.0F) {
            var7 += 180.0F;
        }

        if(rightSpeed > 0.0F) {
            var7 += 90.0F - forwardSpeed * 10.0F;
        } else if(rightSpeed < 0.0F) {
            var7 += 270.0F + forwardSpeed * 10.0F;
        }

        float var9 = MathHelper.cos(-var7 * 0.01745329F - 3.141593F);
        float var10 = MathHelper.sin(-var7 * 0.01745329F - 3.141593F);
        float var11 = -MathHelper.cos(-var6 * 0.01745329F - 0.7853982F);
        //float var12 = MathHelper.sin(-var6 * 0.01745329F - 0.7853982F);
        float var13 = var9 * var11;
        float var15 = var10 * var11;

        if(rightSpeed == 0.0F && forwardSpeed == 0.0F) {
            AddHorizAndSetVerticalVel(entity, (float)entity.getMotion().x / 2.0F, y, (float)entity.getMotion().z / 2.0F);
        } else {
            AddHorizAndSetVerticalVel(entity, var13 * horizontalMultiplier * -1.0F, y, var15 * horizontalMultiplier);
        }
    }

    public static void AddHorizAndSetVerticalVel(Entity entity, float x, float y, float z) {
        entity.setMotion(new Vec3d(entity.getMotion().x + x, y, entity.getMotion().z + z));
        /*entity.motionX += (double)x;
        entity.motionY = (double)y;
        entity.motionZ += (double)z;*/
    }

    //TODO: wont work until setTranslation fix
    public static void renderOffsetAABB(AxisAlignedBB bounds, double x, double y, double z, float r, float g, float b)
    {

        //TODO: 1.15
        /*GlStateManager.disableTexture();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.color4f(r, g, b, 1.0F);
        //TODO: 1.15 replacement
        bufferbuilder.setTranslation(x, y, z);
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
        bufferbuilder.pos(bounds.minX, bounds.maxY, bounds.minZ).normal(0.0F, 0.0F, -1.0F).endVertex();
        bufferbuilder.pos(bounds.maxX, bounds.maxY, bounds.minZ).normal(0.0F, 0.0F, -1.0F).endVertex();
        bufferbuilder.pos(bounds.maxX, bounds.minY, bounds.minZ).normal(0.0F, 0.0F, -1.0F).endVertex();
        bufferbuilder.pos(bounds.minX, bounds.minY, bounds.minZ).normal(0.0F, 0.0F, -1.0F).endVertex();
        bufferbuilder.pos(bounds.minX, bounds.minY, bounds.maxZ).normal(0.0F, 0.0F, 1.0F).endVertex();
        bufferbuilder.pos(bounds.maxX, bounds.minY, bounds.maxZ).normal(0.0F, 0.0F, 1.0F).endVertex();
        bufferbuilder.pos(bounds.maxX, bounds.maxY, bounds.maxZ).normal(0.0F, 0.0F, 1.0F).endVertex();
        bufferbuilder.pos(bounds.minX, bounds.maxY, bounds.maxZ).normal(0.0F, 0.0F, 1.0F).endVertex();
        bufferbuilder.pos(bounds.minX, bounds.minY, bounds.minZ).normal(0.0F, -1.0F, 0.0F).endVertex();
        bufferbuilder.pos(bounds.maxX, bounds.minY, bounds.minZ).normal(0.0F, -1.0F, 0.0F).endVertex();
        bufferbuilder.pos(bounds.maxX, bounds.minY, bounds.maxZ).normal(0.0F, -1.0F, 0.0F).endVertex();
        bufferbuilder.pos(bounds.minX, bounds.minY, bounds.maxZ).normal(0.0F, -1.0F, 0.0F).endVertex();
        bufferbuilder.pos(bounds.minX, bounds.maxY, bounds.maxZ).normal(0.0F, 1.0F, 0.0F).endVertex();
        bufferbuilder.pos(bounds.maxX, bounds.maxY, bounds.maxZ).normal(0.0F, 1.0F, 0.0F).endVertex();
        bufferbuilder.pos(bounds.maxX, bounds.maxY, bounds.minZ).normal(0.0F, 1.0F, 0.0F).endVertex();
        bufferbuilder.pos(bounds.minX, bounds.maxY, bounds.minZ).normal(0.0F, 1.0F, 0.0F).endVertex();
        bufferbuilder.pos(bounds.minX, bounds.minY, bounds.maxZ).normal(-1.0F, 0.0F, 0.0F).endVertex();
        bufferbuilder.pos(bounds.minX, bounds.maxY, bounds.maxZ).normal(-1.0F, 0.0F, 0.0F).endVertex();
        bufferbuilder.pos(bounds.minX, bounds.maxY, bounds.minZ).normal(-1.0F, 0.0F, 0.0F).endVertex();
        bufferbuilder.pos(bounds.minX, bounds.minY, bounds.minZ).normal(-1.0F, 0.0F, 0.0F).endVertex();
        bufferbuilder.pos(bounds.maxX, bounds.minY, bounds.minZ).normal(1.0F, 0.0F, 0.0F).endVertex();
        bufferbuilder.pos(bounds.maxX, bounds.maxY, bounds.minZ).normal(1.0F, 0.0F, 0.0F).endVertex();
        bufferbuilder.pos(bounds.maxX, bounds.maxY, bounds.maxZ).normal(1.0F, 0.0F, 0.0F).endVertex();
        bufferbuilder.pos(bounds.maxX, bounds.minY, bounds.maxZ).normal(1.0F, 0.0F, 0.0F).endVertex();
        tessellator.draw();
        bufferbuilder.setTranslation(0.0D, 0.0D, 0.0D);
        GlStateManager.enableTexture();*/
    }

}


