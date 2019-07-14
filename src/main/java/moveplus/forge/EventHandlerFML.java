package moveplus.forge;


import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.api.distmarker.Dist;

public class EventHandlerFML {
	
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

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public void tickClient(TickEvent.ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.START) {
			ClientTicker.tickClientGame();
		}
	}

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public void tickRenderScreen(TickEvent.RenderTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			ClientTicker.tickClientRenderScreen();
		}
	}

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public void tickRenderScreen(RenderWorldLastEvent event) {
		ClientTicker.tickClientRenderWorldLast();

		//TEMP FOR VISUAL DEBUG
		//ClientTicker.tickClientGame();
	}
}


