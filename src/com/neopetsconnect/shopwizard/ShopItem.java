package com.neopetsconnect.shopwizard;

import com.neopetsconnect.main.Item;

public class ShopItem extends Item {

  private final String link;
  private final int available;

  public ShopItem(String name, int cost, String link, int available) {
    super(name, cost);
    if (!link.startsWith("/")) {
      link = "/" + link;
    }
    this.link = link;
    this.available = available;
  }

  public String getLink() {
    return link;
  }

  public int getAvailable() {
    return available;
  }

  @Override
  public String toString() {
    return "ShopItem [" + super.toString() + ", link=" + link + "]";
  }
}
