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
}
