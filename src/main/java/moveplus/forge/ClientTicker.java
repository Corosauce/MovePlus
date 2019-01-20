package moveplus.forge;

import CoroUtil.forge.CULog;
import moveplus.config.MovePlusCfg;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.input.Keyboard;

import java.util.HashMap;

public class ClientTicker {

    public static boolean needsInit = true;

    public static double prevMotionX;
    public static double prevMotionY;
    public static double prevMotionZ;

    public static HashMap<KeyBinding, Long> keyTimesLastPressed = new HashMap<>();
    public static HashMap<KeyBinding, Boolean> keyLastState = new HashMap<>();

    //Vec2f used as such: forward speed, right speed
    public static HashMap<KeyBinding, Vec2f> lookupKeyToDirection = new HashMap<>();

    public static void tickInit() {
        lookupKeyToDirection.put(Minecraft.getMinecraft().gameSettings.keyBindForward, new Vec2f(1, 0));
        lookupKeyToDirection.put(Minecraft.getMinecraft().gameSettings.keyBindBack, new Vec2f(-1, 0));
        lookupKeyToDirection.put(Minecraft.getMinecraft().gameSettings.keyBindLeft, new Vec2f(0, -1));
        lookupKeyToDirection.put(Minecraft.getMinecraft().gameSettings.keyBindRight, new Vec2f(0, 1));
        keyLastState.put(Minecraft.getMinecraft().gameSettings.keyBindForward, false);
        keyLastState.put(Minecraft.getMinecraft().gameSettings.keyBindBack, false);
        keyLastState.put(Minecraft.getMinecraft().gameSettings.keyBindLeft, false);
        keyLastState.put(Minecraft.getMinecraft().gameSettings.keyBindRight, false);
    }

