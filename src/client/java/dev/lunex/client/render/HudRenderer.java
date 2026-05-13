package dev.lunex.client.render;

import dev.lunex.Lunex;
import dev.lunex.client.config.ClientConfig;
import dev.lunex.client.module.Module;
import dev.lunex.client.module.ModuleManager;

import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;

public final class HudRenderer {
	private static final int PANEL_COLOR = 0x99000000;
	private static final int ACCENT_COLOR = 0xFF9E6CFF;
	private static final int TEXT_COLOR = 0xFFFFFFFF;
	private static final Identifier LAYER = Identifier.of(Lunex.MOD_ID, "hud");

	private final ClientConfig config;
	private final ModuleManager moduleManager;

	public HudRenderer(ClientConfig config, ModuleManager moduleManager) {
		this.config = config;
		this.moduleManager = moduleManager;
	}

	public void register() {
		HudLayerRegistrationCallback.EVENT.register(layeredDrawer -> layeredDrawer.attachLayerAfter(IdentifiedLayer.MISC_OVERLAYS, LAYER, this::render));
	}

	private void render(DrawContext context, RenderTickCounter tickCounter) {
		if (!config.isHudEnabled()) {
			return;
		}

		MinecraftClient client = MinecraftClient.getInstance();
		int x = 8;
		int y = 8;
		int width = 126;
		int lineHeight = 11;
		int height = 22 + moduleManager.getModules().size() * lineHeight;
		Render2D.rect(context, x, y, width, height, PANEL_COLOR);
		Render2D.rect(context, x, y, 2, height, ACCENT_COLOR);
		Render2D.text(context, Lunex.NAME, x + 7, y + 6, ACCENT_COLOR);

		int moduleY = y + 18;
		for (Module module : moduleManager.getModules()) {
			int color = module.isEnabled() ? TEXT_COLOR : 0xFF777777;
			Render2D.text(context, module.getName(), x + 7, moduleY, color);
			moduleY += lineHeight;
		}

		moduleManager.findById("coordinates")
				.filter(Module::isEnabled)
				.ifPresent(module -> renderCoordinates(context, client));
	}

	private void renderCoordinates(DrawContext context, MinecraftClient client) {
		if (client.player == null) {
			return;
		}

		int x = 8;
		int y = client.getWindow().getScaledHeight() - 18;
		String coordinates = "XYZ %.1f / %.1f / %.1f".formatted(client.player.getX(), client.player.getY(), client.player.getZ());
		Render2D.rect(context, x - 2, y - 2, client.textRenderer.getWidth(coordinates) + 7, 13, PANEL_COLOR);
		Render2D.text(context, coordinates, x, y, TEXT_COLOR);
	}
}
