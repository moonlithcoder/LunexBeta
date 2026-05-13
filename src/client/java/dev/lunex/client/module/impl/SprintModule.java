package dev.lunex.client.module.impl;

import dev.lunex.client.config.ClientConfig;
import dev.lunex.client.module.Category;
import dev.lunex.client.module.Module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;

public final class SprintModule extends Module {
	public SprintModule(ClientConfig config, KeyBinding keyBinding) {
		super("sprint", "Sprint", "Keeps sprint active while moving forward.", Category.MOVEMENT, keyBinding, true, config);
	}

	@Override
	public void tick(MinecraftClient client) {
		if (client.player != null && client.options.forwardKey.isPressed()) {
			client.player.setSprinting(true);
		}
	}
}
