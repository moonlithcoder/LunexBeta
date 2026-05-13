package dev.lunex.client.gui;

import dev.lunex.Lunex;
import dev.lunex.client.module.Category;
import dev.lunex.client.module.Module;
import dev.lunex.client.module.ModuleManager;
import dev.lunex.client.render.Render2D;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.List;

public final class ClickGuiScreen extends Screen {
	private static final int BACKGROUND = 0xE914151A;
	private static final int PANEL = 0xF01B1B22;
	private static final int PANEL_DARK = 0xF0101015;
	private static final int PANEL_HOVER = 0xFF252532;
	private static final int ACCENT = 0xFFFF78DD;
	private static final int ACCENT_2 = 0xFF9E6CFF;
	private static final int TEXT = 0xFFF8F8F8;
	private static final int MUTED = 0xFF8C8C96;
	private static final int OFF = 0xFF4E4E58;

	private final ModuleManager moduleManager;
	private Category selectedCategory = Category.COMBAT;
	private int scroll;

	public ClickGuiScreen(ModuleManager moduleManager) {
		super(Text.literal("Lunex ClickGUI"));
		this.moduleManager = moduleManager;
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		renderBackground(context, mouseX, mouseY, delta);

		int guiWidth = 420;
		int guiHeight = 260;
		int x = (width - guiWidth) / 2;
		int y = (height - guiHeight) / 2;
		Render2D.verticalGradient(context, x, y, guiWidth, guiHeight, BACKGROUND, PANEL_DARK);
		Render2D.border(context, x, y, guiWidth, guiHeight, 0x66000000);
		renderSidebar(context, mouseX, mouseY, x, y, guiHeight);
		renderModulePanel(context, mouseX, mouseY, x + 116, y + 14, guiWidth - 130, guiHeight - 28);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		int guiWidth = 420;
		int guiHeight = 260;
		int x = (width - guiWidth) / 2;
		int y = (height - guiHeight) / 2;

		int categoryY = y + 54;
		for (Category category : Category.values()) {
			if (Render2D.hovered(mouseX, mouseY, x + 12, categoryY, 90, 22)) {
				selectedCategory = category;
				scroll = 0;
				return true;
			}
			categoryY += 27;
		}

		List<Module> modules = moduleManager.getModules(selectedCategory);
		int startX = x + 130;
		int startY = y + 50 + scroll;
		for (int index = 0; index < modules.size(); index++) {
			int column = index % 2;
			int row = index / 2;
			int moduleX = startX + column * 140;
			int moduleY = startY + row * 56;
			if (Render2D.hovered(mouseX, mouseY, moduleX, moduleY, 130, 46)) {
				modules.get(index).toggle();
				return true;
			}
		}

		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		scroll += (int) (verticalAmount * 12.0D);
		scroll = Math.min(0, Math.max(scroll, -120));
		return true;
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	private void renderSidebar(DrawContext context, int mouseX, int mouseY, int x, int y, int height) {
		Render2D.rect(context, x, y, 110, height, 0xF0181820);
		Render2D.text(context, "☾", x + 14, y + 15, ACCENT);
		Render2D.scaledText(context, Lunex.NAME, x + 34, y + 14, 1.15F, TEXT);
		Render2D.rect(context, x + 12, y + 38, 84, 1, 0x2233333A);

		int categoryY = y + 54;
		for (Category category : Category.values()) {
			boolean selected = category == selectedCategory;
			boolean hovered = Render2D.hovered(mouseX, mouseY, x + 12, categoryY, 90, 22);
			int color = selected ? ACCENT : hovered ? PANEL_HOVER : 0;
			if (color != 0) {
				Render2D.rect(context, x + 12, categoryY, 90, 22, color);
			}

			Render2D.text(context, icon(category), x + 20, categoryY + 7, selected ? TEXT : MUTED);
			Render2D.text(context, category.getTitle(), x + 39, categoryY + 7, selected ? TEXT : MUTED);
			categoryY += 27;
		}
	}

	private void renderModulePanel(DrawContext context, int mouseX, int mouseY, int x, int y, int width, int height) {
		Render2D.rect(context, x, y, width, height, 0xB0101015);
		Render2D.rect(context, x + 12, y + 12, width - 24, 22, 0xFF0C0C11);
		Render2D.text(context, "⌕", x + 20, y + 19, MUTED);
		Render2D.text(context, "Search", x + 38, y + 19, 0xFF5E5E66);
		Render2D.text(context, "⚙", x + width - 24, y + 19, MUTED);

		context.enableScissor(x + 10, y + 42, x + width - 10, y + height - 8);
		List<Module> modules = moduleManager.getModules(selectedCategory);
		int startX = x + 14;
		int startY = y + 44 + scroll;
		for (int index = 0; index < modules.size(); index++) {
			int column = index % 2;
			int row = index / 2;
			int moduleX = startX + column * 140;
			int moduleY = startY + row * 56;
			renderModuleCard(context, mouseX, mouseY, modules.get(index), moduleX, moduleY);
		}
		context.disableScissor();
	}

	private void renderModuleCard(DrawContext context, int mouseX, int mouseY, Module module, int x, int y) {
		boolean hovered = Render2D.hovered(mouseX, mouseY, x, y, 130, 46);
		boolean enabled = module.isEnabled();
		int top = enabled ? ACCENT : hovered ? PANEL_HOVER : PANEL;
		int bottom = enabled ? ACCENT_2 : PANEL_DARK;
		Render2D.verticalGradient(context, x, y, 130, 46, top, bottom);
		Render2D.border(context, x, y, 130, 46, enabled ? 0x66FFFFFF : 0x22000000);
		Render2D.text(context, module.getName() + (enabled ? " ◉" : " ○"), x + 8, y + 8, TEXT);
		Render2D.text(context, trim(module.getDescription(), 23), x + 8, y + 24, enabled ? 0xFFEFEFFF : OFF);
	}

	private String icon(Category category) {
		return switch (category) {
			case COMBAT -> "⚔";
			case MOVEMENT -> "➤";
			case VISUAL -> "◈";
			case MISC -> "☰";
		};
	}

	private String trim(String text, int limit) {
		if (text.length() <= limit) {
			return text;
		}

		return text.substring(0, limit - 1) + "…";
	}
}
