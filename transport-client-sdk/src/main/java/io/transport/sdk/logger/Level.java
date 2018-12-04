package io.transport.sdk.logger;

public enum Level {

	DEBUG(4), INFO(3), WARN(2), ERROR(1);

	private int value;

	private Level(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

}
