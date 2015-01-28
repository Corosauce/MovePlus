package moveplus.forge;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import CoroUtil.packet.PacketHelper;
import CoroUtil.util.CoroUtilEntity;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class EventHandlerPacket {
	
	//1.6.4 original usage was PMGloveCommand channel, but we only have 1 type of packet, so a packetCommand lookup isnt needed
	
	@SubscribeEvent
	public void onPacketFromServer(FMLNetworkEvent.ClientCustomPacketEvent event) {
		
		try {
			NBTTagCompound nbt = PacketHelper.readNBTTagCompound(event.packet.payload());
			
			String packetCommand = nbt.getString("packetCommand");
			
			if (packetCommand.equals("")) {
				
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
	@SubscribeEvent
	public void onPacketFromClient(FMLNetworkEvent.ServerCustomPacketEvent event) {
		EntityPlayerMP entP = ((NetHandlerPlayServer)event.handler).playerEntity;
		
		try {
			
			ByteBuf buffer = event.packet.payload();
        	
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		
	}
    
    @SideOnly(Side.CLIENT)
    public String getSelfUsername() {
    	return CoroUtilEntity.getName(Minecraft.getMinecraft().thePlayer);
    }
	
}
