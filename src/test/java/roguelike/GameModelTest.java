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
    void generatedRoomContainsTraps() {
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

        assertTrue(traps > 0);
    }
}
