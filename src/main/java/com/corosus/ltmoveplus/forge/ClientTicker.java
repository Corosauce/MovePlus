package com.corosus.ltmoveplus.forge;

import com.corosus.ltmoveplus.config.MovePlusCfgForge;
import com.corosus.ltmoveplus.network.MovePlusNetwork;
import com.corosus.ltmoveplus.network.ToServerPlayerCrawlState;
import com.mojang.math.Vector3d;
import com.mojang.math.Vector3f;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
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

    public static Vec3 prevMotion;

    public static HashMap<KeyMapping, Long> keyTimesLastPressed = new HashMap<>();
    public static HashMap<KeyMapping, Boolean> keyLastState = new HashMap<>();

    //Vector2f used as such: forward speed, right speed
    public static HashMap<KeyMapping, Vector3f> lookupKeyToDirection = new HashMap<>();

    //spectate stuff
    public static boolean keepSpectatingPlayer = false;
    public static String lastPlayerSpectated = "";
    public static long lastTick = 0;
    public static boolean debug = false;
    public static int syncDelay = 5000;
    public static String csvSpecPlayers = "";

    public static HashMap<Class, String> cacheClassToCanonicalName = new HashMap<>();

    public static boolean spectateFixes = false;

    public static void tickInit() {
        lookupKeyToDirection.put(Minecraft.getInstance().options.keyUp, new Vector3f(1, 0, 0));
        lookupKeyToDirection.put(Minecraft.getInstance().options.keyDown, new Vector3f(-1, 0, 0));
        lookupKeyToDirection.put(Minecraft.getInstance().options.keyLeft, new Vector3f(0, -1, 0));
        lookupKeyToDirection.put(Minecraft.getInstance().options.keyRight, new Vector3f(0, 1, 0));
        keyLastState.put(Minecraft.getInstance().options.keyUp, false);
        keyLastState.put(Minecraft.getInstance().options.keyDown, false);
        keyLastState.put(Minecraft.getInstance().options.keyLeft, false);
        keyLastState.put(Minecraft.getInstance().options.keyRight, false);
    }

    public static void tickClientRender() {

        Player player = Minecraft.getInstance().player;
        Entity camera = Minecraft.getInstance().getCameraEntity();

        if (player == null || camera == null) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.screen == null && (!MovePlusCfgForge.GENERAL.dontGroundDodgeIfSneaking.get() || player.getPose() != Pose.CROUCHING)) {
            if (MovePlusCfgForge.GENERAL.useGroundDodge.get()) {
                tickDodging();
            }
        }
    }

    public static void tickClientRenderWorldLast() {

    }

    public static void tickClientGame() {
        Player player = Minecraft.getInstance().player;
        Entity camera = Minecraft.getInstance().getCameraEntity();

        if (player == null || camera == null) return;

        if (needsInit) {
            needsInit = false;
            tickInit();
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.screen == null) {
            if (MovePlusCfgForge.GENERAL.useLedgeClimb.get()) {
                tickLedgeClimb();
            }
            if (MovePlusCfgForge.GENERAL.knockbackResistAmount.get() > 0D) {
                tickKnockbackResistence();
            }
        }

        tickSpectating();
        if (MovePlusCfgForge.GENERAL.useCrawlAnywhere.get()) {
            tickCrawl();
        }
    }

    public static void tickSpectating() {
        Player player = Minecraft.getInstance().player;
        Entity camera = Minecraft.getInstance().getCameraEntity();
        Minecraft mc = Minecraft.getInstance();
        long curTime = System.currentTimeMillis();

        if (player == null || camera == null) return;


        /**
         * This fixes the vanilla bug of the client spectate player not having its position updated when its spectating a player, resulting in unloaded chunks visually
         * - also now tracks players across dimensions
         */
        if (spectateFixes) {
            Entity specEnt = mc.getCameraEntity();
            LocalPlayer clientPlayer = mc.player;

            if (specEnt != null && player != null && player.level != null && player.isSpectator()) {
                if (curTime > lastTick + syncDelay) {

                    //dbg("curTime: " + curTime);
                    //dbg("lastTick: " + lastTick);

                    lastTick = curTime;

                    //updated tracked data
                    if (specEnt != clientPlayer) {
                        lastPlayerSpectated = specEnt.getName().getString();
                        keepSpectatingPlayer = true;

                        //apply the chunk rendering out of range fix
                        dbg("syncing client player to spectator position: " + specEnt);
                        clientPlayer.setPos(specEnt.getX(), specEnt.getY(), specEnt.getZ());
                    }

                    if (keepSpectatingPlayer && !lastPlayerSpectated.equals("")) {
                        boolean ghostPlayer = false;
                        boolean diffDimensionPlayer = false;

                        AbstractClientPlayer foundNonSpectatingPlayerInSameDimension = null;
                        for (AbstractClientPlayer otherPlayer : mc.level.players()) {
                            if (otherPlayer.getName().getString().equals(lastPlayerSpectated)) {
                                foundNonSpectatingPlayerInSameDimension = otherPlayer;
                            }
                        }

                        /**
                         * check and fix diff dimension spectating
                         */

                        ClientPacketListener clientplaynethandler = mc.player.connection;
                        PlayerInfo netInfo = clientplaynethandler.getPlayerInfo(lastPlayerSpectated);
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

                        /*if (diffDimensionPlayer && !isSpectatingASpectator) {
                            clientPlayer.sendChatMessage("/tp " + clientPlayer.getName().getString() + " " + lastPlayerSpectated);
                            clientPlayer.sendChatMessage("/spectate");
                            clientPlayer.sendChatMessage("/spectate " + lastPlayerSpectated);
                        }*/

                        /**
                         * check and fix invalid spectating player
                         */

                        if (foundNonSpectatingPlayerInSameDimension != null && foundNonSpectatingPlayerInSameDimension != specEnt) {
                            ghostPlayer = true;
                        }

                        /*if (ghostPlayer) {
                            clientPlayer.sendChatMessage("/spectate");
                            clientPlayer.sendChatMessage("/spectate " + lastPlayerSpectated);
                        }*/

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
                                AbstractClientPlayer foundNonSpectatingPlayerInSameDimension2 = null;
                                for (AbstractClientPlayer otherPlayer : mc.level.players()) {
                                    if (abort) break;
                                    if (otherPlayer.getName().getString().equals(names[i])) {
                                        foundNonSpectatingPlayerInSameDimension2 = otherPlayer;

                                        if (!lastPlayerSpectated.equals(foundNonSpectatingPlayerInSameDimension2.getName().getString())) {
                                            lastPlayerSpectated = foundNonSpectatingPlayerInSameDimension2.getName().getString();
                                            dbg("spec cycle found: " + names[i] + " in dim to spectate, setting " + lastPlayerSpectated);
                                            /*clientPlayer.sendChatMessage("/spectate");
                                            clientPlayer.sendChatMessage("/spectate " + lastPlayerSpectated);*/
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

                if (clientPlayer.getPose() == Pose.CROUCHING) {
                    keepSpectatingPlayer = false;
                    lastPlayerSpectated = "";
                    dbg("setting keepSpectatingPlayer = false");
                }

            }
        }
    }

    public static void clientChatEvent(ClientChatEvent event) {
        if (!spectateFixes) return;
        dbg("intercepting chat event: " + event.getMessage());
        if (event.getMessage().startsWith("/mp_spec_csv")) {
            dbg("mp_spec_csv firing");
            try {
                String[] args = event.getMessage().split(" ");
                csvSpecPlayers = args[1];
                dbg("csvSpecPlayers set to: " + csvSpecPlayers);
                //Minecraft.getInstance().ingameGUI.addChatMessage(ChatType.GAME_INFO, new StringTextComponent("csvSpecPlayers set to: " + csvSpecPlayers));
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
        Player player = Minecraft.getInstance().player;

        lookupKeyToDirection.forEach((k, v) -> processDodgeKey(k, v));
    }

    public static void processDodgeKey(KeyMapping key, Vector3f vec) {
        long curTime = System.currentTimeMillis();
        long lastTime = getLastKeyTime(key);

        Player player = Minecraft.getInstance().player;

        //key.matchesKey()

        //not mouse check
        //if (key.getKeyCode() > 0) {
            //if (Keyboard.isKeyDown(key.getKeyCode()) && !keyLastState.get(key)) {
            if (key.isDown() && !keyLastState.get(key)) {
                //CULog.dbg(key.getDisplayName() + ": " + (Keyboard.isKeyDown(key.getKeyCode()) ? "pressed" : "not pressed"));
                if (lastTime == -1L) {
                    setLastKeyTime(key, curTime);
                } else {
                    if (player.isOnGround() && lastTime + MovePlusCfgForge.GENERAL.doubleTapDodgeMaxTimeInMilliseconds.get() > curTime) {
                        //CULog.dbg("dodge! " + key.getDisplayName());
                        double forceVertical = MovePlusCfgForge.GENERAL.groundDodgeForceVertical.get();
                        double forceHorizontal = MovePlusCfgForge.GENERAL.groundDodgeForceHorizontal.get();
                        setRelVel(player, vec.y(), (float)forceVertical, vec.x(), (float)forceHorizontal);
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
            if (!key.isDown() && keyLastState.get(key)) {
                for (Map.Entry<KeyMapping, Long> entry : keyTimesLastPressed.entrySet()) {
                    if (entry.getKey() != key) {
                        entry.setValue(-1L);
                    }
                }
            }

            keyLastState.put(key, key.isDown());
            //keyLastState.put(key, Keyboard.isKeyDown(key.getKeyCode()));

        //}
    }

    public static void tickLedgeClimb() {
        Player player = Minecraft.getInstance().player;
        Entity camera = Minecraft.getInstance().getCameraEntity();

        boolean renderDebug = true;

        if (Minecraft.getInstance().options.keySprint.isDown()) {

            float grabDist = 0.75F;
            Vec3 vec1 = player.getLookAngle().scale(grabDist);
            Vec3 vec2 = player.getLookAngle().scale(-0.25F);
            Vector3d lookVec = new Vector3d(vec1.x, vec1.y, vec1.z);
            Vector3d lookVecBehind = new Vector3d(vec2.x, vec2.y, vec2.z);

            //from where they are grabbing along x and z
            //now we will scan from up to down, seeing if theres a spot there hands or feet could be
            //find air spot of min y space 0.25F
            //- then there must be a solid near underneath it, 2 for loop checks

            //start y at base bb + ent height (1.62?) + 0.2 or so
            //expand y bb 0.25

            //double yScanRangeAir = player.height + 0.2D;
            double yScanRangeAir = player.getEyeHeight() + 0.2D;
            double yScanRangeSolid = 0.4D;
            double yScanRes = 0.2D;
            double yAirSize = 0.25D;
            double xzSize = 0.3D;
            double xzSizeBehind = 0.1D;

            AABB playerAABB = player.getBoundingBox();
            AABB spotForHandsAir = new AABB(player.getX() + lookVec.x, playerAABB.minY, player.getZ() + lookVec.z,
                    player.getX() + lookVec.x, playerAABB.minY, player.getZ() + lookVec.z)
                    .expandTowards(xzSize, yAirSize, xzSize);

            AABB behindUnderFeet = new AABB(player.getX() + lookVecBehind.x, playerAABB.minY, player.getZ() + lookVecBehind.z,
                    player.getX() + lookVecBehind.x, playerAABB.minY, player.getZ() + lookVecBehind.z)
                    .inflate(xzSizeBehind, xzSizeBehind, xzSizeBehind);

            if (renderDebug) renderOffsetAABB(behindUnderFeet.move(-player.getX(), -playerAABB.minY, -player.getZ()), 0, 0, 0, 0, 1, 0);

            //initial air finding loop
            boolean foundGrabbableSpot = false;
            //fix it trying to climb while either on the ground or just getting over ledge
            //if (!player.onGround && player.world.getCollisionBoxes(player, behindUnderFeet).size() == 0) {
            if (!player.isOnGround() && player.level.noCollision(player, behindUnderFeet)) {
                for (double y = yScanRangeAir; y > 0.25D && !foundGrabbableSpot; y -= yScanRes) {
                    //if found a good air spot, find a solid spot under it within a tiny range of it
                    //if (player.world.getCollisionBoxes(player, spotForHandsAir.offset(0, y, 0)).size() == 0) {
                    if (player.level.noCollision(player, spotForHandsAir.move(0, y, 0))) {
                        //TEMP
                        //foundGrabbableSpot = true;
                        AABB aabbRenderAir = spotForHandsAir.move(-player.getX(), -playerAABB.minY + y, -player.getZ());
                        if (renderDebug) renderOffsetAABB(aabbRenderAir.inflate(xzSize, 0, xzSize), 0, 0, 0, 0, 0, 1);
                        for (double y2 = 0; y2 < yScanRangeSolid; y2 += yScanRes) {
                            //start the spot half intersecting the air spot and iterate down a bit
                            AABB aabbTry2 = spotForHandsAir.move(0, y - (yAirSize * 1D) - y2, 0);
                            AABB aabbRenderSolid = aabbTry2.move(-player.getX(), -playerAABB.minY, -player.getZ());
                            AABB aabb2 = new AABB(0, 0, 0, 0, 0, 0).inflate(1, 1, 1);
                            //if (player.world.getCollisionBoxes(player, aabbTry2).size() > 0 && aabbTry2.minY + 0.15D > playerAABB.minY) {
                            if (!player.level.noCollision(player, aabbTry2) && aabbTry2.minY + 0.15D > playerAABB.minY) {
                                foundGrabbableSpot = true;
                                if (renderDebug) renderOffsetAABB(aabbRenderSolid.inflate(xzSize, 0, xzSize), 0, 0, 0, 1, 0, 0);
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
                if (player.getDeltaMovement().y < climbSpeed) {
                    Vec3 speed = player.getDeltaMovement();
                    player.setDeltaMovement(speed.x, climbSpeed, speed.z);
                }
            }
        }
    }

    public static void tickKnockbackResistence() {

        Player player = Minecraft.getInstance().player;

        float speed = (float) player.getDeltaMovement().lengthSqr();

        if (player.hurtTime > 0) {

            player.hurtTime = 0;

            if (MovePlusCfgForge.GENERAL.knockbackResistAmount.get() == 1D) {
                player.setDeltaMovement(prevMotion);
                /*player.motionX = prevMotionX;
                player.motionY = prevMotionY;
                player.motionZ = prevMotionZ;*/
            } else {
                player.setDeltaMovement
                        (prevMotion.x + player.getDeltaMovement().x * (1D - Math.min(MovePlusCfgForge.GENERAL.knockbackResistAmount.get(), 1D))
                        , prevMotion.y + player.getDeltaMovement().y > 0.1D ? 0D : (player.getDeltaMovement().y * (1D - Math.min(MovePlusCfgForge.GENERAL.knockbackResistAmount.get(), 1D)))
                        , prevMotion.z + player.getDeltaMovement().x * (1D - Math.min(MovePlusCfgForge.GENERAL.knockbackResistAmount.get(), 1D)));
                /*player.motionX = prevMotionX + (player.motionX * (1D - Math.min(MovePlusCfgForge.GENERAL.knockbackResistAmount, 1D)));
                player.motionY = prevMotionY + (prevMotionY > 0.1D ? 0D : (player.motionY * (1D - Math.min(MovePlusCfgForge.GENERAL.knockbackResistAmount, 1D))));
                player.motionZ = prevMotionZ + (player.motionZ * (1D - Math.min(MovePlusCfgForge.GENERAL.knockbackResistAmount, 1D)));*/
            }
        } else {
            prevMotion = player.getDeltaMovement().scale(1D);
            /*prevMotionX = player.motionX;
            prevMotionY = player.motionY;
            prevMotionZ = player.motionZ;*/
        }
    }

    public static long getLastKeyTime(KeyMapping keybind) {
        if (!keyTimesLastPressed.containsKey(keybind)) {
            keyTimesLastPressed.put(keybind, -1L);
        }
        return keyTimesLastPressed.get(keybind);
    }

    public static void setLastKeyTime(KeyMapping keybind, long time) {
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
        float var7 = entity.yRotO + (entity.getYRot() - entity.yRotO) * var5;
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

        float var9 = Mth.cos(-var7 * 0.01745329F - 3.141593F);
        float var10 = Mth.sin(-var7 * 0.01745329F - 3.141593F);
        float var11 = -Mth.cos(-var6 * 0.01745329F - 0.7853982F);
        //float var12 = Mth.sin(-var6 * 0.01745329F - 0.7853982F);
        float var13 = var9 * var11;
        float var15 = var10 * var11;

        if(rightSpeed == 0.0F && forwardSpeed == 0.0F) {
            AddHorizAndSetVerticalVel(entity, (float)entity.getDeltaMovement().x / 2.0F, y, (float)entity.getDeltaMovement().z / 2.0F);
        } else {
            AddHorizAndSetVerticalVel(entity, var13 * horizontalMultiplier * -1.0F, y, var15 * horizontalMultiplier);
        }
    }

    public static void AddHorizAndSetVerticalVel(Entity entity, float x, float y, float z) {
        entity.setDeltaMovement(entity.getDeltaMovement().x + x, y, entity.getDeltaMovement().z + z);
        /*entity.motionX += (double)x;
        entity.motionY = (double)y;
        entity.motionZ += (double)z;*/
    }

    //TODO: wont work until setTranslation fix
    public static void renderOffsetAABB(AABB bounds, double x, double y, double z, float r, float g, float b)
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

    public static String getCanonicalNameCached(Class clazz) {
        if (!cacheClassToCanonicalName.containsKey(clazz)) {
            cacheClassToCanonicalName.put(clazz, clazz.getCanonicalName());
        }
        return cacheClassToCanonicalName.get(clazz);
    }

    public static void tickCrawl() {
        try {
            Player player = Minecraft.getInstance().player;

            if (player != null) {
                KeyMapping keySneak = Minecraft.getInstance().options.keyShift;
                KeyMapping keySprint = Minecraft.getInstance().options.keySprint;

                //tap sprint while sneaking to lock crawling on, requires actively holding down sneak
                //letting go of sneak stops crawl

                if (keySneak.isDown()) {
                    if (keySprint.isDown()) {
                        if (!isCurrentlyCrawling()) {
                            //System.out.println("set crawling true");
                            setCrawling(true);
                            sendCrawlPacketToServer(true);
                        }
                    }
                } else {
                    //send packet only on client state change
                    if (isCurrentlyCrawling()) {
                        sendCrawlPacketToServer(false);
                    }
                    setCrawling(false);
                }
            }
        } catch (Exception ex) {
            //shhh
        }
    }

    public static boolean isCurrentlyCrawling() {
        return EventHandlerForge.playerCrawlingClient;
    }

    public static void setCrawling(boolean crawling) {
        EventHandlerForge.playerCrawlingClient = crawling;
    }

    public static void sendCrawlPacketToServer(boolean isCrawling) {
        MovePlusNetwork.CHANNEL.sendToServer(new ToServerPlayerCrawlState(isCrawling));
        //System.out.println("sendCrawlPacketToServer: " + isCrawling);
    }

    public static boolean isMainClientPlayer(Player player) {
        if (Minecraft.getInstance().player == null) return false;
        return player.getUUID() == Minecraft.getInstance().player.getUUID();
    }

}