    public static void tickClientGame() {
        EntityPlayer player = Minecraft.getMinecraft().player;
        Entity camera = Minecraft.getMinecraft().getRenderViewEntity();

        if (player == null || camera == null) return;

        if (needsInit) {
            needsInit = false;
            tickInit();
        }

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.currentScreen == null) {
            if (MovePlusCfg.useLedgeClimb) {
                tickLedgeClimb();
            }
            if (MovePlusCfg.knockbackResistAmount > 0D) {
                tickKnockbackResistence();
            }
            if (MovePlusCfg.useGroundDodge) {
                tickDodging();
            }
        }
    }

    public static void tickDodging() {
        EntityPlayer player = Minecraft.getMinecraft().player;

        lookupKeyToDirection.forEach((k, v) -> processDodgeKey(k, v));
    }

    public static void processDodgeKey(KeyBinding key, Vec2f vec) {
        long curTime = System.currentTimeMillis();
        long lastTime = getLastKeyTime(key);

        EntityPlayer player = Minecraft.getMinecraft().player;

        if (key.getKeyCode() > 0) {
            if (Keyboard.isKeyDown(key.getKeyCode()) && !keyLastState.get(key)) {
                //CULog.dbg(key.getDisplayName() + ": " + (Keyboard.isKeyDown(key.getKeyCode()) ? "pressed" : "not pressed"));
                if (lastTime == -1L) {
                    setLastKeyTime(key, curTime);
                } else {
                    if (player.onGround && lastTime + MovePlusCfg.dodgeDelay > curTime) {
                        CULog.dbg("dodge! " + key.getDisplayName());
                        setRelVel(player, vec.y, 0.4F, vec.x, 1F);
                        setLastKeyTime(key, -1L);
                    } else {
                        setLastKeyTime(key, curTime);
                    }
                }
            }

            keyLastState.put(key, Keyboard.isKeyDown(key.getKeyCode()));

        }
    }

    public static void tickLedgeClimb() {
        EntityPlayer player = Minecraft.getMinecraft().player;
        Entity camera = Minecraft.getMinecraft().getRenderViewEntity();

        boolean renderDebug = false;

        if (Minecraft.getMinecraft().gameSettings.keyBindSprint.isKeyDown()/*Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)*/) {

            float grabDist = 0.75F;
            Vec3d lookVec = player.getLookVec().scale(grabDist);
            Vec3d lookVecBehind = player.getLookVec().scale(-0.25F);

            //from where they are grabbing along x and z
            //now we will scan from up to down, seeing if theres a spot there hands or feet could be
            //find air spot of min y space 0.25F
            //- then there must be a solid near underneath it, 2 for loop checks

            //start y at base bb + ent height (1.62?) + 0.2 or so
            //expand y bb 0.25

            double yScanRangeAir = player.height + 0.2D;
            double yScanRangeSolid = 0.4D;
            double yScanRes = 0.2D;
            double yAirSize = 0.25D;
            double xzSize = 0.3D;
            double xzSizeBehind = 0.1D;

            AxisAlignedBB playerAABB = player.getEntityBoundingBox();
            AxisAlignedBB spotForHandsAir = new AxisAlignedBB(player.posX + lookVec.x, playerAABB.minY, player.posZ + lookVec.z,
                    player.posX + lookVec.x, playerAABB.minY, player.posZ + lookVec.z)
                    .grow(xzSize, yAirSize, xzSize);

            AxisAlignedBB behindUnderFeet = new AxisAlignedBB(player.posX + lookVecBehind.x, playerAABB.minY, player.posZ + lookVecBehind.z,
                    player.posX + lookVecBehind.x, playerAABB.minY, player.posZ + lookVecBehind.z)
                    .grow(xzSizeBehind, xzSizeBehind, xzSizeBehind);

            if (renderDebug) renderOffsetAABB(behindUnderFeet.offset(-player.posX, -playerAABB.minY, -player.posZ), 0, 0, 0, 0, 1, 0);

            //initial air finding loop
            boolean foundGrabbableSpot = false;
            //fix it trying to climb while either on the ground or just getting over ledge
            if (!player.onGround && player.world.getCollisionBoxes(player, behindUnderFeet).size() == 0) {
                for (double y = yScanRangeAir; y > 0.25D && !foundGrabbableSpot; y -= yScanRes) {
                    //if found a good air spot, find a solid spot under it within a tiny range of it
                    if (player.world.getCollisionBoxes(player, spotForHandsAir.offset(0, y, 0)).size() == 0) {
                        //TEMP
                        //foundGrabbableSpot = true;
                        AxisAlignedBB aabbRenderAir = spotForHandsAir.offset(-player.posX, -playerAABB.minY + y, -player.posZ);
                        if (renderDebug) renderOffsetAABB(aabbRenderAir.grow(xzSize, 0, xzSize), 0, 0, 0, 0, 0, 1);
                        for (double y2 = 0; y2 < yScanRangeSolid; y2 += yScanRes) {
                            //start the spot half intersecting the air spot and iterate down a bit
                            AxisAlignedBB aabbTry2 = spotForHandsAir.offset(0, y - (yAirSize * 1D) - y2, 0);
                            AxisAlignedBB aabbRenderSolid = aabbTry2.offset(-player.posX, -playerAABB.minY, -player.posZ);
                            AxisAlignedBB aabb2 = new AxisAlignedBB(0, 0, 0, 0, 0, 0).grow(1, 1, 1);
                            if (player.world.getCollisionBoxes(player, aabbTry2).size() > 0 && aabbTry2.minY + 0.15D > playerAABB.minY) {
                                foundGrabbableSpot = true;
                                if (renderDebug) renderOffsetAABB(aabbRenderSolid.grow(xzSize, 0, xzSize), 0, 0, 0, 1, 0, 0);
                                //Render.renderOffsetAABB(aabbRender, -camera.posX, -camera.posY, -camera.posZ);
                                //Render.renderOffsetAABB(aabb2, 0, 0, 0);
                                break;
                            }
                        }
                    }
                }
            }

            if (foundGrabbableSpot/*nearWall(player)*/) {
                float climbSpeed = 0.08F;
                if (player.motionY < climbSpeed) {
                    player.motionY = climbSpeed;
                }
            }
        }
    }

    public static void tickKnockbackResistence() {

        EntityPlayer player = Minecraft.getMinecraft().player;

        float speed = (float) Math.sqrt(player.motionX * player.motionX + player.motionY * player.motionY + player.motionZ * player.motionZ);

        if (player.hurtTime > 0) {

            player.hurtTime = 0;

            if (MovePlusCfg.knockbackResistAmount == 1D) {
                player.motionX = prevMotionX;
                player.motionY = prevMotionY;
                player.motionZ = prevMotionZ;
            } else {
                player.motionX = prevMotionX + (player.motionX * (1D - Math.min(MovePlusCfg.knockbackResistAmount, 1D)));
                player.motionY = prevMotionY + (prevMotionY > 0.1D ? 0D : (player.motionY * (1D - Math.min(MovePlusCfg.knockbackResistAmount, 1D))));
                player.motionZ = prevMotionZ + (player.motionZ * (1D - Math.min(MovePlusCfg.knockbackResistAmount, 1D)));
            }
        } else {
            prevMotionX = player.motionX;
            prevMotionY = player.motionY;
            prevMotionZ = player.motionZ;
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
            setVel(entity, (float)entity.motionX / 2.0F, y, (float)entity.motionZ / 2.0F);
        } else {
            setVel(entity, var13 * horizontalMultiplier * -1.0F, y, var15 * horizontalMultiplier);
        }
    }

    public static void setVel(Entity entity, float x, float y, float z) {
        entity.motionX += (double)x;
        entity.motionY = (double)y;
        entity.motionZ += (double)z;
    }

    public static void tickClientRenderScreen() {

    }

    public static void tickClientRenderWorldLast() {

    }

    public static boolean nearWall(EntityPlayer player) {
        return player.world.getCollisionBoxes(player, player.getEntityBoundingBox().grow(0.2D, 0.0D, 0.2D)).size() > 0;
    }

    public static void renderOffsetAABB(AxisAlignedBB boundingBox, double x, double y, double z, float r, float g, float b)
    {
        GlStateManager.disableTexture2D();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.color(r, g, b, 1.0F);
        bufferbuilder.setTranslation(x, y, z);
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_NORMAL);
        bufferbuilder.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).normal(0.0F, 0.0F, -1.0F).endVertex();
        bufferbuilder.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).normal(0.0F, 0.0F, -1.0F).endVertex();
        bufferbuilder.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).normal(0.0F, 0.0F, -1.0F).endVertex();
        bufferbuilder.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).normal(0.0F, 0.0F, -1.0F).endVertex();
        bufferbuilder.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).normal(0.0F, 0.0F, 1.0F).endVertex();
        bufferbuilder.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).normal(0.0F, 0.0F, 1.0F).endVertex();
        bufferbuilder.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).normal(0.0F, 0.0F, 1.0F).endVertex();
        bufferbuilder.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).normal(0.0F, 0.0F, 1.0F).endVertex();
        bufferbuilder.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).normal(0.0F, -1.0F, 0.0F).endVertex();
        bufferbuilder.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).normal(0.0F, -1.0F, 0.0F).endVertex();
        bufferbuilder.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).normal(0.0F, -1.0F, 0.0F).endVertex();
        bufferbuilder.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).normal(0.0F, -1.0F, 0.0F).endVertex();
        bufferbuilder.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).normal(0.0F, 1.0F, 0.0F).endVertex();
        bufferbuilder.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).normal(0.0F, 1.0F, 0.0F).endVertex();
        bufferbuilder.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).normal(0.0F, 1.0F, 0.0F).endVertex();
        bufferbuilder.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).normal(0.0F, 1.0F, 0.0F).endVertex();
        bufferbuilder.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).normal(-1.0F, 0.0F, 0.0F).endVertex();
        bufferbuilder.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).normal(-1.0F, 0.0F, 0.0F).endVertex();
        bufferbuilder.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).normal(-1.0F, 0.0F, 0.0F).endVertex();
        bufferbuilder.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).normal(-1.0F, 0.0F, 0.0F).endVertex();
        bufferbuilder.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).normal(1.0F, 0.0F, 0.0F).endVertex();
        bufferbuilder.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).normal(1.0F, 0.0F, 0.0F).endVertex();
        bufferbuilder.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).normal(1.0F, 0.0F, 0.0F).endVertex();
        bufferbuilder.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).normal(1.0F, 0.0F, 0.0F).endVertex();
        tessellator.draw();
        bufferbuilder.setTranslation(0.0D, 0.0D, 0.0D);
        GlStateManager.enableTexture2D();
    }

}
