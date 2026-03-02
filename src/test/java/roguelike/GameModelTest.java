package roguelike;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameModelTest {

    @Test
    void playerUsesBestWeaponForAttack() {
        Player player = new Player();
        player.addItems(List.of(
                new Item("Knife", ItemType.WEAPON, 3),
                new Item("Sword", ItemType.WEAPON, 6)
        ));

        assertEquals(16, player.attackPower());
    }

    @Test
    void chestCanBeOpenedOnlyOnce() {
        Chest chest = new Chest(List.of(new Item("Potion", ItemType.CONSUMABLE, 10)));

        assertEquals(1, chest.open().size());
        assertTrue(chest.open().isEmpty());
    }

    @Test
    void generatedRoomsAreDeterministicForSameSeed() {
        WorldGenerator genA = new WorldGenerator(42L);
        WorldGenerator genB = new WorldGenerator(42L);

        Room a = genA.generate(1, -3);
        Room b = genB.generate(1, -3);

        assertEquals(a.description(), b.description());
    }
}
