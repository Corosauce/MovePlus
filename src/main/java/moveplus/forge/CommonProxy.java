package moveplus.forge;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class CommonProxy
{
    public MovePlus mod;

    public CommonProxy()
    {
    }

    public void preInit(MovePlus pMod)
    {
    	
    }
    
    public void init(MovePlus pMod)
    {
        mod = pMod;
    	
    }

}
