package com.neopetsconnect.dailies.status;

public class NoneStatus implements Status {

  @Override
  public boolean equals(Object o) {
    return o instanceof NoneStatus;
  }
}
