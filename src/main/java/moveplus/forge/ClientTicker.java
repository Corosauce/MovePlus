package moveplus.forge;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.input.Keyboard;

public class ClientTicker {

    public static void tickClientGame() {
        long curTime = System.currentTimeMillis();

        EntityPlayer player = Minecraft.getMinecraft().player;
        Entity camera = Minecraft.getMinecraft().getRenderViewEntity();

        boolean renderDebug = false;

        if (player == null || camera == null) return;

        if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)/* && !player.onGround*/) {

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
