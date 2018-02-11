package com.neopetsconnect.dailies.status;

public class CompletedStatus implements Status {

  @Override
  public boolean equals(Object o) {
    return o instanceof CompletedStatus;
  }
}
