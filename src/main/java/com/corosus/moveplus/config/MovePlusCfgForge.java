package com.corosus.moveplus.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
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

            CLIENT_BUILDER.pop();
        }
    }
    public static final ForgeConfigSpec CLIENT_CONFIG = CLIENT_BUILDER.build();

    public static void onLoad(final ModConfig.Loading configEvent) {

    }

    public static void onFileChange(final ModConfig.ConfigReloading configEvent) {

    }
}
