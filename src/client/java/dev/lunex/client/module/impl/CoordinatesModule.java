package dev.lunex.client.module.impl;

import dev.lunex.client.config.ClientConfig;
import dev.lunex.client.module.Category;
import dev.lunex.client.module.Module;

import net.minecraft.client.option.KeyBinding;

public final class CoordinatesModule extends Module {
	public CoordinatesModule(ClientConfig config, KeyBinding keyBinding) {
		super("coordinates", "Coordinates", "Shows current player coordinates on the HUD.", Category.RENDER, keyBinding, true, config);
	}
}
