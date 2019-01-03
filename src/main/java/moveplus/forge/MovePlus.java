package moveplus.forge;

import modconfig.ConfigMod;
import moveplus.config.MovePlusCfg;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;


@Mod(modid = "moveplus", name="Move Plus", version="${version}")
public class MovePlus {
	
	@Mod.Instance( value = "moveplus" )
	public static MovePlus instance;
	public static String modID = "moveplus";
    
    /** For use in preInit ONLY */
    public Configuration config;
    
    @SidedProxy(clientSide = "moveplus.forge.ClientProxy", serverSide = "moveplus.forge.CommonProxy")
    public static CommonProxy proxy;
    
    //public static String eventChannelName = "moveplus";
	//public static final FMLEventChannel eventChannel = NetworkRegistry.INSTANCE.newEventDrivenChannel(eventChannelName);

    public MovePlus() {
    	
    }
    
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
    	
    	ConfigMod.addConfigFile(event, new MovePlusCfg());
        
        proxy.preInit(this);
    }
    
    @Mod.EventHandler
    public void load(FMLInitializationEvent event)
    {
    	proxy.init(this);
    	FMLCommonHandler.instance().bus().register(new EventHandlerFML());
    	
    }
    
    @Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		
	}
    
    @Mod.EventHandler
    public void serverStart(FMLServerStartedEvent event) {
    	
    }
    
    @Mod.EventHandler
    public void serverStop(FMLServerStoppedEvent event) {
    	
    }
    
	public static void dbg(Object obj) {
		if (true) System.out.println(obj);
	}
}
