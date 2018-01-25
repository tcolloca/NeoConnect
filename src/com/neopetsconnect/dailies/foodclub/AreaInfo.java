package com.neopetsconnect.dailies.foodclub;

public class AreaInfo {
	
	private String name;
	private String player;
	
	public AreaInfo(String area, String player) {
		super();
		this.name = area;
		this.player = player;
	}

	public String getName() {
		return name;
	}

	public String getPlayer() {
		return player;
	}

	@Override
	public String toString() {
		return "[" + name + ": " + player + "]";
	}
}
