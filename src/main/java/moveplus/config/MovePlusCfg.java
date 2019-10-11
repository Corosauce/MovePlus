package moveplus.config;

import moveplus.forge.MovePlus;

public class MovePlusCfg/* implements IConfigCategory*/ {

    public static boolean useGroundDodge = true;
    public static boolean useLedgeClimb = true;
    public static int doubleTapDodgeMaxTimeInMilliseconds = 300;
    public static double knockbackResistAmount = 0.3D;
    public static boolean dontGroundDodgeIfSneaking = true;

    //@ConfigComment("Range: 0.0 to 1.0")
    public static double groundDodgeForceHorizontal = 1D;

    //@ConfigComment("Range: 0.0 to 0.5")
    public static double groundDodgeForceVertical = 0.4D;
    
	//@Override
	public String getCategory() {
		return "MovePlus General";
	}

	//@Override
	public String getConfigFileName() {
		return getName();
	}

    //@Override
    public String getName() {
        return "MovePlus";
    }

    //@Override
    public String getRegistryName() {
        return MovePlus.MODID + ":" + getName();
    }

    //@Override
    public void hookUpdatedValues() {
        /*groundDodgeForceHorizontal = Math.min(groundDodgeForceHorizontal, 1D);
        groundDodgeForceVertical = Math.min(groundDodgeForceVertical, 0.5D);*/

        groundDodgeForceHorizontal = Math.max(Math.min(groundDodgeForceHorizontal, 1.0D), 0D);
        groundDodgeForceVertical = Math.max(Math.min(groundDodgeForceVertical, 0.5D), 0D);
    }

}


