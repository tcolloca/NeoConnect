package com.neopetsconnect.dailies.trainingschool;

public enum StatType {

  STRENGTH, DEFENCE, LEVEL, AGILITY, ENDURANCE;

  public static StatType fromName(String statTypeStr) {
    for (StatType type : values()) {
      if (type.name().equals(statTypeStr.toUpperCase())) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown stat type");
  }
}
