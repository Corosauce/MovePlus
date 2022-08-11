package com.corosus.ltmoveplus.network;

import com.corosus.ltmoveplus.forge.EventHandlerForge;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ToServerPlayerCrawlState {
	private boolean isCrawling;

	public ToServerPlayerCrawlState(boolean isCrawling) {
		this.isCrawling = isCrawling;
	}

	public void encode(FriendlyByteBuf buffer) {
		buffer.writeBoolean(isCrawling);
	}

	public static ToServerPlayerCrawlState decode(FriendlyByteBuf buffer) {
		return new ToServerPlayerCrawlState(buffer.readBoolean());
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			//System.out.println("received crawl packet: " + isCrawling);
			EventHandlerForge.setPlayerCrawlStateServer(ctx.get().getSender(), isCrawling);
			//WATUT.playerManagerServer.getPlayerStatus(uuid).setStatusType(type);
		});
		ctx.get().setPacketHandled(true);
	}
}
