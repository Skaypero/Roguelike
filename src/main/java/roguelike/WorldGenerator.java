package roguelike;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WorldGenerator {
    private final Random random;

    private static final String[] MONSTER_NAMES = {
            "Скелет", "Гоблин", "Паук", "Теневой рыцарь", "Слизень"
    };

    private static final String[] WEAPONS = {
            "Ржавый меч", "Копьё охотника", "Боевой топор", "Кинжал"
    };

    private static final String[] CONSUMABLES = {
            "Малое зелье лечения", "Гриб здоровья", "Эликсир"
    };

    public WorldGenerator(long seed) {
        this.random = new Random(seed);
    }

    public Room generate(int roomX, int roomY) {
        TileType[][] tiles = new TileType[Room.HEIGHT][Room.WIDTH];

        for (int y = 0; y < Room.HEIGHT; y++) {
            for (int x = 0; x < Room.WIDTH; x++) {
                boolean border = x == 0 || y == 0 || x == Room.WIDTH - 1 || y == Room.HEIGHT - 1;
                tiles[y][x] = border ? TileType.WALL : TileType.FLOOR;
            }
        }

        int northDoorX = Room.WIDTH / 2;
        int southDoorX = Room.WIDTH / 2;
        int westDoorY = Room.HEIGHT / 2;
        int eastDoorY = Room.HEIGHT / 2;

        tiles[Room.HEIGHT - 1][northDoorX] = TileType.DOOR_NORTH;
        tiles[0][southDoorX] = TileType.DOOR_SOUTH;
        tiles[westDoorY][0] = TileType.DOOR_WEST;
        tiles[eastDoorY][Room.WIDTH - 1] = TileType.DOOR_EAST;

        addWallPatches(tiles);
        addTraps(tiles);

        Monster monster = null;
        if (!(roomX == 0 && roomY == 0) && random.nextDouble() < 0.7) {
            String name = MONSTER_NAMES[random.nextInt(MONSTER_NAMES.length)];
            int hp = 20 + random.nextInt(35);
            int atk = 4 + random.nextInt(10);
            monster = new Monster(name, hp, atk);
        }

        Chest chest = null;
        if (random.nextDouble() < 0.55) {
            int[] chestPos = randomFloorPosition(tiles);
            chest = new Chest(chestPos[0], chestPos[1], generateLoot());
        }

        return new Room(roomX, roomY, tiles, monster, chest);
    }

    private void addWallPatches(TileType[][] tiles) {
        int clusters = 8 + random.nextInt(5);
        for (int cluster = 0; cluster < clusters; cluster++) {
            int x = 2 + random.nextInt(Room.WIDTH - 4);
            int y = 2 + random.nextInt(Room.HEIGHT - 4);
            int length = 6 + random.nextInt(12);
            int direction = random.nextInt(4);

            for (int step = 0; step < length; step++) {
                if (isSafeForFeature(x, y) && tiles[y][x] == TileType.FLOOR) {
                    tiles[y][x] = TileType.WALL;
                    maybeThickenWall(tiles, x, y);
                }

                if (step % 2 == 0 && random.nextDouble() < 0.45) {
                    direction = (direction + (random.nextBoolean() ? 1 : 3)) % 4;
                }
                switch (direction) {
                    case 0 -> x++;
                    case 1 -> y++;
                    case 2 -> x--;
                    default -> y--;
                }
                x = Math.max(1, Math.min(Room.WIDTH - 2, x));
                y = Math.max(1, Math.min(Room.HEIGHT - 2, y));
            }
        }
    }

    private void maybeThickenWall(TileType[][] tiles, int x, int y) {
        if (random.nextDouble() < 0.55 && isSafeForFeature(x + 1, y) && tiles[y][x + 1] == TileType.FLOOR) {
            tiles[y][x + 1] = TileType.WALL;
        }
        if (random.nextDouble() < 0.55 && isSafeForFeature(x - 1, y) && tiles[y][x - 1] == TileType.FLOOR) {
            tiles[y][x - 1] = TileType.WALL;
        }
        if (random.nextDouble() < 0.35 && isSafeForFeature(x, y + 1) && tiles[y + 1][x] == TileType.FLOOR) {
            tiles[y + 1][x] = TileType.WALL;
        }
    }

    private void addTraps(TileType[][] tiles) {
        int traps = 8 + random.nextInt(7);
        for (int i = 0; i < traps; i++) {
            int x = 1 + random.nextInt(Room.WIDTH - 2);
            int y = 1 + random.nextInt(Room.HEIGHT - 2);
            if (isSafeForFeature(x, y) && tiles[y][x] == TileType.FLOOR) {
                tiles[y][x] = TileType.TRAP;
            }
        }
    }

    private boolean isSafeForFeature(int x, int y) {
        if (x <= 1 || y <= 1 || x >= Room.WIDTH - 2 || y >= Room.HEIGHT - 2) {
            return false;
        }
        int cx = Room.WIDTH / 2;
        int cy = Room.HEIGHT / 2;
        return Math.abs(x - cx) > 2 || Math.abs(y - cy) > 2;
    }

    private List<Item> generateLoot() {
        int count = 1 + random.nextInt(3);
        List<Item> loot = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            double roll = random.nextDouble();
            if (roll < 0.4) {
                loot.add(new Item(WEAPONS[random.nextInt(WEAPONS.length)], ItemType.WEAPON, 2 + random.nextInt(8)));
            } else if (roll < 0.8) {
                loot.add(new Item(CONSUMABLES[random.nextInt(CONSUMABLES.length)], ItemType.CONSUMABLE, 10 + random.nextInt(20)));
            } else {
                loot.add(new Item("Горсть золота", ItemType.TREASURE, 10 + random.nextInt(100)));
            }
        }
        return loot;
    }

    private int[] randomFloorPosition(TileType[][] tiles) {
        for (int attempt = 0; attempt < 500; attempt++) {
            int x = 1 + random.nextInt(Room.WIDTH - 2);
            int y = 1 + random.nextInt(Room.HEIGHT - 2);
            if (tiles[y][x] == TileType.FLOOR) {
                return new int[]{x, y};
            }
        }
        return new int[]{Room.WIDTH / 2, Room.HEIGHT / 2};
    }
}
