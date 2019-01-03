package moveplus.forge;


import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy
{

    public ClientProxy()
    {
    	
    }
    
    @Override
    public void preInit(MovePlus pMod)
    {
    	super.preInit(pMod);
    }

    @Override
    public void init(MovePlus pMod)
    {
        super.init(pMod);
    }
    
}
