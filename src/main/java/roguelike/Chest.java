package roguelike;

import java.util.List;

public class Chest {
    private final int x;
    private final int y;
    private final List<Item> loot;
    private boolean opened;

    public Chest(int x, int y, List<Item> loot) {
        this.x = x;
        this.y = y;
        this.loot = loot;
        this.opened = false;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
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
