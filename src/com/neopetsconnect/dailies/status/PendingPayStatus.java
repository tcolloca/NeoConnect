package com.neopetsconnect.dailies.status;

import java.util.List;

import com.neopetsconnect.main.Item;

public class PendingPayStatus implements Status {

	private final List<Item> items;
	private final String key;

	public PendingPayStatus(List<Item> items) {
		super();
		this.items = items;
		this.key = null;
	}

	public PendingPayStatus(List<Item> items, String key) {
		this.items = items;
		this.key = key;
	}

	public List<Item> getItems() {
		return items;
	}

	public String getKey() {
		return key;
	}
}
