package roguelike;

import java.util.List;

public class Chest {
    private final List<Item> loot;
    private boolean opened;

    public Chest(List<Item> loot) {
        this.loot = loot;
        this.opened = false;
    }

    public boolean isOpened() {
        return opened;
    }

    public List<Item> open() {
        if (opened) {
            return List.of();
        }
        opened = true;
        return loot;
    }
}
