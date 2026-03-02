package roguelike;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WorldGenerator {
    public enum GenerationMode {
        RANDOM,
        PREDEFINED
    }

    private final Random random;
    private GenerationMode mode = GenerationMode.RANDOM;

    private static final String[] MONSTER_NAMES = {
            "Skeleton", "Goblin", "Spider", "Shadow Knight", "Slime", "Rat"
    };

    private static final String[] WEAPONS = {
            "Rusty Sword", "Hunter Spear", "Battle Axe", "Dagger"
    };

    private static final String[] CONSUMABLES = {
            "Small Healing Potion", "Health Mushroom", "Elixir"
    };

    public WorldGenerator(long seed) {
        this.random = new Random(seed);
    }

    public void setMode(GenerationMode mode) {
        this.mode = mode;
    }

    public Room generate(int roomX, int roomY) {
        return mode == GenerationMode.PREDEFINED ? generateFromTemplate(roomX, roomY) : generateRandom(roomX, roomY);
    }

    private Room generateRandom(int roomX, int roomY) {
        TileType[][] tiles = createEmptyRoom();
        addWallPatches(tiles);

        List<MonsterInstance> monsters = generateMonsters(tiles, 2 + random.nextInt(4));
        List<Chest> chests = generateChests(tiles, 1 + random.nextInt(3));

        return new Room(roomX, roomY, tiles, monsters, chests);
    }

    private Room generateFromTemplate(int roomX, int roomY) {
        int templateId = Math.floorMod(roomX * 31 + roomY * 17, 3) + 1;
        String resourcePath = "/maps/room_template_" + templateId + ".txt";
        InputStream stream = WorldGenerator.class.getResourceAsStream(resourcePath);
        if (stream == null) {
            return generateRandom(roomX, roomY);
        }

        TileType[][] tiles = createEmptyRoom();
        List<MonsterInstance> monsters = new ArrayList<>();
        List<Chest> chests = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            for (int y = Room.HEIGHT - 1; y >= 0; y--) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                for (int x = 0; x < Math.min(Room.WIDTH, line.length()); x++) {
                    char cell = line.charAt(x);
                    switch (cell) {
                        case '#' -> tiles[y][x] = TileType.WALL;
                        case 'N' -> tiles[y][x] = TileType.DOOR_NORTH;
                        case 'S' -> tiles[y][x] = TileType.DOOR_SOUTH;
                        case 'E' -> tiles[y][x] = TileType.DOOR_EAST;
                        case 'W' -> tiles[y][x] = TileType.DOOR_WEST;
                        case 'M' -> {
                            tiles[y][x] = TileType.FLOOR;
                            monsters.add(new MonsterInstance(randomMonster(), x, y, random.nextInt(4)));
                        }
                        case 'C' -> {
                            tiles[y][x] = TileType.FLOOR;
                            chests.add(new Chest(x, y, generateLoot()));
                        }
                        default -> tiles[y][x] = TileType.FLOOR;
                    }
                }
            }
        } catch (Exception e) {
            return generateRandom(roomX, roomY);
        }

        if (monsters.isEmpty()) {
            monsters.addAll(generateMonsters(tiles, 3));
        }
        if (chests.isEmpty()) {
            chests.addAll(generateChests(tiles, 2));
        }

        return new Room(roomX, roomY, tiles, monsters, chests);
    }

    private TileType[][] createEmptyRoom() {
        TileType[][] tiles = new TileType[Room.HEIGHT][Room.WIDTH];
        for (int y = 0; y < Room.HEIGHT; y++) {
            for (int x = 0; x < Room.WIDTH; x++) {
                boolean border = x == 0 || y == 0 || x == Room.WIDTH - 1 || y == Room.HEIGHT - 1;
                tiles[y][x] = border ? TileType.WALL : TileType.FLOOR;
            }
        }
        tiles[Room.HEIGHT - 1][Room.WIDTH / 2] = TileType.DOOR_NORTH;
        tiles[0][Room.WIDTH / 2] = TileType.DOOR_SOUTH;
        tiles[Room.HEIGHT / 2][0] = TileType.DOOR_WEST;
        tiles[Room.HEIGHT / 2][Room.WIDTH - 1] = TileType.DOOR_EAST;
        return tiles;
    }

    private void addWallPatches(TileType[][] tiles) {
        int clusters = 10 + random.nextInt(6);
        for (int cluster = 0; cluster < clusters; cluster++) {
            int x = 2 + random.nextInt(Room.WIDTH - 4);
            int y = 2 + random.nextInt(Room.HEIGHT - 4);
            int length = 8 + random.nextInt(14);
            int direction = random.nextInt(4);

            for (int step = 0; step < length; step++) {
                if (isSafeForFeature(x, y) && tiles[y][x] == TileType.FLOOR) {
                    tiles[y][x] = TileType.WALL;
                    maybeThickenWall(tiles, x, y);
                }

                if (step % 2 == 0 && random.nextDouble() < 0.4) {
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
        if (random.nextDouble() < 0.5 && isSafeForFeature(x + 1, y) && tiles[y][x + 1] == TileType.FLOOR) {
            tiles[y][x + 1] = TileType.WALL;
        }
        if (random.nextDouble() < 0.5 && isSafeForFeature(x - 1, y) && tiles[y][x - 1] == TileType.FLOOR) {
            tiles[y][x - 1] = TileType.WALL;
        }
    }

    private List<MonsterInstance> generateMonsters(TileType[][] tiles, int count) {
        List<MonsterInstance> monsters = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int[] pos = randomFloorPosition(tiles);
            monsters.add(new MonsterInstance(randomMonster(), pos[0], pos[1], random.nextInt(4)));
        }
        return monsters;
    }

    private Monster randomMonster() {
        String name = MONSTER_NAMES[random.nextInt(MONSTER_NAMES.length)];
        int hp = 16 + random.nextInt(30);
        int atk = 4 + random.nextInt(8);
        return new Monster(name, hp, atk);
    }

    private List<Chest> generateChests(TileType[][] tiles, int count) {
        List<Chest> chests = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int[] pos = randomFloorPosition(tiles);
            chests.add(new Chest(pos[0], pos[1], generateLoot()));
        }
        return chests;
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
                loot.add(new Item("Gold Pouch", ItemType.TREASURE, 10 + random.nextInt(100)));
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
