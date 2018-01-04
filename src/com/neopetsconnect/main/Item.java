package com.neopetsconnect.main;

import java.util.Optional;

public class Item {

	private final String name;
	private int amount;
	protected Optional<Integer> price;
	
	public Item(String name) {
		this(name, Optional.empty());
	}
	
	protected Item(String name, int price) {
		this(name, Optional.of(price));
	}

	public Item(String name, Optional<Integer> price) {
		super();
		this.name = name;
		this.price = price;
		this.amount = 1;
	}

	public String getName() {
		return name;
	}

	public Optional<Integer> getPrice() {
		return price;
	}
	
	public int forceGetPrice() {
		return price.get();
	}

	public int getAmount() {
		return amount;
	}
	
	public Item setAmount(int amount) {
		this.amount = amount;
		return this;
	}

	@Override
	public String toString() {
		return "Item [name=" + name + ", price=" + price  + ", amount=" + amount + "]";
	}
}
