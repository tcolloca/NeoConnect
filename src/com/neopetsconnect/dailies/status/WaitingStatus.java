package com.neopetsconnect.dailies.status;

import java.time.LocalTime;

public class WaitingStatus implements Status {

  private final LocalTime waitTime;

  public WaitingStatus(LocalTime waitTime) {
    super();
    this.waitTime = waitTime;
  }

  public LocalTime getWaitTime() {
    return waitTime;
  }

  @Override
  public String toString() {
    return "WaitingStatus [waitTime=" + waitTime.toString() + "]";
  }
}
