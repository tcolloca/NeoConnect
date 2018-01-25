package com.neopetsconnect.dailies.foodclub;

public class PlayerOdds {
	
	private final int id;
	private final String name;
	private final int odds;

	public PlayerOdds(int id, String player, int odds) {
		super();
		this.id = id;
		this.name = player;
		this.odds = odds;
	}
	
	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public int getOdds() {
		return odds;
	}

	@Override
	public String toString() {
		return "PlayerOdds [id=" + id + ", player=" + name + ", odds=" + odds + "]";
	}
}
