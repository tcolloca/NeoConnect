package com.neopetsconnect.myshop;

import java.util.Optional;
import com.neopetsconnect.main.Item;

public class MyShopItem extends Item {

  private String id;
  private String rowId;
  private int pageIndex;
  private int oldCost;

  public MyShopItem(String id, String rowId, String name, int price, int pageIndex) {
    super(name, price);
    this.id = id;
    this.rowId = rowId;
    this.oldCost = price;
    this.pageIndex = pageIndex;
  }

  String getId() {
    return id;
  }

  String getRowId() {
    return rowId;
  }

  int getPageIndex() {
    return pageIndex;
  }

  int getOldCost() {
    return oldCost;
  }

  void changePrice(int price) {
    super.price = Optional.of(price);
  }

  @Override
  public String toString() {
    return "MyShopItem [" + super.toString() + ", id=" + id + ", rowId=" + rowId + ", oldCost="
        + oldCost + ", pageIndex=" + pageIndex + "]";
  }

  public void changePrice(int price, double percent) {
    changePrice((int) (price * percent));
  }
}
