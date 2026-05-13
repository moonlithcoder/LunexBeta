package dev.lunex.client.render;

import dev.lunex.Lunex;
import dev.lunex.client.config.ClientConfig;
import dev.lunex.client.module.Module;
import dev.lunex.client.module.ModuleManager;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;

import org.lwjgl.glfw.GLFW;

public final class HudRenderer {
	private static final int PANEL_COLOR = 0x99000000;
	private static final int ACCENT_COLOR = 0xFF9E6CFF;
	private static final int TEXT_COLOR = 0xFFFFFFFF;
	private static final Identifier LAYER = Identifier.of(Lunex.MOD_ID, "hud");

	private final ClientConfig config;
	private final ModuleManager moduleManager;
	private final KeyBinding hudKeyBinding;

	public HudRenderer(ClientConfig config, ModuleManager moduleManager) {
		this.config = config;
		this.moduleManager = moduleManager;
		this.hudKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.lunex.toggle_hud",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_RIGHT_SHIFT,
				ModuleManager.KEY_CATEGORY
		));
	}

	public void register() {
		HudLayerRegistrationCallback.EVENT.register(layeredDrawer -> layeredDrawer.attachLayerAfter(IdentifiedLayer.MISC_OVERLAYS, LAYER, this::render));
	}

	private void render(DrawContext context, RenderTickCounter tickCounter) {
		handleHudKeybind();
		if (!config.isHudEnabled()) {
			return;
		}

		MinecraftClient client = MinecraftClient.getInstance();
		int x = 8;
		int y = 8;
		int width = 126;
		int lineHeight = 11;
		int height = 22 + moduleManager.getModules().size() * lineHeight;
		context.fill(x, y, x + width, y + height, PANEL_COLOR);
		context.fill(x, y, x + 2, y + height, ACCENT_COLOR);
		context.drawText(client.textRenderer, Lunex.NAME, x + 7, y + 6, ACCENT_COLOR, false);

		int moduleY = y + 18;
		for (Module module : moduleManager.getModules()) {
			int color = module.isEnabled() ? TEXT_COLOR : 0xFF777777;
			context.drawText(client.textRenderer, module.getName(), x + 7, moduleY, color, false);
			moduleY += lineHeight;
		}

		moduleManager.findById("coordinates")
				.filter(Module::isEnabled)
				.ifPresent(module -> renderCoordinates(context, client));
	}

	private void handleHudKeybind() {
		while (hudKeyBinding.wasPressed()) {
			config.setHudEnabled(!config.isHudEnabled());
		}
	}

	private void renderCoordinates(DrawContext context, MinecraftClient client) {
		if (client.player == null) {
			return;
		}

		int x = 8;
		int y = client.getWindow().getScaledHeight() - 18;
		String coordinates = "XYZ %.1f / %.1f / %.1f".formatted(client.player.getX(), client.player.getY(), client.player.getZ());
		context.fill(x - 2, y - 2, x + client.textRenderer.getWidth(coordinates) + 5, y + 11, PANEL_COLOR);
		context.drawText(client.textRenderer, coordinates, x, y, TEXT_COLOR, false);
	}
}
