package moveplus.forge;


import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class EventHandlerFML {

	public static World lastWorld = null;
	
	@SubscribeEvent
	public void tickWorld(TickEvent.WorldTickEvent event) {
		if (event.phase == TickEvent.Phase.START) {
			
		}
	}
	
	@SubscribeEvent
	public void tickServer(TickEvent.ServerTickEvent event) {
		
		if (event.phase == TickEvent.Phase.START) {
			
		}
		
	}
	
	@SubscribeEvent
	public void tickClient(TickEvent.ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.START) {
			ClientTicker.tickClientGame();
		}
	}
	
	@SubscribeEvent
	public void tickRenderScreen(TickEvent.RenderTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			ClientTicker.tickClientRenderScreen();
		}
	}
}
