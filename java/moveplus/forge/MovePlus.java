package moveplus.forge;

import modconfig.ConfigMod;
import moveplus.config.MovePlusCfg;
import net.minecraftforge.common.config.Configuration;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStoppedEvent;
import cpw.mods.fml.common.network.FMLEventChannel;
import cpw.mods.fml.common.network.NetworkRegistry;


@Mod(modid = "moveplus", name="Move Plus", version="v2.3.1")
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
    	
    	ConfigMod.addConfigFile(event, "moveplus", new MovePlusCfg());
    	
    	//eventChannel.register(new EventHandlerPacket());
        
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
