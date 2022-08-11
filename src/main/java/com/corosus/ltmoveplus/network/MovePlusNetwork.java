package com.corosus.ltmoveplus.network;

import com.corosus.ltmoveplus.forge.MovePlus;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class MovePlusNetwork {
	private static final String PROTOCOL_VERSION = "1";

	public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
			new ResourceLocation(MovePlus.MODID, "main"),
			() -> PROTOCOL_VERSION,
			(obj) -> true,
			(obj) -> true
	);

	public static void register() {

		int id = 0;

		CHANNEL.messageBuilder(ToServerPlayerCrawlState.class, id++)
				.encoder(ToServerPlayerCrawlState::encode)
				.decoder(ToServerPlayerCrawlState::decode)
				.consumer(ToServerPlayerCrawlState::handle)
				.add();
	}
}
