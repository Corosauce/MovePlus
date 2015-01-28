package moveplus.forge;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.client.FMLClientHandler;

public class ClientTickHandler
{
	public static ResourceLocation resTerrain = TextureMap.locationBlocksTexture;
	
    public static void onRenderTick()
    {
    	Minecraft mc = FMLClientHandler.instance().getClient();
    	
    	boolean debug = false;
    	
    	if (mc != null && mc.thePlayer != null && (mc.currentScreen == null || debug))
        {
    		
        }
    }
}
