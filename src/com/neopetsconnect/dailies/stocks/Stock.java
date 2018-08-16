package com.neopetsconnect.dailies.stocks;

public class Stock {

  private final int id;
  private final String tickerName;
  private final int curr;
  private final double change;
  private final int shares;
  private final String formName;

  public Stock(int id, String tickerName, int curr) {
    this(id, tickerName, curr, 0, 0, null);
  }

  public Stock(int shares, double change, String formName) {
    this(0, null, 0, change, shares, formName);
  }

  public Stock(int id, String tickerName, int curr, double change, int shares, String formName) {
    this.id = id;
    this.tickerName = tickerName;
    this.curr = curr;
    this.change = change;
    this.shares = shares;
    this.formName = formName;
  }

  public int getId() {
    return id;
  }

  public String getTickerName() {
    return tickerName;
  }

  public int getCurr() {
    return curr;
  }

  public double getChange() {
    return change;
  }

  public int getShares() {
    return shares;
  }

  public String getFormName() {
    return formName;
  }

  @Override
  public String toString() {
    return "Stock [id=" + id + ", tickerName=" + tickerName + ", curr=" + curr + ", change="
        + change + ", shares=" + shares + ", formName=" + formName + "]";
  }
}
