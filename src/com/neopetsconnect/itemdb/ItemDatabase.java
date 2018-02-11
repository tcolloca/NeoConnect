package com.neopetsconnect.itemdb;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import com.neopetsconnect.main.Item;

public class ItemDatabase {

  private final Map<String, Item> itemDb;
  private static ItemDatabase instance;
  private static String dbPath = "database/items.txt";

  public static void main(String[] args) {
    ItemDatabase itemDatabase = ItemDatabase.getInstance();
    itemDatabase.addItems(JellyneoItemDatabase.getInstance().query("choco"));
    itemDatabase.save();
  }

  public synchronized static ItemDatabase getInstance() {
    if (instance == null) {
      instance = new ItemDatabase();
    }
    return instance;
  }

  private ItemDatabase() {
    itemDb = new HashMap<>();
    loadItems();
  }

  public Item getItem(String name) {
    return itemDb.get(name);
  }

  public void addItems(List<Item> items) {
    items.forEach(item -> itemDb.put(item.getName(), item));
  }

  public void save() {
    String itemsStr = String.join("\r\n",
        itemDb.entrySet().stream()
            .map(e -> e.getKey() + ";" + e.getValue().getPrice().map(Object::toString).orElse(""))
            .collect(Collectors.toList()));
    Path path = Paths.get(dbPath);
    try {
      if (Files.notExists(path.getParent())) {
        Files.createDirectory(path.getParent());
      }
      Files.write(path, itemsStr.getBytes(), StandardOpenOption.TRUNCATE_EXISTING,
          StandardOpenOption.CREATE);
    } catch (IOException e) {
      throw new RuntimeException("Error while saving item database", e);
    }
  }

  private void loadItems() {
    Path path = Paths.get(dbPath);
    if (Files.exists(path)) {
      try {
        List<Item> items = Files.readAllLines(path).stream().map(line -> {
          String[] itemInfo = line.split(";");
          String name = itemInfo[0].trim();
          Optional<Integer> price = Optional.empty();
          if (itemInfo.length >= 2 && !itemInfo[1].trim().isEmpty()) {
            price = Optional.of(Integer.valueOf(itemInfo[1]));
          }
          return new Item(name, price);
        }).collect(Collectors.toList());
        addItems(items);
      } catch (IOException e) {
        throw new RuntimeException("Error while loading item database", e);
      }
    }
  }
}
