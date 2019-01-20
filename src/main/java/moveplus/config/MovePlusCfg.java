package moveplus.config;

import modconfig.IConfigCategory;
import moveplus.forge.MovePlus;

public class MovePlusCfg implements IConfigCategory {

    public static boolean useGroundDodge = true;
    public static boolean useLedgeClimb = true;
    public static long dodgeDelay = 300;
    public static double knockbackResistAmount = 0.3D;
    
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
