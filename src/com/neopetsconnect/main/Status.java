package com.neopetsconnect.main;

public enum Status {

  ON, PAUSED, STOPPED, OFF;

  public static Status get(String name) {
    for (Status value : values()) {
      if (value.name().equals(name.toUpperCase())) {
        return value;
      }
    }
    throw new IllegalArgumentException("Uknown status: " + name);
  }
  
  public boolean isStoppedOrOff() {
    return this.equals(STOPPED) || this.equals(OFF);
  }
}
