package dev.lunex.client.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

public final class Render2D {
	private Render2D() {
	}

	public static void rect(DrawContext context, int x, int y, int width, int height, int color) {
		context.fill(x, y, x + width, y + height, color);
	}

	public static void border(DrawContext context, int x, int y, int width, int height, int color) {
		context.drawBorder(x, y, width, height, color);
	}

	public static void glow(DrawContext context, int x, int y, int width, int height, int color, int radius) {
		for (int layer = radius; layer > 0; layer--) {
			int alpha = Math.max(8, 42 - layer * 5);
			border(context, x - layer, y - layer, width + layer * 2, height + layer * 2, alpha(color, alpha));
		}
	}

	public static void horizontalGradient(DrawContext context, int x, int y, int width, int height, int leftColor, int rightColor) {
		for (int offset = 0; offset < width; offset++) {
			float progress = width <= 1 ? 1.0F : offset / (float) (width - 1);
			context.fill(x + offset, y, x + offset + 1, y + height, lerpColor(leftColor, rightColor, progress));
		}
	}

	public static void verticalGradient(DrawContext context, int x, int y, int width, int height, int topColor, int bottomColor) {
		context.fillGradient(x, y, x + width, y + height, topColor, bottomColor);
	}

	public static void text(DrawContext context, String text, int x, int y, int color) {
		text(context, text, x, y, color, false);
	}

	public static void text(DrawContext context, String text, int x, int y, int color, boolean shadow) {
		TextRenderer renderer = MinecraftClient.getInstance().textRenderer;
		context.drawText(renderer, text, x, y, color, shadow);
	}

	public static void scaledText(DrawContext context, String text, int x, int y, float scale, int color) {
		MatrixStack matrices = context.getMatrices();
		matrices.push();
		matrices.translate(x, y, 0.0F);
		matrices.scale(scale, scale, 1.0F);
		context.drawText(MinecraftClient.getInstance().textRenderer, text, 0, 0, color, false);
		matrices.pop();
	}

	public static void line(DrawContext context, int x1, int y1, int x2, int y2, int color) {
		if (y1 == y2) {
			context.drawHorizontalLine(Math.min(x1, x2), Math.max(x1, x2), y1, color);
			return;
		}

		if (x1 == x2) {
			context.drawVerticalLine(x1, Math.min(y1, y2), Math.max(y1, y2), color);
		}
	}

	public static boolean hovered(double mouseX, double mouseY, int x, int y, int width, int height) {
		return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
	}

	public static int alpha(int color, int alpha) {
		return (alpha << 24) | (color & 0x00FFFFFF);
	}

	public static int lerpColor(int start, int end, float progress) {
		float value = clamp(progress, 0.0F, 1.0F);
		int a = (int) (channel(start, 24) + (channel(end, 24) - channel(start, 24)) * value);
		int r = (int) (channel(start, 16) + (channel(end, 16) - channel(start, 16)) * value);
		int g = (int) (channel(start, 8) + (channel(end, 8) - channel(start, 8)) * value);
		int b = (int) (channel(start, 0) + (channel(end, 0) - channel(start, 0)) * value);
		return a << 24 | r << 16 | g << 8 | b;
	}

	public static float clamp(float value, float min, float max) {
		return Math.max(min, Math.min(max, value));
	}

	private static int channel(int color, int shift) {
		return color >> shift & 0xFF;
	}
}
