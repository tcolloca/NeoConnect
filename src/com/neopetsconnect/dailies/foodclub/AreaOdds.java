package com.neopetsconnect.dailies.foodclub;

import java.util.List;

public class AreaOdds {
	
	private final int id;
	private final String name;
	private final List<PlayerOdds> playerOdds;
	
	public AreaOdds(int id, String name, List<PlayerOdds> playerOdds) {
		super();
		this.id = id;
		this.name = name;
		this.playerOdds = playerOdds;
	}
	
	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public PlayerOdds getPlayerOdds(String playerName) {
		return playerOdds.stream().filter(playerOdds -> playerOdds.getName().equals(playerName)).findFirst().get();
	}
	
	@Override
	public String toString() {
		return "AreaOdds [id=" + id + ", name=" + name + ", playerOdds=" + playerOdds + "]";
	}
}