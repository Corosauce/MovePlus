package com.corosus.moveplus.config;

import com.corosus.moveplus.forge.MovePlus;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;

import static net.minecraftforge.common.ForgeConfigSpec.*;

@EventBusSubscriber
public class MovePlusCfgForge {

    private static final Builder CLIENT_BUILDER = new Builder();

    public static final CategoryGeneral GENERAL = new CategoryGeneral();

    public static final class CategoryGeneral {

        public final BooleanValue useGroundDodge;
        public final BooleanValue useLedgeClimb;
        public final IntValue doubleTapDodgeMaxTimeInMilliseconds;
        public final DoubleValue knockbackResistAmount;
        public final BooleanValue dontGroundDodgeIfSneaking;
        public final DoubleValue groundDodgeForceHorizontal;
        public final DoubleValue groundDodgeForceVertical;

        public final DoubleValue tileEntityRenderRangeMax;
        public final DoubleValue entityRenderRangeMax;

        public final BooleanValue tileEntityRenderLimitModdedOnly;
        public final BooleanValue entityRenderLimitModdedOnly;

        private CategoryGeneral() {
            CLIENT_BUILDER.comment("General mod settings").push("general");

            useGroundDodge = CLIENT_BUILDER
                    .define("useGroundDodge", true);

            useLedgeClimb = CLIENT_BUILDER
                    .define("useLedgeClimb", true);

            doubleTapDodgeMaxTimeInMilliseconds = CLIENT_BUILDER
                    .defineInRange("doubleTapDodgeMaxTimeInMilliseconds", 300, 0, Integer.MAX_VALUE);

            knockbackResistAmount = CLIENT_BUILDER
                    .defineInRange("knockbackResistAmount", 0.3D, 0D, 1D);

            dontGroundDodgeIfSneaking = CLIENT_BUILDER
                    .define("dontGroundDodgeIfSneaking", true);

            groundDodgeForceHorizontal = CLIENT_BUILDER
                    .defineInRange("groundDodgeForceHorizontal", 1D, 0D, 1D);

            groundDodgeForceVertical = CLIENT_BUILDER
                    .defineInRange("groundDodgeForceVertical", 0.4D, 0D, 0.5D);

            tileEntityRenderRangeMax = CLIENT_BUILDER
                    .defineInRange("tileEntityRenderRangeMax", 64, 1D, Double.MAX_VALUE);

            entityRenderRangeMax = CLIENT_BUILDER
                    .defineInRange("entityRenderRangeMax", 64, 1D, Double.MAX_VALUE);

            tileEntityRenderLimitModdedOnly = CLIENT_BUILDER
                    .define("tileEntityRenderLimitModdedOnly", true);

            entityRenderLimitModdedOnly = CLIENT_BUILDER
                    .define("entityRenderLimitModdedOnly", true);

            CLIENT_BUILDER.pop();
        }
    }
    public static final ForgeConfigSpec CLIENT_CONFIG = CLIENT_BUILDER.build();

    //TODO: off for 1.15
    /*public static void onLoad(final ModConfig.Loading configEvent) {
        MovePlus.LOGGER.info("ModConfig.Loading!" + configEvent.toString());
    }

    public static void onFileChange(final ModConfig.ConfigReloading configEvent) {
        MovePlus.LOGGER.info("file changed!" + configEvent.toString());
    }*/
}
