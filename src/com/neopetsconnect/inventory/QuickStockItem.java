package com.neopetsconnect.inventory;

import com.neopetsconnect.main.Item;

public class QuickStockItem extends Item {

	private final String id;
	private final String rowId;
	
	public QuickStockItem(String name, String id, String rowId) {
		super(name);
		this.id = id;
		this.rowId = rowId;
	}

	String getId() {
		return id;
	}

	String getRowId() {
		return rowId;
	}
	
	@Override
	public String toString() {
		return "QuickStockItem [" + super.toString() + ", id=" + id + ", rowId=" + rowId + "]";
	}
}
