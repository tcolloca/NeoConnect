package com.neopetsconnect.dailies.foodclub;

import java.util.List;

public class FoodClubOdds {

	private final int maxBet;
	private final List<AreaOdds> areaOdds;

	public FoodClubOdds(int maxBet, List<AreaOdds> areaOdds) {
		super();
		this.maxBet = maxBet;
		this.areaOdds = areaOdds;
	}
	
	public int getMaxBet() {
		return maxBet;
	}

	public AreaOdds getAreaOdds(String areaName) {
		return areaOdds.stream().filter(area -> area.getName().equals(areaName)).findFirst().get();
	}
}
