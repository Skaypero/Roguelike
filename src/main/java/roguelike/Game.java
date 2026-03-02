package roguelike;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Game extends ApplicationAdapter {
    private static final int TILE_SIZE = 16;

    private final Player player = new Player();
    private final WorldGenerator generator = new WorldGenerator(System.currentTimeMillis());
    private final Map<String, Room> rooms = new HashMap<>();
    private final List<String> log = new ArrayList<>();

    private int roomX = 0;
    private int roomY = 0;
    private int playerX = Room.WIDTH / 2;
    private int playerY = Room.HEIGHT / 2;

    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont font;

    @Override
    public void create() {
        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.setColor(Color.WHITE);
        addLog("libGDX roguelike: WASD/стрелки ходить, F бить, E сундук, Q зелье");
    }

    @Override
    public void render() {
        handleInput();

        Gdx.gl.glClearColor(0.08f, 0.08f, 0.12f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        Room room = currentRoom();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int y = 0; y < Room.HEIGHT; y++) {
            for (int x = 0; x < Room.WIDTH; x++) {
                shapeRenderer.setColor(colorForTile(room.tile(x, y)));
                shapeRenderer.rect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }

        shapeRenderer.setColor(new Color(0.2f, 0.2f, 0.2f, 1f));
        shapeRenderer.rect(Room.WIDTH * TILE_SIZE + 8, 8, 320, 620);

        shapeRenderer.setColor(new Color(0.2f, 0.9f, 0.2f, 1f));
        shapeRenderer.rect(playerX * TILE_SIZE + 2, playerY * TILE_SIZE + 2, TILE_SIZE - 4, TILE_SIZE - 4);
        shapeRenderer.end();

        batch.begin();
        int infoX = Room.WIDTH * TILE_SIZE + 16;
        int topY = 610;

        font.draw(batch, "Room: [" + roomX + ", " + roomY + "]", infoX, topY);
        font.draw(batch, "HP: " + player.health() + "  ATK: " + player.attackPower(), infoX, topY - 24);
        if (room.monster() != null && room.monster().isAlive()) {
            font.draw(batch, "Monster: " + room.monster().name() + " HP " + room.monster().health(), infoX, topY - 48);
        } else {
            font.draw(batch, "Monster: none", infoX, topY - 48);
        }
        font.draw(batch, "Inventory: " + player.inventory().size(), infoX, topY - 72);

        int lineY = topY - 120;
        for (String s : log) {
            font.draw(batch, s, infoX, lineY);
            lineY -= 20;
        }
        batch.end();
    }

    private Color colorForTile(TileType tile) {
        return switch (tile) {
            case FLOOR -> new Color(0.35f, 0.35f, 0.40f, 1f);
            case WALL -> new Color(0.14f, 0.14f, 0.16f, 1f);
            case TRAP -> new Color(0.8f, 0.2f, 0.2f, 1f);
            case DOOR_NORTH, DOOR_EAST, DOOR_SOUTH, DOOR_WEST -> new Color(0.9f, 0.8f, 0.3f, 1f);
        };
    }

    private void handleInput() {
        if (!player.isAlive()) {
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.W) || Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            moveBy(0, 1);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.S) || Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            moveBy(0, -1);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.A) || Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            moveBy(-1, 0);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.D) || Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            moveBy(1, 0);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.F)) {
            fight();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            openChest();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
            boolean used = player.usePotionIfAny();
            addLog(used ? "Использовано лечение." : "Лечебных предметов нет.");
        }
    }

    private void moveBy(int dx, int dy) {
        Room room = currentRoom();
        int nx = playerX + dx;
        int ny = playerY + dy;

        if (nx < 0 || ny < 0 || nx >= Room.WIDTH || ny >= Room.HEIGHT) {
            return;
        }

        TileType target = room.tile(nx, ny);
        if (target == TileType.WALL) {
            return;
        }

        if (target == TileType.DOOR_NORTH) {
            roomY += 1;
            playerX = Room.WIDTH / 2;
            playerY = 1;
            addLog("Переход в северную комнату.");
            return;
        }
        if (target == TileType.DOOR_SOUTH) {
            roomY -= 1;
            playerX = Room.WIDTH / 2;
            playerY = Room.HEIGHT - 2;
            addLog("Переход в южную комнату.");
            return;
        }
        if (target == TileType.DOOR_EAST) {
            roomX += 1;
            playerX = 1;
            playerY = Room.HEIGHT / 2;
            addLog("Переход в восточную комнату.");
            return;
        }
        if (target == TileType.DOOR_WEST) {
            roomX -= 1;
            playerX = Room.WIDTH - 2;
            playerY = Room.HEIGHT / 2;
            addLog("Переход в западную комнату.");
            return;
        }

        playerX = nx;
        playerY = ny;
        if (target == TileType.TRAP) {
            player.takeDamage(8);
            addLog("Ловушка! -8 HP. Текущее HP: " + player.health());
            if (!player.isAlive()) {
                addLog("GAME OVER");
            }
        }
    }

    private void fight() {
        Room room = currentRoom();
        Monster monster = room.monster();
        if (monster == null || !monster.isAlive()) {
            addLog("Монстра рядом нет.");
            return;
        }

        int playerDamage = player.attackPower();
        monster.takeDamage(playerDamage);
        addLog("Вы нанесли " + playerDamage + " урона монстру " + monster.name() + ".");
        if (!monster.isAlive()) {
            addLog("Монстр повержен.");
            return;
        }

        player.takeDamage(monster.attack());
        addLog(monster.name() + " ударил на " + monster.attack() + ". HP: " + player.health());
    }

    private void openChest() {
        Room room = currentRoom();
        Chest chest = room.chest();
        if (chest == null) {
            addLog("Сундука нет.");
            return;
        }

        List<Item> loot = chest.open();
        if (loot.isEmpty()) {
            addLog("Сундук пуст.");
            return;
        }

        player.addItems(loot);
        addLog("Лут: " + loot);
    }

    private Room currentRoom() {
        String key = roomX + ":" + roomY;
        return rooms.computeIfAbsent(key, ignored -> generator.generate(roomX, roomY));
    }

    private void addLog(String message) {
        log.add(0, message);
        if (log.size() > 20) {
            log.remove(log.size() - 1);
        }
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        font.dispose();
    }
}
