package com.neopetsconnect.inventory;

import com.neopetsconnect.main.Item;

public class InventoryItem extends Item {

  private final boolean isBlocked;

  InventoryItem(String name, boolean blocked) {
    super(name);
    this.isBlocked = blocked;
  }

  public boolean isBlocked() {
    return isBlocked;
  }

  @Override
  public String toString() {
    return "InventoryItem [" + super.toString() + ", isBlocked=" + isBlocked + "]";
  }
}
