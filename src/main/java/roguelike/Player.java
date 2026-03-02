package roguelike;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Player {
    private int health = 100;
    private final List<Item> inventory = new ArrayList<>();

    public int health() {
        return health;
    }

    public boolean isAlive() {
        return health > 0;
    }

    public void takeDamage(int value) {
        health -= value;
    }

    public void heal(int value) {
        health = Math.min(100, health + value);
    }

    public void addItems(List<Item> items) {
        inventory.addAll(items);
    }

    public List<Item> inventory() {
        return List.copyOf(inventory);
    }

    public int attackPower() {
        int base = 10;
        int bonus = inventory.stream()
                .filter(item -> item.type() == ItemType.WEAPON)
                .map(Item::power)
                .max(Comparator.naturalOrder())
                .orElse(0);
        return base + bonus;
    }

    public boolean usePotionIfAny() {
        for (int i = 0; i < inventory.size(); i++) {
            Item item = inventory.get(i);
            if (item.type() == ItemType.CONSUMABLE) {
                heal(item.power());
                inventory.remove(i);
                return true;
            }
        }
        return false;
    }
}
