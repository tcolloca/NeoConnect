package com.neopetsconnect.stores;

public enum NeopetsStoreId {

	BOOKS_SHOP(7), 
	BOOKSTASTIC_BOOKS(70),
	CHOCOLATE_FACTORY(14),
	FAERIE_FOODS(39),
	KAUVARAS_MAGIC_SHOP(2),
	KAYLAS_SHOP(73),
	STAMP_OFFICE(58),
	TIKI_TACK(21);
	
	public final int id;
	
	private NeopetsStoreId(int id) {
		this.id = id;
	}

	public static NeopetsStoreId getByName(String storeName) {
		if (storeName == null) {
			throw new IllegalArgumentException("storeName is null.");
		}
		String normName = storeName.trim().replace(" ", "_").toUpperCase();
		for (NeopetsStoreId value : NeopetsStoreId.values()) {
			if (value.toString().equals(normName)) {
				return value;
			}
		}
		throw new IllegalArgumentException(storeName + " is not a valid NeopetsStoreId.");
	}
}
