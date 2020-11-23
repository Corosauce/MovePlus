package com.corosus.moveplus.mixin;

import com.corosus.moveplus.config.MovePlusCfgForge;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ReportedException;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer<T extends Entity> {

    @Overwrite
    public boolean shouldRender(T livingEntityIn, ClippingHelper camera, double camX, double camY, double camZ) {
        if (!isInRangeToRender3d(livingEntityIn, camX, camY, camZ)) {
            return false;
        } else if (livingEntityIn.ignoreFrustumCheck) {
            return true;
        } else {
            AxisAlignedBB axisalignedbb = livingEntityIn.getRenderBoundingBox().grow(0.5D);
            if (axisalignedbb.hasNaN() || axisalignedbb.getAverageEdgeLength() == 0.0D) {
                axisalignedbb = new AxisAlignedBB(livingEntityIn.getPosX() - 2.0D, livingEntityIn.getPosY() - 2.0D, livingEntityIn.getPosZ() - 2.0D, livingEntityIn.getPosX() + 2.0D, livingEntityIn.getPosY() + 2.0D, livingEntityIn.getPosZ() + 2.0D);
            }

            return camera.isBoundingBoxInFrustum(axisalignedbb);
        }
    }

    public boolean isInRangeToRender3d(T livingEntityIn, double x, double y, double z) {
        double d0 = livingEntityIn.getPosX() - x;
        double d1 = livingEntityIn.getPosY() - y;
        double d2 = livingEntityIn.getPosZ() - z;
        double d3 = d0 * d0 + d1 * d1 + d2 * d2;
        if (d3 > MovePlusCfgForge.GENERAL.entityRenderRangeMax.get() * MovePlusCfgForge.GENERAL.entityRenderRangeMax.get()) {
            if (!MovePlusCfgForge.GENERAL.entityRenderLimitModdedOnly.get() || !livingEntityIn.getClass().getCanonicalName().startsWith("net.minecraft")) {
                return false;
            }
        }
        return livingEntityIn.isInRangeToRenderDist(d3);
    }
}