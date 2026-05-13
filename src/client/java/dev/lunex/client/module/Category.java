package dev.lunex.client.module;

public enum Category {
	COMBAT("Combat"),
	MOVEMENT("Movement"),
	VISUAL("Visual"),
	MISC("Misc");

	private final String title;

	Category(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}
}
