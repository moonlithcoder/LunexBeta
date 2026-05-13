package dev.lunex.client.module;

import dev.lunex.client.config.ClientConfig;
import dev.lunex.client.gui.ClickGuiScreen;
import dev.lunex.client.module.impl.AuraModule;
import dev.lunex.client.module.impl.CoordinatesModule;
import dev.lunex.client.module.impl.FullbrightModule;
import dev.lunex.client.module.impl.SprintModule;
import dev.lunex.client.module.impl.TargetEspModule;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.LivingEntity;

import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class ModuleManager {
	public static final String KEY_CATEGORY = "key.category.lunex";

	private final ClientConfig config;
	private final List<Module> modules = new ArrayList<>();
	private final KeyBinding clickGuiKey = key("key.lunex.toggle_clickgui", GLFW.GLFW_KEY_RIGHT_SHIFT);
	private AuraModule auraModule;
	private TargetEspModule targetEspModule;

	public ModuleManager(ClientConfig config) {
		this.config = config;
	}

	public void registerDefaults() {
		auraModule = new AuraModule(config, key("key.lunex.toggle_aura", GLFW.GLFW_KEY_G));
		targetEspModule = new TargetEspModule(config, key("key.lunex.toggle_targetesp", GLFW.GLFW_KEY_V));
		register(auraModule);
		register(new SprintModule(config, key("key.lunex.toggle_sprint", GLFW.GLFW_KEY_R)));
		register(targetEspModule);
		register(new FullbrightModule(config, key("key.lunex.toggle_fullbright", GLFW.GLFW_KEY_B)));
		register(new CoordinatesModule(config, key("key.lunex.toggle_coordinates", GLFW.GLFW_KEY_C)));
		modules.forEach(Module::loadState);
	}

	public void handleKeybinds() {
		MinecraftClient client = MinecraftClient.getInstance();
		while (clickGuiKey.wasPressed()) {
			client.setScreen(new ClickGuiScreen(this));
		}

		for (Module module : modules) {
			while (module.getKeyBinding().wasPressed()) {
				module.toggle();
			}
		}
	}

	public void tick(MinecraftClient client) {
		for (Module module : modules) {
			if (module.isEnabled()) {
				module.tick(client);
			}
		}
	}

	public void renderWorld(WorldRenderContext context) {
		if (targetEspModule != null && targetEspModule.isEnabled()) {
			targetEspModule.render(context, this::getAuraTarget);
		}
	}

	public List<Module> getModules() {
		return Collections.unmodifiableList(modules);
	}

	public List<Module> getModules(Category category) {
		return modules.stream().filter(module -> module.getCategory() == category).toList();
	}

	public Optional<Module> findById(String id) {
		return modules.stream().filter(module -> module.getId().equals(id)).findFirst();
	}

	private LivingEntity getAuraTarget() {
		if (auraModule == null || !auraModule.isEnabled()) {
			return null;
		}

		return auraModule.getTarget();
	}

	private void register(Module module) {
		modules.add(module);
	}

	private static KeyBinding key(String translationKey, int code) {
		return KeyBindingHelper.registerKeyBinding(new KeyBinding(translationKey, InputUtil.Type.KEYSYM, code, KEY_CATEGORY));
	}
}
