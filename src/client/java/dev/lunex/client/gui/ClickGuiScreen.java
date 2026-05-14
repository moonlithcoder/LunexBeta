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
	private static final int GUI_WIDTH = 488;
	private static final int GUI_HEIGHT = 300;
	private static final int SIDEBAR_WIDTH = 130;
	private static final int CARD_WIDTH = 152;
	private static final int CARD_HEIGHT = 58;
	private static final int CARD_GAP = 164;
	private static final int BACKGROUND = 0xF20A0B0F;
	private static final int PANEL = 0xF0161720;
	private static final int PANEL_DARK = 0xF006070A;
	private static final int PANEL_HOVER = 0xFF222431;
	private static final int FIELD = 0xEE101119;
	private static final int ACCENT = 0xFF3C414B;
	private static final int ACCENT_SOFT = 0xFF252932;
	private static final int TEXT = 0xFFF4F1E8;
	private static final int MUTED = 0xFF9B9FAA;
	private static final int DARK_TEXT = 0xFF6E737E;
	private static final int OFF = 0xFF4C4A45;

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
		Render2D.roundedGlow(context, x, y, GUI_WIDTH, GUI_HEIGHT, 20, ACCENT, 6);
		Render2D.roundedVerticalGradient(context, x, y, GUI_WIDTH, GUI_HEIGHT, 20, Render2D.alpha(BACKGROUND, alpha), Render2D.alpha(PANEL_DARK, alpha));
		Render2D.roundedBorder(context, x, y, GUI_WIDTH, GUI_HEIGHT, 20, Render2D.alpha(ACCENT, 120));
		Render2D.roundedRect(context, x + 16, y + 9, GUI_WIDTH - 32, 2, 1, Render2D.alpha(0xFFB9BEC8, 80));

		renderOrbits(context, x, y);
		renderSidebar(context, mouseX, mouseY, x, y, GUI_HEIGHT);
		renderModulePanel(context, mouseX, mouseY, x + SIDEBAR_WIDTH, y + 14, GUI_WIDTH - SIDEBAR_WIDTH - 14, GUI_HEIGHT - 28);
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
			int moduleX = startX + column * CARD_GAP;
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

	private void renderOrbits(DrawContext context, int x, int y) {
		float pulse = pulse();
		Render2D.roundedRect(context, x + GUI_WIDTH - 74, y + 18, 42, 42, 21, Render2D.alpha(ACCENT, (int) (10 + pulse * 10)));
		Render2D.roundedBorder(context, x + GUI_WIDTH - 70, y + 22, 34, 34, 17, Render2D.alpha(ACCENT, 42));
		Render2D.roundedRect(context, x + 168, y + GUI_HEIGHT - 38, 78, 5, 2, Render2D.alpha(0xFFB9BEC8, 12));
		Render2D.roundedRect(context, x + 254, y + GUI_HEIGHT - 40, 42, 5, 2, Render2D.alpha(0xFFB9BEC8, 8));
	}

	private void renderSidebar(DrawContext context, int mouseX, int mouseY, int x, int y, int height) {
		Render2D.roundedRect(context, x + 8, y + 10, SIDEBAR_WIDTH - 16, height - 20, 17, 0xEA11131A);
		Render2D.roundedBorder(context, x + 8, y + 10, SIDEBAR_WIDTH - 16, height - 20, 17, 0x24FFFFFF);
		Render2D.roundedGlow(context, x + 19, y + 20, 28, 28, 10, ACCENT, 5);
		Render2D.roundedRect(context, x + 18, y + 19, 30, 30, 10, 0xFF2C3038);
		Render2D.centeredText(context, "L", x + 33, y + 30, TEXT);
		Render2D.scaledText(context, Lunex.NAME, x + 56, y + 21, 1.32F, TEXT);
		Render2D.text(context, "private fabric", x + 57, y + 37, MUTED);
		Render2D.roundedRect(context, x + 18, y + 60, 96, 2, 1, Render2D.alpha(0xFFB9BEC8, 72));

		int indicatorY = y + 76 + Math.round(categorySlide.get() * 34.0F);
		Render2D.roundedGlow(context, x + 14, indicatorY, 104, 28, 11, ACCENT, 2);
		Render2D.roundedRect(context, x + 14, indicatorY, 104, 28, 11, 0xFF323640);

		int categoryY = y + 76;
		for (Category category : Category.values()) {
			boolean selected = category == selectedCategory;
			boolean hovered = Render2D.hovered(mouseX, mouseY, x + 14, categoryY, 104, 28);
			float hover = animation("category:" + category.name(), hovered || selected, 0.18F);
			if (!selected && hover > 0.02F) {
				Render2D.roundedRect(context, x + 14, categoryY, 104, 28, 11, Render2D.alpha(PANEL_HOVER, (int) (122 * hover)));
			}

			int color = selected ? TEXT : Render2D.lerpColor(MUTED, TEXT, hover);
			Render2D.roundedRect(context, x + 26 + Math.round(2.0F * hover), categoryY + 12, 5, 5, 2, selected ? TEXT : Render2D.alpha(color, 150));
			Render2D.text(context, category.getTitle(), x + 43 + Math.round(2.0F * hover), categoryY + 9, color);
			categoryY += 34;
		}

		renderFooter(context, x, y, height);
	}

	private void renderFooter(DrawContext context, int x, int y, int height) {
		int enabled = (int) moduleManager.getModules().stream().filter(Module::isEnabled).count();
		int footerY = y + height - 50;
		Render2D.roundedRect(context, x + 18, footerY, 96, 28, 10, 0xD00D0D14);
		Render2D.roundedBorder(context, x + 18, footerY, 96, 28, 10, Render2D.alpha(ACCENT, 52));
		Render2D.text(context, enabled + " active", x + 29, footerY + 7, TEXT);
		Render2D.roundedRect(context, x + 85, footerY + 9, 17, 10, 5, 0xFF30343D);
	}

	private void renderModulePanel(DrawContext context, int mouseX, int mouseY, int x, int y, int panelWidth, int panelHeight) {
		Render2D.roundedRect(context, x, y, panelWidth, panelHeight, 18, 0x82101017);
		Render2D.roundedBorder(context, x, y, panelWidth, panelHeight, 18, 0x28FFFFFF);
		Render2D.text(context, selectedCategory.getTitle(), x + 24, y + 18, TEXT);
		Render2D.text(context, "modules", x + 24 + Render2D.width(selectedCategory.getTitle()) + 7, y + 18, MUTED);
		renderSearch(context, x + panelWidth - 164, y + 13);
		renderCategoryStats(context, x + 24, y + 40);

		context.enableScissor(x + 14, y + 64, x + panelWidth - 10, y + panelHeight - 12);
		List<Module> modules = moduleManager.getModules(selectedCategory);
		int startX = x + 24;
		int startY = y + 70 + Math.round(scrollAnimation.get());
		for (int index = 0; index < modules.size(); index++) {
			int column = index % 2;
			int row = index / 2;
			int moduleX = startX + column * CARD_GAP;
			int moduleY = startY + row * 70;
			float delay = Render2D.clamp(openAnimation.get() + index * 0.055F, 0.0F, 1.0F);
			int animatedY = moduleY + Math.round((1.0F - Animation.easeOutCubic(delay)) * 20.0F);
			renderModuleCard(context, mouseX, mouseY, modules.get(index), moduleX, animatedY, index);
		}
		context.disableScissor();
	}

	private void renderSearch(DrawContext context, int x, int y) {
		float shine = pulse(1.7F);
		Render2D.roundedGlow(context, x, y, 140, 28, 11, ACCENT, 1);
		Render2D.roundedRect(context, x, y, 140, 28, 11, FIELD);
		Render2D.roundedBorder(context, x, y, 140, 28, 11, Render2D.alpha(ACCENT, 58));
		Render2D.text(context, "Search", x + 16, y + 10, DARK_TEXT);
		Render2D.roundedRect(context, x + 114, y + 11, 8, 6, 3, Render2D.alpha(ACCENT, (int) (80 + shine * 45)));
	}

	private void renderCategoryStats(DrawContext context, int x, int y) {
		List<Module> modules = moduleManager.getModules(selectedCategory);
		long active = modules.stream().filter(Module::isEnabled).count();
		String label = active + "/" + modules.size() + " enabled";
		Render2D.roundedRect(context, x, y, 86, 18, 8, 0xA60D0D14);
		Render2D.roundedBorder(context, x, y, 86, 18, 8, Render2D.alpha(ACCENT, 42));
		Render2D.text(context, label, x + 10, y + 5, MUTED);
	}

	private void renderModuleCard(DrawContext context, int mouseX, int mouseY, Module module, int x, int y, int index) {
		boolean hovered = Render2D.hovered(mouseX, mouseY, x, y, CARD_WIDTH, CARD_HEIGHT);
		float hover = animation("hover:" + module.getId(), hovered, 0.2F);
		float enabled = animation("toggle:" + module.getId(), module.isEnabled(), 0.18F);
		float wave = module.isEnabled() ? pulse(index * 0.45F) : hover;
		int top = Render2D.lerpColor(Render2D.lerpColor(PANEL, PANEL_HOVER, hover), 0xFF242832, enabled * 0.42F);
		int bottom = Render2D.lerpColor(PANEL_DARK, 0xFF14171D, enabled * 0.55F);
		int drawY = y - Math.round(hover * 4.0F);
		int glowColor = Render2D.alpha(ACCENT, (int) (170 + wave * 45));
		Render2D.roundedGlow(context, x, drawY, CARD_WIDTH, CARD_HEIGHT, 15, glowColor, (int) (hover * 2.0F + enabled * 3.0F));
		Render2D.roundedVerticalGradient(context, x, drawY, CARD_WIDTH, CARD_HEIGHT, 15, top, bottom);
		Render2D.roundedBorder(context, x, drawY, CARD_WIDTH, CARD_HEIGHT, 15, Render2D.alpha(TEXT, (int) (34 + enabled * 78 + hover * 20)));
		Render2D.roundedRect(context, x + 8, drawY + 7, Math.round((CARD_WIDTH - 16) * Math.max(hover, enabled)), 2, 1, Render2D.alpha(0xFFB9BEC8, 95));

		Render2D.text(context, module.getName(), x + 13, drawY + 16, TEXT);
		Render2D.text(context, Render2D.trimToWidth(module.getDescription(), 88), x + 13, drawY + 32, Render2D.lerpColor(OFF, 0xFFE9E8F5, enabled));
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
		Render2D.roundedRect(context, x, y, 32, 14, 7, Render2D.lerpColor(0xFF262932, ACCENT_SOFT, enabled));
		Render2D.roundedBorder(context, x, y, 32, 14, 7, Render2D.alpha(TEXT, (int) (28 + enabled * 74)));
		Render2D.roundedRect(context, x + 3 + Math.round(enabled * 16.0F), y + 3, 8, 8, 4, TEXT);
	}

	private float animation(String id, boolean target, float speed) {
		Animation animation = id.startsWith("toggle:") ? toggleAnimations.computeIfAbsent(id, key -> new Animation(0.0F)) : hoverAnimations.computeIfAbsent(id, key -> new Animation(0.0F));
		animation.animate(target ? 1.0F : 0.0F, speed);
		return animation.get();
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
}
