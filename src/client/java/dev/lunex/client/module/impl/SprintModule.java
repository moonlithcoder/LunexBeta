package dev.lunex.client.module.impl;

import dev.lunex.client.config.ClientConfig;
import dev.lunex.client.module.Category;
import dev.lunex.client.module.Module;

import net.minecraft.client.MinecraftClient;

import org.lwjgl.glfw.GLFW;

public final class SprintModule extends Module {
	public SprintModule(ClientConfig config) {
		super("sprint", "Sprint", "Keeps sprint active", Category.MOVEMENT, true, GLFW.GLFW_KEY_R, config);
	}

	@Override
	public void tick(MinecraftClient client) {
		if (client.player != null && client.options.forwardKey.isPressed()) {
			client.player.setSprinting(true);
		}
	}
}
