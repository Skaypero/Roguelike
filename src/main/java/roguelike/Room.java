package roguelike;

public class Room {
    public static final int WIDTH = 40;
    public static final int HEIGHT = 40;

    private final int roomX;
    private final int roomY;
    private final TileType[][] tiles;
    private final Monster monster;
    private final Chest chest;

    public Room(int roomX, int roomY, TileType[][] tiles, Monster monster, Chest chest) {
        this.roomX = roomX;
        this.roomY = roomY;
        this.tiles = tiles;
        this.monster = monster;
        this.chest = chest;
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

    public Monster monster() {
        return monster;
    }

    public Chest chest() {
        return chest;
    }

    public boolean isWalkable(int x, int y) {
        if (x < 0 || y < 0 || x >= WIDTH || y >= HEIGHT) {
            return false;
        }
        TileType t = tile(x, y);
        return t != TileType.WALL;
    }
}
