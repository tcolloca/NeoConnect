package com.neopetsconnect.sdb;

import com.neopetsconnect.main.Item;

public class SdbItem extends Item {

  private final String id;
  private final String quantity;

  public SdbItem(String name, String id, String quantity) {
    super(name);
    this.id = id;
    this.quantity = quantity;
  }

  String getId() {
    return id;
  }

  String getQuantity() {
    return quantity;
  }

  @Override
  public String toString() {
    return "SdbItem [" + super.toString() + ", id=" + id + ", quantity=" + quantity + "]";
  }
}
