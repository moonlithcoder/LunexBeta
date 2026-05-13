package dev.lunex.client.module;

import dev.lunex.client.config.ClientConfig;
import dev.lunex.client.module.impl.CoordinatesModule;
import dev.lunex.client.module.impl.FullbrightModule;
import dev.lunex.client.module.impl.SprintModule;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class ModuleManager {
	public static final String KEY_CATEGORY = "key.category.lunex";

	private final ClientConfig config;
	private final List<Module> modules = new ArrayList<>();

	public ModuleManager(ClientConfig config) {
		this.config = config;
	}

	public void registerDefaults() {
		register(new SprintModule(config, key("key.lunex.toggle_sprint", GLFW.GLFW_KEY_R)));
		register(new FullbrightModule(config, key("key.lunex.toggle_fullbright", GLFW.GLFW_KEY_B)));
		register(new CoordinatesModule(config, key("key.lunex.toggle_coordinates", GLFW.GLFW_KEY_C)));
		modules.forEach(Module::loadState);
	}

	public void handleKeybinds() {
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

	public List<Module> getModules() {
		return Collections.unmodifiableList(modules);
	}

	public Optional<Module> findById(String id) {
		return modules.stream().filter(module -> module.getId().equals(id)).findFirst();
	}

	private void register(Module module) {
		modules.add(module);
	}

	private static KeyBinding key(String translationKey, int code) {
		return KeyBindingHelper.registerKeyBinding(new KeyBinding(translationKey, InputUtil.Type.KEYSYM, code, KEY_CATEGORY));
	}
}
