package dev.lunex.client.gui;

import dev.lunex.Lunex;
import dev.lunex.client.module.Category;
import dev.lunex.client.module.Module;
import dev.lunex.client.module.ModuleManager;
import dev.lunex.client.render.Render2D;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ClickGuiScreen extends Screen {
	private static final int GUI_WIDTH = 492;
	private static final int GUI_HEIGHT = 304;
	private static final int SIDEBAR_WIDTH = 132;
	private static final int CARD_WIDTH = 154;
	private static final int CARD_HEIGHT = 58;
	private static final int BACKGROUND = 0xF3121219;
	private static final int PANEL = 0xE91A1A25;
	private static final int PANEL_DARK = 0xF0090910;
	private static final int PANEL_HOVER = 0xFF292938;
	private static final int FIELD = 0xE80D0D14;
	private static final int ACCENT = 0xFFFF6FD8;
	private static final int ACCENT_2 = 0xFFA26BFF;
	private static final int ACCENT_3 = 0xFF54E7FF;
	private static final int TEXT = 0xFFF8F7FF;
	private static final int MUTED = 0xFFA1A0AF;
	private static final int DARK_TEXT = 0xFF686877;
	private static final int OFF = 0xFF4E4E5B;

	private final ModuleManager moduleManager;
	private final Map<String, Animation> hoverAnimations = new HashMap<>();
	private final Map<String, Animation> toggleAnimations = new HashMap<>();
	private final Animation openAnimation = new Animation(0.0F);
	private final Animation categorySlide = new Animation(0.0F);
	private final Animation scrollAnimation = new Animation(0.0F);
	private Category selectedCategory = Category.COMBAT;
	private int targetScroll;
	private int ticks;

	public ClickGuiScreen(ModuleManager moduleManager) {
		super(Text.literal("Lunex ClickGUI"));
		this.moduleManager = moduleManager;
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		ticks++;
		openAnimation.animate(1.0F, 0.17F);
		scrollAnimation.animate(targetScroll, 0.25F);
		categorySlide.animate(categoryIndex(selectedCategory), 0.21F);
		renderBackground(context, mouseX, mouseY, delta);

		float open = Animation.easeOutBack(Render2D.clamp(openAnimation.get(), 0.0F, 1.0F));
		int x = (width - GUI_WIDTH) / 2;
		int y = (height - GUI_HEIGHT) / 2;
		MatrixStack matrices = context.getMatrices();
		matrices.push();
		matrices.translate(x + GUI_WIDTH / 2.0F, y + GUI_HEIGHT / 2.0F, 0.0F);
		matrices.scale(0.84F + open * 0.16F, 0.84F + open * 0.16F, 1.0F);
		matrices.translate(-(x + GUI_WIDTH / 2.0F), -(y + GUI_HEIGHT / 2.0F), 0.0F);

		int alpha = (int) (242 * Render2D.clamp(openAnimation.get(), 0.0F, 1.0F));
		int accent = animatedAccent();
		Render2D.roundedGlow(context, x, y, GUI_WIDTH, GUI_HEIGHT, 20, accent, 9);
		Render2D.roundedVerticalGradient(context, x, y, GUI_WIDTH, GUI_HEIGHT, 20, Render2D.alpha(BACKGROUND, alpha), Render2D.alpha(PANEL_DARK, alpha));
		Render2D.roundedBorder(context, x, y, GUI_WIDTH, GUI_HEIGHT, 20, Render2D.alpha(accent, 120));
		Render2D.roundedHorizontalGradient(context, x + 16, y + 9, GUI_WIDTH - 32, 3, 2, ACCENT, ACCENT_3);

		renderOrbits(context, x, y, accent);
		renderSidebar(context, mouseX, mouseY, x, y, GUI_HEIGHT, accent);
		renderModulePanel(context, mouseX, mouseY, x + SIDEBAR_WIDTH, y + 14, GUI_WIDTH - SIDEBAR_WIDTH - 14, GUI_HEIGHT - 28, accent);
		matrices.pop();
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		int x = (width - GUI_WIDTH) / 2;
		int y = (height - GUI_HEIGHT) / 2;

		int categoryY = y + 76;
		for (Category category : Category.values()) {
			if (Render2D.hovered(mouseX, mouseY, x + 14, categoryY, 104, 28)) {
				selectedCategory = category;
				targetScroll = 0;
				return true;
			}
			categoryY += 34;
		}

		List<Module> modules = moduleManager.getModules(selectedCategory);
		int panelX = x + SIDEBAR_WIDTH;
		int panelY = y + 14;
		int startX = panelX + 24;
		int startY = panelY + 70 + Math.round(scrollAnimation.get());
		for (int index = 0; index < modules.size(); index++) {
			int column = index % 2;
			int row = index / 2;
			int moduleX = startX + column * 168;
			int moduleY = startY + row * 70;
			if (Render2D.hovered(mouseX, mouseY, moduleX, moduleY, CARD_WIDTH, CARD_HEIGHT)) {
				modules.get(index).toggle();
				return true;
			}
		}

		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		targetScroll += (int) (verticalAmount * 22.0D);
		targetScroll = Math.min(0, Math.max(targetScroll, -180));
		return true;
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	private void renderOrbits(DrawContext context, int x, int y, int accent) {
		float pulse = pulse();
		Render2D.roundedRect(context, x + GUI_WIDTH - 74, y + 18, 42, 42, 21, Render2D.alpha(ACCENT_3, (int) (18 + pulse * 18)));
		Render2D.roundedBorder(context, x + GUI_WIDTH - 70, y + 22, 34, 34, 17, Render2D.alpha(accent, 58));
		Render2D.roundedRect(context, x + 168, y + GUI_HEIGHT - 38, 78, 6, 3, Render2D.alpha(ACCENT_2, 34));
		Render2D.roundedRect(context, x + 254, y + GUI_HEIGHT - 40, 42, 6, 3, Render2D.alpha(ACCENT_3, 28));
	}

	private void renderSidebar(DrawContext context, int mouseX, int mouseY, int x, int y, int height, int accent) {
		Render2D.roundedRect(context, x + 8, y + 10, SIDEBAR_WIDTH - 16, height - 20, 17, 0xE5161620);
		Render2D.roundedBorder(context, x + 8, y + 10, SIDEBAR_WIDTH - 16, height - 20, 17, 0x24FFFFFF);
		Render2D.roundedGlow(context, x + 19, y + 20, 28, 28, 10, accent, 5);
		Render2D.roundedHorizontalGradient(context, x + 18, y + 19, 30, 30, 10, ACCENT, ACCENT_2);
		Render2D.centeredText(context, "L", x + 33, y + 30, TEXT);
		Render2D.scaledText(context, Lunex.NAME, x + 56, y + 21, 1.32F, TEXT);
		Render2D.text(context, "private fabric", x + 57, y + 37, MUTED);
		Render2D.roundedHorizontalGradient(context, x + 18, y + 60, 96, 2, 1, Render2D.alpha(ACCENT, 180), Render2D.alpha(ACCENT_3, 170));

		int indicatorY = y + 76 + Math.round(categorySlide.get() * 34.0F);
		Render2D.roundedGlow(context, x + 14, indicatorY, 104, 28, 11, accent, 4);
		Render2D.roundedHorizontalGradient(context, x + 14, indicatorY, 104, 28, 11, Render2D.alpha(ACCENT, 210), Render2D.alpha(ACCENT_2, 190));

		int categoryY = y + 76;
		for (Category category : Category.values()) {
			boolean selected = category == selectedCategory;
			boolean hovered = Render2D.hovered(mouseX, mouseY, x + 14, categoryY, 104, 28);
			float hover = animation("category:" + category.name(), hovered || selected, 0.18F);
			if (!selected && hover > 0.02F) {
				Render2D.roundedRect(context, x + 14, categoryY, 104, 28, 11, Render2D.alpha(PANEL_HOVER, (int) (122 * hover)));
			}

			int color = selected ? TEXT : Render2D.lerpColor(MUTED, TEXT, hover);
			Render2D.text(context, icon(category), x + 25 + Math.round(2.0F * hover), categoryY + 10, color);
			Render2D.text(context, category.getTitle(), x + 48 + Math.round(2.0F * hover), categoryY + 10, color);
			categoryY += 34;
		}

		renderFooter(context, x, y, height, accent);
	}

	private void renderFooter(DrawContext context, int x, int y, int height, int accent) {
		int enabled = (int) moduleManager.getModules().stream().filter(Module::isEnabled).count();
		int footerY = y + height - 50;
		Render2D.roundedRect(context, x + 18, footerY, 96, 28, 10, 0xD00D0D14);
		Render2D.roundedBorder(context, x + 18, footerY, 96, 28, 10, Render2D.alpha(accent, 52));
		Render2D.text(context, enabled + " active", x + 29, footerY + 7, TEXT);
		Render2D.roundedRect(context, x + 85, footerY + 9, 17, 10, 5, Render2D.alpha(accent, 92));
	}

	private void renderModulePanel(DrawContext context, int mouseX, int mouseY, int x, int y, int panelWidth, int panelHeight, int accent) {
		Render2D.roundedRect(context, x, y, panelWidth, panelHeight, 18, 0x82101017);
		Render2D.roundedBorder(context, x, y, panelWidth, panelHeight, 18, 0x28FFFFFF);
		Render2D.text(context, selectedCategory.getTitle(), x + 24, y + 18, TEXT);
		Render2D.text(context, "modules", x + 24 + Render2D.width(selectedCategory.getTitle()) + 7, y + 18, MUTED);
		renderSearch(context, x + panelWidth - 164, y + 13, accent);
		renderCategoryStats(context, x + 24, y + 40, accent);

		context.enableScissor(x + 14, y + 64, x + panelWidth - 10, y + panelHeight - 12);
		List<Module> modules = moduleManager.getModules(selectedCategory);
		int startX = x + 24;
		int startY = y + 70 + Math.round(scrollAnimation.get());
		for (int index = 0; index < modules.size(); index++) {
			int column = index % 2;
			int row = index / 2;
			int moduleX = startX + column * 168;
			int moduleY = startY + row * 70;
			float delay = Render2D.clamp(openAnimation.get() + index * 0.055F, 0.0F, 1.0F);
			int animatedY = moduleY + Math.round((1.0F - Animation.easeOutCubic(delay)) * 20.0F);
			renderModuleCard(context, mouseX, mouseY, modules.get(index), moduleX, animatedY, index, accent);
		}
		context.disableScissor();
	}

	private void renderSearch(DrawContext context, int x, int y, int accent) {
		float shine = pulse(1.7F);
		Render2D.roundedGlow(context, x, y, 140, 28, 11, Render2D.lerpColor(accent, ACCENT_3, shine), 2);
		Render2D.roundedRect(context, x, y, 140, 28, 11, FIELD);
		Render2D.roundedBorder(context, x, y, 140, 28, 11, Render2D.alpha(accent, 58));
		Render2D.text(context, "⌕", x + 12, y + 10, MUTED);
		Render2D.text(context, "Search", x + 31, y + 10, DARK_TEXT);
		Render2D.text(context, "✦", x + 118, y + 10, Render2D.lerpColor(MUTED, accent, shine));
	}

	private void renderCategoryStats(DrawContext context, int x, int y, int accent) {
		List<Module> modules = moduleManager.getModules(selectedCategory);
		long active = modules.stream().filter(Module::isEnabled).count();
		String label = active + "/" + modules.size() + " enabled";
		Render2D.roundedRect(context, x, y, 86, 18, 8, 0xA60D0D14);
		Render2D.roundedBorder(context, x, y, 86, 18, 8, Render2D.alpha(accent, 42));
		Render2D.text(context, label, x + 10, y + 5, MUTED);
	}

	private void renderModuleCard(DrawContext context, int mouseX, int mouseY, Module module, int x, int y, int index, int accent) {
		boolean hovered = Render2D.hovered(mouseX, mouseY, x, y, CARD_WIDTH, CARD_HEIGHT);
		float hover = animation("hover:" + module.getId(), hovered, 0.2F);
		float enabled = animation("toggle:" + module.getId(), module.isEnabled(), 0.18F);
		float wave = module.isEnabled() ? pulse(index * 0.45F) : hover;
		int top = Render2D.lerpColor(Render2D.lerpColor(PANEL, PANEL_HOVER, hover), Render2D.alpha(ACCENT, 230), enabled * 0.45F);
		int bottom = Render2D.lerpColor(PANEL_DARK, Render2D.alpha(ACCENT_2, 220), enabled * 0.65F);
		int drawY = y - Math.round(hover * 4.0F);
		int glowColor = Render2D.lerpColor(accent, ACCENT_3, wave);
		Render2D.roundedGlow(context, x, drawY, CARD_WIDTH, CARD_HEIGHT, 15, glowColor, (int) (hover * 4.0F + enabled * 6.0F));
		Render2D.roundedVerticalGradient(context, x, drawY, CARD_WIDTH, CARD_HEIGHT, 15, top, bottom);
		Render2D.roundedBorder(context, x, drawY, CARD_WIDTH, CARD_HEIGHT, 15, Render2D.alpha(TEXT, (int) (34 + enabled * 78 + hover * 20)));
		Render2D.roundedHorizontalGradient(context, x + 8, drawY + 7, Math.round((CARD_WIDTH - 16) * Math.max(hover, enabled)), 3, 2, ACCENT, ACCENT_3);

		Render2D.text(context, module.getName(), x + 13, drawY + 16, TEXT);
		Render2D.text(context, Render2D.trimToWidth(module.getDescription(), 94), x + 13, drawY + 32, Render2D.lerpColor(OFF, 0xFFE9E8F5, enabled));
		renderStatusDot(context, x + CARD_WIDTH - 24, drawY + 13, enabled, glowColor);
		renderToggle(context, x + CARD_WIDTH - 46, drawY + CARD_HEIGHT - 21, enabled);
	}

	private void renderStatusDot(DrawContext context, int x, int y, float enabled, int accent) {
		Render2D.roundedRect(context, x, y, 9, 9, 4, Render2D.lerpColor(OFF, accent, enabled));
		if (enabled > 0.05F) {
			Render2D.roundedGlow(context, x, y, 9, 9, 4, accent, Math.round(4 * enabled));
		}
	}

	private void renderToggle(DrawContext context, int x, int y, float enabled) {
		Render2D.roundedHorizontalGradient(context, x, y, 32, 14, 7, Render2D.lerpColor(0xFF30303B, ACCENT_2, enabled), Render2D.lerpColor(0xFF3B3B48, ACCENT_3, enabled));
		Render2D.roundedBorder(context, x, y, 32, 14, 7, Render2D.alpha(TEXT, (int) (28 + enabled * 74)));
		Render2D.roundedRect(context, x + 3 + Math.round(enabled * 16.0F), y + 3, 8, 8, 4, TEXT);
	}

	private float animation(String id, boolean target, float speed) {
		Animation animation = id.startsWith("toggle:") ? toggleAnimations.computeIfAbsent(id, key -> new Animation(0.0F)) : hoverAnimations.computeIfAbsent(id, key -> new Animation(0.0F));
		animation.animate(target ? 1.0F : 0.0F, speed);
		return animation.get();
	}

	private int animatedAccent() {
		return Render2D.lerpColor(ACCENT, ACCENT_3, pulse());
	}

	private float pulse() {
		return pulse(0.0F);
	}

	private float pulse(float offset) {
		return (float) ((Math.sin((ticks + offset) * 0.08D) + 1.0D) * 0.5D);
	}

	private int categoryIndex(Category category) {
		Category[] categories = Category.values();
		for (int index = 0; index < categories.length; index++) {
			if (categories[index] == category) {
				return index;
			}
		}

		return 0;
	}

	private String icon(Category category) {
		return switch (category) {
			case COMBAT -> "⚔";
			case MOVEMENT -> "➤";
			case RENDER -> "◈";
			case MISC -> "☰";
		};
	}
}
