package moveplus.forge;


import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
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


