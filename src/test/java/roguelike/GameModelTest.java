package roguelike;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameModelTest {

    @Test
    void playerUsesSelectedWeaponForAttack() {
        Player player = new Player();
        player.addItems(List.of(
                new Item("Knife", ItemType.WEAPON, 3),
                new Item("Sword", ItemType.WEAPON, 6)
        ));

        assertEquals(13, player.attackPower());
        player.selectNextItem();
        assertEquals(16, player.attackPower());
    }

    @Test
    void selectedConsumableCanBeUsed() {
        Player player = new Player();
        player.takeDamage(30);
        player.addItems(List.of(
                new Item("Sword", ItemType.WEAPON, 6),
                new Item("Potion", ItemType.CONSUMABLE, 20)
        ));

        player.selectNextItem();
        assertTrue(player.useSelectedConsumable());
        assertEquals(90, player.health());
    }

    @Test
    void generatedRoomHasExpectedSizeAndDoors() {
        WorldGenerator generator = new WorldGenerator(42L);
        Room room = generator.generate(0, 0);

        assertEquals(Room.WIDTH, room.tiles()[0].length);
        assertEquals(Room.HEIGHT, room.tiles().length);
        assertEquals(TileType.DOOR_NORTH, room.tile(Room.WIDTH / 2, Room.HEIGHT - 1));
        assertEquals(TileType.DOOR_SOUTH, room.tile(Room.WIDTH / 2, 0));
        assertEquals(TileType.DOOR_WEST, room.tile(0, Room.HEIGHT / 2));
        assertEquals(TileType.DOOR_EAST, room.tile(Room.WIDTH - 1, Room.HEIGHT / 2));
    }

    @Test
    void generatedRoomHasNoTrapsAndHasMultipleEntities() {
        WorldGenerator generator = new WorldGenerator(10L);
        Room room = generator.generate(3, 2);

        int traps = 0;
        for (int y = 0; y < Room.HEIGHT; y++) {
            for (int x = 0; x < Room.WIDTH; x++) {
                if (room.tile(x, y) == TileType.TRAP) {
                    traps++;
                }
            }
        }

        assertEquals(0, traps);
        assertTrue(room.monsters().size() >= 2);
        assertTrue(room.chests().size() >= 1);
    }

    @Test
    void predefinedGenerationLoadsEntitiesFromTemplates() {
        WorldGenerator generator = new WorldGenerator(123L);
        generator.setMode(WorldGenerator.GenerationMode.PREDEFINED);
        Room room = generator.generate(0, 0);

        assertFalse(room.monsters().isEmpty());
        assertFalse(room.chests().isEmpty());
    }
}
