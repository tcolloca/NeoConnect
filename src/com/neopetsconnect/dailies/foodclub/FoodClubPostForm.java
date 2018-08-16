package com.neopetsconnect.dailies.foodclub;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.httphelper.main.Parameter;

public class FoodClubPostForm {

  private final FoodClubOdds foodClubOdds;
  private final BetInfo betInfo;

  private List<Integer> areaIds;
  private HashMap<Integer, Integer> winnerIds;
  private int totalOdds;

  public FoodClubPostForm(FoodClubOdds foodClubOdds, BetInfo betInfo) {
    super();
    this.foodClubOdds = foodClubOdds;
    this.betInfo = betInfo;
    process();
  }

  private void process() {
    areaIds = new ArrayList<>();
    winnerIds = new HashMap<>();
    totalOdds = 1;
    for (AreaInfo areaInfo : betInfo.getAreaInfos()) {
      AreaOdds areaOdds = foodClubOdds.getAreaOdds(areaInfo.getName());
      areaIds.add(areaOdds.getId());
      PlayerOdds playerOdds = areaOdds.getPlayerOdds(areaInfo.getPlayer());
      winnerIds.put(areaOdds.getId(), playerOdds.getId());
      totalOdds *= playerOdds.getOdds();
    }
  }

  public List<Parameter> toFormParameters() {
    List<Parameter> formParams = new ArrayList<>();

    for (int i = 1; i <= 5; i++) {
      if (areaIds.contains(i)) {
        formParams.add(new Parameter("matches[]", String.valueOf(i)));
      }
      Integer areaWinnerId = winnerIds.get(i);
      String winnerIdStr = "";
      if (areaWinnerId != null) {
        winnerIdStr = String.valueOf(areaWinnerId);
      }
      formParams.add(new Parameter("winner" + i, winnerIdStr));
    }
    formParams.add(new Parameter("bet_amount", String.valueOf(foodClubOdds.getMaxBet())));
    formParams.add(new Parameter("total_odds", String.valueOf(totalOdds) + ":1"));
    formParams.add(new Parameter("winnings", String.valueOf(totalOdds * foodClubOdds.getMaxBet())));
    formParams.add(new Parameter("type", "bet"));
    return formParams;
  }
}
