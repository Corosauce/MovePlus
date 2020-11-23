package com.corosus.moveplus.mixin;

import com.corosus.moveplus.config.MovePlusCfgForge;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ReportedException;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(TileEntityRendererDispatcher.class)
public abstract class MixinTileEntityRendererDispatcher {

    @Shadow
    public ActiveRenderInfo renderInfo;

    @Overwrite
    public <E extends TileEntity> void renderTileEntity(E tileEntityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn) {
        /*if (!tileEntityIn.getClass().getCanonicalName().contains("net.minecraft")) {
            return;
        }*/

        double dist = getDistanceSq(tileEntityIn, this.renderInfo.getProjectedView().x, this.renderInfo.getProjectedView().y, this.renderInfo.getProjectedView().z);
        if (dist > MovePlusCfgForge.GENERAL.tileEntityRenderRangeMax.get() * MovePlusCfgForge.GENERAL.tileEntityRenderRangeMax.get()) {
            if (!MovePlusCfgForge.GENERAL.tileEntityRenderLimitModdedOnly.get() || !tileEntityIn.getClass().getCanonicalName().startsWith("net.minecraft")) {
                return;
            }
        }
        TileEntityRenderer<E> tileentityrenderer = net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher.instance.getRenderer(tileEntityIn);
        if (tileentityrenderer != null) {
            if (tileEntityIn.hasWorld() && tileEntityIn.getType().isValidBlock(tileEntityIn.getBlockState().getBlock())) {
                runCrashReportable(tileEntityIn, () -> {
                    render(tileentityrenderer, tileEntityIn, partialTicks, matrixStackIn, bufferIn);
                });
            }
        }
    }

    private static void runCrashReportable(TileEntity tileEntityIn, Runnable runnableIn) {
        try {
            runnableIn.run();
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Rendering Block Entity");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Block Entity Details");
            tileEntityIn.addInfoToCrashReport(crashreportcategory);
            throw new ReportedException(crashreport);
        }
    }

    private static <T extends TileEntity> void render(TileEntityRenderer<T> rendererIn, T tileEntityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn) {
        World world = tileEntityIn.getWorld();
        int i;
        if (world != null) {
            i = WorldRenderer.getCombinedLight(world, tileEntityIn.getPos());
        } else {
            i = 15728880;
        }

        rendererIn.render(tileEntityIn, partialTicks, matrixStackIn, bufferIn, i, OverlayTexture.NO_OVERLAY);
    }

    public double getDistanceSq(TileEntity tileEntity, double x, double y, double z) {
        double d0 = (double)tileEntity.getPos().getX() + 0.5D - x;
        double d1 = (double)tileEntity.getPos().getY() + 0.5D - y;
        double d2 = (double)tileEntity.getPos().getZ() + 0.5D - z;
        return d0 * d0 + d1 * d1 + d2 * d2;
    }
}