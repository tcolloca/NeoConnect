package com.neopetsconnect.dailies.foodclub;

import java.util.List;

public class BetInfo {

  private List<AreaInfo> areaInfos;

  public BetInfo(List<AreaInfo> areaInfos) {
    super();
    this.areaInfos = areaInfos;
  }

  public List<AreaInfo> getAreaInfos() {
    return areaInfos;
  }

  @Override
  public String toString() {
    return "BetInfo {" + areaInfos + "}";
  }
}
