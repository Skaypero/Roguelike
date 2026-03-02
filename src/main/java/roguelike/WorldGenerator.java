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

    public Room generate(int x, int y) {
        Monster monster = null;
        Chest chest = null;

        if (!(x == 0 && y == 0) && random.nextDouble() < 0.65) {
            String name = MONSTER_NAMES[random.nextInt(MONSTER_NAMES.length)];
            int hp = 20 + random.nextInt(25);
            int atk = 5 + random.nextInt(8);
            monster = new Monster(name, hp, atk);
        }

        if (random.nextDouble() < 0.5) {
            chest = new Chest(generateLoot());
        }

        return new Room(x, y, monster, chest);
    }

    private List<Item> generateLoot() {
        int count = 1 + random.nextInt(3);
        List<Item> loot = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            double roll = random.nextDouble();
            if (roll < 0.4) {
                String weapon = WEAPONS[random.nextInt(WEAPONS.length)];
                loot.add(new Item(weapon, ItemType.WEAPON, 2 + random.nextInt(8)));
            } else if (roll < 0.8) {
                String consumable = CONSUMABLES[random.nextInt(CONSUMABLES.length)];
                loot.add(new Item(consumable, ItemType.CONSUMABLE, 10 + random.nextInt(20)));
            } else {
                loot.add(new Item("Горсть золота", ItemType.TREASURE, 10 + random.nextInt(100)));
            }
        }
        return loot;
    }
}
