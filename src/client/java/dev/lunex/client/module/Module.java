package dev.lunex.client.module;

import dev.lunex.client.config.ClientConfig;

import net.minecraft.client.MinecraftClient;

public abstract class Module {
	private final String id;
	private final String name;
	private final String description;
	private final Category category;
	private final boolean defaultEnabled;
	private final ClientConfig config;
	private boolean enabled;

	protected Module(String id, String name, String description, Category category, boolean defaultEnabled, ClientConfig config) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.category = category;
		this.defaultEnabled = defaultEnabled;
		this.config = config;
		this.enabled = defaultEnabled;
	}

	public final void loadState() {
		enabled = config.isModuleEnabled(id, defaultEnabled);
		if (enabled) {
			onEnable();
		} else {
			onDisable();
		}
	}

	public final void toggle() {
		setEnabled(!enabled);
	}

	public final void setEnabled(boolean enabled) {
		if (this.enabled == enabled) {
			return;
		}

		this.enabled = enabled;
		config.setModuleEnabled(id, enabled);

		if (enabled) {
			onEnable();
		} else {
			onDisable();
		}
	}

	public void tick(MinecraftClient client) {
	}

	protected void onEnable() {
	}

	protected void onDisable() {
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public Category getCategory() {
		return category;
	}

	public boolean isEnabled() {
		return enabled;
	}
}
