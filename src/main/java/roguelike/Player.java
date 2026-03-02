package roguelike;

import java.util.ArrayList;
import java.util.List;

public class Player {
    private int health = 100;
    private final List<Item> inventory = new ArrayList<>();
    private int selectedItemIndex = -1;

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
        if (selectedItemIndex < 0 && !inventory.isEmpty()) {
            selectedItemIndex = 0;
        }
    }

    public List<Item> inventory() {
        return List.copyOf(inventory);
    }

    public int selectedItemIndex() {
        return selectedItemIndex;
    }

    public Item selectedItem() {
        if (selectedItemIndex < 0 || selectedItemIndex >= inventory.size()) {
            return null;
        }
        return inventory.get(selectedItemIndex);
    }

    public void selectNextItem() {
        if (inventory.isEmpty()) {
            selectedItemIndex = -1;
            return;
        }
        selectedItemIndex = (selectedItemIndex + 1 + inventory.size()) % inventory.size();
    }

    public void selectPreviousItem() {
        if (inventory.isEmpty()) {
            selectedItemIndex = -1;
            return;
        }
        selectedItemIndex = (selectedItemIndex - 1 + inventory.size()) % inventory.size();
    }

    public int attackPower() {
        int base = 10;
        Item selected = selectedItem();
        if (selected != null && selected.type() == ItemType.WEAPON) {
            return base + selected.power();
        }
        return base;
    }

    public boolean useSelectedConsumable() {
        Item selected = selectedItem();
        if (selected == null || selected.type() != ItemType.CONSUMABLE) {
            return false;
        }

        heal(selected.power());
        inventory.remove(selectedItemIndex);
        normalizeSelectionAfterRemoval();
        return true;
    }

    public boolean usePotionIfAny() {
        for (int i = 0; i < inventory.size(); i++) {
            Item item = inventory.get(i);
            if (item.type() == ItemType.CONSUMABLE) {
                heal(item.power());
                inventory.remove(i);
                if (i <= selectedItemIndex) {
                    selectedItemIndex--;
                }
                normalizeSelectionAfterRemoval();
                return true;
            }
        }
        return false;
    }

    private void normalizeSelectionAfterRemoval() {
        if (inventory.isEmpty()) {
            selectedItemIndex = -1;
            return;
        }
        if (selectedItemIndex < 0) {
            selectedItemIndex = 0;
        }
        if (selectedItemIndex >= inventory.size()) {
            selectedItemIndex = inventory.size() - 1;
        }
    }
}
