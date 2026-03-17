package roguelike;

import java.util.List;

public class Room {
    public static final int WIDTH = 40;
    public static final int HEIGHT = 40;

    private final int roomX;
    private final int roomY;
    private final TileType[][] tiles;
    private final List<MonsterInstance> monsters;
    private final List<Chest> chests;

    public Room(int roomX, int roomY, TileType[][] tiles, List<MonsterInstance> monsters, List<Chest> chests) {
        this.roomX = roomX;
        this.roomY = roomY;
        this.tiles = tiles;
        this.monsters = monsters;
        this.chests = chests;
    }

    public int roomX() {
        return roomX;
    }

    public int roomY() {
        return roomY;
    }

    public TileType tile(int x, int y) {
        return tiles[y][x];
    }

    public TileType[][] tiles() {
        return tiles;
    }

    public List<MonsterInstance> monsters() {
        return monsters;
    }

    public List<Chest> chests() {
        return chests;
    }

    public Monster monster() {
        return monsters.isEmpty() ? null : monsters.get(0).monster();
    }

    public Chest chest() {
        return chests.isEmpty() ? null : chests.get(0);
    }

    public boolean isWalkable(int x, int y) {
        if (x < 0 || y < 0 || x >= WIDTH || y >= HEIGHT) {
            return false;
        }
        TileType t = tile(x, y);
        return t != TileType.WALL;
    }
}
