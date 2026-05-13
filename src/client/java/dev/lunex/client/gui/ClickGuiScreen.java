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
	private static final int GUI_WIDTH = 448;
	private static final int GUI_HEIGHT = 274;
	private static final int SIDEBAR_WIDTH = 116;
	private static final int CARD_WIDTH = 136;
	private static final int CARD_HEIGHT = 48;
	private static final int BACKGROUND = 0xF014141B;
	private static final int PANEL = 0xF01B1B24;
	private static final int PANEL_DARK = 0xF00D0D12;
	private static final int PANEL_HOVER = 0xFF282836;
	private static final int ACCENT = 0xFFFF78DD;
	private static final int ACCENT_2 = 0xFF9E6CFF;
	private static final int ACCENT_3 = 0xFF58E6FF;
	private static final int TEXT = 0xFFF8F8F8;
	private static final int MUTED = 0xFF92929E;
	private static final int OFF = 0xFF5A5A66;

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
		openAnimation.animate(1.0F, 0.18F);
		scrollAnimation.animate(targetScroll, 0.28F);
		categorySlide.animate(categoryIndex(selectedCategory), 0.22F);
		renderBackground(context, mouseX, mouseY, delta);

		float open = Animation.easeOutBack(openAnimation.get());
		int x = (width - GUI_WIDTH) / 2;
		int y = (height - GUI_HEIGHT) / 2;
		MatrixStack matrices = context.getMatrices();
		matrices.push();
		matrices.translate(x + GUI_WIDTH / 2.0F, y + GUI_HEIGHT / 2.0F, 0.0F);
		matrices.scale(0.86F + open * 0.14F, 0.86F + open * 0.14F, 1.0F);
		matrices.translate(-(x + GUI_WIDTH / 2.0F), -(y + GUI_HEIGHT / 2.0F), 0.0F);

		int alpha = (int) (235 * Render2D.clamp(openAnimation.get(), 0.0F, 1.0F));
		int animatedAccent = animatedAccent();
		Render2D.glow(context, x, y, GUI_WIDTH, GUI_HEIGHT, animatedAccent, 7);
		Render2D.verticalGradient(context, x, y, GUI_WIDTH, GUI_HEIGHT, Render2D.alpha(BACKGROUND, alpha), Render2D.alpha(PANEL_DARK, alpha));
		Render2D.border(context, x, y, GUI_WIDTH, GUI_HEIGHT, Render2D.alpha(animatedAccent, 120));
		Render2D.horizontalGradient(context, x + 1, y + 1, GUI_WIDTH - 2, 2, ACCENT, ACCENT_3);

		renderSidebar(context, mouseX, mouseY, x, y, GUI_HEIGHT, animatedAccent);
		renderModulePanel(context, mouseX, mouseY, x + SIDEBAR_WIDTH, y + 12, GUI_WIDTH - SIDEBAR_WIDTH - 12, GUI_HEIGHT - 24, animatedAccent);
		matrices.pop();
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		int x = (width - GUI_WIDTH) / 2;
		int y = (height - GUI_HEIGHT) / 2;

		int categoryY = y + 58;
		for (Category category : Category.values()) {
			if (Render2D.hovered(mouseX, mouseY, x + 12, categoryY, 92, 24)) {
				selectedCategory = category;
				targetScroll = 0;
				return true;
			}
			categoryY += 29;
		}

		List<Module> modules = moduleManager.getModules(selectedCategory);
		int startX = x + SIDEBAR_WIDTH + 22;
		int startY = y + 60 + Math.round(scrollAnimation.get());
		for (int index = 0; index < modules.size(); index++) {
			int column = index % 2;
			int row = index / 2;
			int moduleX = startX + column * 148;
			int moduleY = startY + row * 58;
			if (Render2D.hovered(mouseX, mouseY, moduleX, moduleY, CARD_WIDTH, CARD_HEIGHT)) {
				modules.get(index).toggle();
				return true;
			}
		}

		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		targetScroll += (int) (verticalAmount * 20.0D);
		targetScroll = Math.min(0, Math.max(targetScroll, -160));
		return true;
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	private void renderSidebar(DrawContext context, int mouseX, int mouseY, int x, int y, int height, int accent) {
		Render2D.rect(context, x, y, SIDEBAR_WIDTH, height, 0xE9181821);
		Render2D.glow(context, x + 14, y + 15, 20, 20, accent, 4);
		Render2D.verticalGradient(context, x + 12, y + 13, 24, 24, ACCENT, ACCENT_2);
		Render2D.text(context, "☾", x + 20, y + 21, TEXT);
		Render2D.scaledText(context, Lunex.NAME, x + 42, y + 16, 1.25F, TEXT);
		Render2D.text(context, "private client", x + 43, y + 30, MUTED);
		Render2D.horizontalGradient(context, x + 12, y + 44, 92, 1, ACCENT, ACCENT_3);

		int indicatorY = y + 58 + Math.round(categorySlide.get() * 29.0F);
		Render2D.glow(context, x + 12, indicatorY, 92, 24, accent, 3);
		Render2D.horizontalGradient(context, x + 12, indicatorY, 92, 24, Render2D.alpha(ACCENT, 210), Render2D.alpha(ACCENT_2, 190));

		int categoryY = y + 58;
		for (Category category : Category.values()) {
			boolean selected = category == selectedCategory;
			boolean hovered = Render2D.hovered(mouseX, mouseY, x + 12, categoryY, 92, 24);
			float hover = animation("category:" + category.name(), hovered || selected, 0.18F);
			if (!selected && hover > 0.02F) {
				Render2D.rect(context, x + 12, categoryY, 92, 24, Render2D.alpha(PANEL_HOVER, (int) (130 * hover)));
			}

			int color = selected ? TEXT : Render2D.lerpColor(MUTED, TEXT, hover);
			Render2D.text(context, icon(category), x + 20 + Math.round(2.0F * hover), categoryY + 8, color);
			Render2D.text(context, category.getTitle(), x + 40 + Math.round(2.0F * hover), categoryY + 8, color);
			categoryY += 29;
		}
	}

	private void renderModulePanel(DrawContext context, int mouseX, int mouseY, int x, int y, int width, int height, int accent) {
		Render2D.rect(context, x, y, width, height, 0x9C101016);
		Render2D.border(context, x, y, width, height, 0x22000000);
		Render2D.glow(context, x + 14, y + 13, width - 28, 24, accent, 2);
		Render2D.rect(context, x + 14, y + 13, width - 28, 24, 0xFF0B0B10);
		Render2D.text(context, "⌕", x + 23, y + 21, MUTED);
		Render2D.text(context, "Search modules", x + 42, y + 21, 0xFF5E5E66);
		Render2D.text(context, "⚙", x + width - 27, y + 21, Render2D.lerpColor(MUTED, accent, pulse()));

		context.enableScissor(x + 12, y + 45, x + width - 8, y + height - 8);
		List<Module> modules = moduleManager.getModules(selectedCategory);
		int startX = x + 18;
		int startY = y + 48 + Math.round(scrollAnimation.get());
		for (int index = 0; index < modules.size(); index++) {
			int column = index % 2;
			int row = index / 2;
			int moduleX = startX + column * 148;
			int moduleY = startY + row * 58;
			float delay = Math.min(1.0F, openAnimation.get() + index * 0.04F);
			renderModuleCard(context, mouseX, mouseY, modules.get(index), moduleX, moduleY + Math.round((1.0F - Animation.easeOutCubic(delay)) * 16.0F), index, accent);
		}
		context.disableScissor();
	}

	private void renderModuleCard(DrawContext context, int mouseX, int mouseY, Module module, int x, int y, int index, int accent) {
		boolean hovered = Render2D.hovered(mouseX, mouseY, x, y, CARD_WIDTH, CARD_HEIGHT);
		float hover = animation("hover:" + module.getId(), hovered, 0.2F);
		float enabled = animation("toggle:" + module.getId(), module.isEnabled(), 0.18F);
		float pulse = module.isEnabled() ? pulse(index * 0.4F) : 0.0F;
		int top = Render2D.lerpColor(Render2D.lerpColor(PANEL, PANEL_HOVER, hover), ACCENT, enabled);
		int bottom = Render2D.lerpColor(PANEL_DARK, ACCENT_2, enabled);
		int drawY = y - Math.round(hover * 3.0F);
		Render2D.glow(context, x, drawY, CARD_WIDTH, CARD_HEIGHT, Render2D.lerpColor(accent, ACCENT_3, pulse), (int) (hover * 4.0F + enabled * 5.0F));
		Render2D.verticalGradient(context, x, drawY, CARD_WIDTH, CARD_HEIGHT, top, bottom);
		Render2D.border(context, x, drawY, CARD_WIDTH, CARD_HEIGHT, Render2D.alpha(TEXT, (int) (36 + enabled * 80)));
		Render2D.horizontalGradient(context, x + 1, drawY + 1, Math.round((CARD_WIDTH - 2) * Math.max(hover, enabled)), 2, ACCENT, ACCENT_3);
		Render2D.text(context, module.getName(), x + 9, drawY + 8, TEXT);
		Render2D.text(context, trim(module.getDescription(), 24), x + 9, drawY + 24, Render2D.lerpColor(OFF, 0xFFEFEFFF, enabled));
		renderToggle(context, x + CARD_WIDTH - 32, drawY + 9, enabled);
	}

	private void renderToggle(DrawContext context, int x, int y, float enabled) {
		Render2D.rect(context, x, y, 22, 10, Render2D.lerpColor(0xFF343440, ACCENT_2, enabled));
		Render2D.rect(context, x + 2 + Math.round(enabled * 10.0F), y + 2, 6, 6, TEXT);
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

	private String trim(String text, int limit) {
		if (text.length() <= limit) {
			return text;
		}

		return text.substring(0, limit - 1) + "…";
	}
}
