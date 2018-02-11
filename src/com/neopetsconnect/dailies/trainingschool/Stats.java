package com.neopetsconnect.dailies.trainingschool;

public class Stats {

  private final int level;
  private final int health;
  private final int strength;
  private final int defence;
  private final int move;

  public static Builder newBuilder() {
    return new Builder();
  }

  private Stats(int level, int health, int strength, int defence, int move) {
    super();
    this.level = level;
    this.health = health;
    this.strength = strength;
    this.defence = defence;
    this.move = move;
  }

  public int getLevel() {
    return level;
  }

  public int getHealth() {
    return health;
  }

  public int getStrength() {
    return strength;
  }

  public int getDefence() {
    return defence;
  }

  public int getMove() {
    return move;
  }

  @Override
  public String toString() {
    return "Stats [level=" + level + ", health=" + health + ", strength=" + strength + ", defence="
        + defence + ", move=" + move + "]";
  }

  public static class Builder {

    private int level;
    private int health;
    private int strength;
    private int defence;
    private int move;

    public Builder setLevel(int level) {
      this.level = level;
      return this;
    }

    public Builder setHealth(int health) {
      this.health = health;
      return this;
    }

    public Builder setStrength(int strength) {
      this.strength = strength;
      return this;
    }

    public Builder setDefence(int defence) {
      this.defence = defence;
      return this;
    }

    public Builder setMove(int move) {
      this.move = move;
      return this;
    }

    public Stats build() {
      return new Stats(level, health, strength, defence, move);
    }
  }
}
