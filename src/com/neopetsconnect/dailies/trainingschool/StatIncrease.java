package com.neopetsconnect.dailies.trainingschool;

public class StatIncrease {

  private final StatType statType;
  private final int amount;

  public StatIncrease(StatType statType, int amount) {
    super();
    this.statType = statType;
    this.amount = amount;
  }

  public StatType getStatType() {
    return statType;
  }

  public int getAmount() {
    return amount;
  }

  @Override
  public String toString() {
    return "StatIncrease [statType=" + statType + ", amount=" + amount + "]";
  }
}
