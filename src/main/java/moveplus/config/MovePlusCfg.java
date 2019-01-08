package moveplus.config;

import modconfig.IConfigCategory;
import moveplus.forge.MovePlus;

public class MovePlusCfg implements IConfigCategory {

    public static boolean useGroundDodge = true;
    public static long dodgeDelay = 300;
    public static double knockbackResistAmount = 0.3D;

	public static String toggleKey = "Z";
    public static boolean useDoubleJump = true;

    public static boolean useWallDodge = true;
    public static boolean useBoostDodge = true;
    public static boolean useSpeedJump = false;
    public static boolean useAirControl = true;
    public static int maxExtraJumps = 1;
    public static float dodgeUpForce = 0.3F;
    public static float doubleJumpForce = 0.5F;
    public static float dodgeForce = 1.2F;
    public static float fwForce = 0.8F;
    public static int dodgeMinDelayTime = 7;

    public static long boostDodgeDelay = 0;
    //public static boolean speedMining = false;
    public static boolean dolphinSkills = false;
    public static float dolphinSpeed = 0.2F;
    public static float dolphinSpeedMax = 15.0F;
    public static float airControlSpeed = 0.1F;
    public static float airControlSpeedMax = 0.15F;
    public static float speedJumpMax = 0.9F;
    public static boolean autoJump = false;
    public static boolean pathFeatures = false;
    public static boolean useStamina = true;
    public static boolean blockInfoToggle = false;
    public static boolean airResistanceRemoval = false;
    public static boolean fallDamageReduction = true;
    
	@Override
	public String getCategory() {
		return "MovePlus General";
	}

	@Override
	public String getConfigFileName() {
		return getName();
	}

	@Override
	public void hookUpdatedValues() {
		
	}

    @Override
    public String getName() {
        return "MovePlus";
    }

    @Override
    public String getRegistryName() {
        return MovePlus.modID + ":" + getName();
    }

}
