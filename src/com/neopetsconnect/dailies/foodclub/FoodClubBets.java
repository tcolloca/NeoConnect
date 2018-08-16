package com.neopetsconnect.dailies.foodclub;

import java.time.LocalDate;
import java.util.List;

public class FoodClubBets {

  private LocalDate date;
  private List<BetInfo> betInfos;

  public FoodClubBets(LocalDate date, List<BetInfo> betInfos) {
    this.date = date;
    this.betInfos = betInfos;
  }

  public LocalDate getDate() {
    return date;
  }

  public List<BetInfo> getBetInfos() {
    return betInfos;
  }
}
