package moveplus.forge;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;

public class CommonProxy implements IGuiHandler
{
    public World mainWorld;
    private int entityId = 0;

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

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world,
			int x, int y, int z) {
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world,
			int x, int y, int z) {
		return null;
	}
	
    public static void addBlock(Block block, Class tEnt, String unlocalizedName, String blockNameBase) {
		addBlock(block, unlocalizedName, blockNameBase);
		GameRegistry.registerTileEntity(tEnt, unlocalizedName);
	}
	
	public static void addBlock(Block parBlock, String unlocalizedName, String blockNameBase) {
		//vanilla calls
		GameRegistry.registerBlock(parBlock, unlocalizedName);
		parBlock.setBlockName(MovePlus.modID + ":" + unlocalizedName);
		parBlock.setBlockTextureName(MovePlus.modID + ":" + unlocalizedName);
		parBlock.setCreativeTab(CreativeTabs.tabMisc);
		LanguageRegistry.addName(parBlock, blockNameBase);
	}

}
