package roguelike;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;
import java.util.EnumMap;
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

    private SpriteBatch batch;
    private BitmapFont font;

    private final Map<TileType, Texture> tileTextures = new EnumMap<>(TileType.class);
    private Texture playerTexture;
    private Texture chestClosedTexture;
    private Texture chestOpenedTexture;
    private Texture monsterTexture;
    private Texture panelTexture;

    @Override
    public void create() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.setColor(Color.WHITE);

        createTextures();
        addLog("Рогалик: WASD/стрелки — движение, F — бой, E — сундук, Q — зелье");
    }

    @Override
    public void render() {
        handleInput();

        Gdx.gl.glClearColor(0.08f, 0.08f, 0.12f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        Room room = currentRoom();

        batch.begin();

        for (int y = 0; y < Room.HEIGHT; y++) {
            for (int x = 0; x < Room.WIDTH; x++) {
                Texture tileTexture = tileTextures.get(room.tile(x, y));
                batch.draw(tileTexture, x * TILE_SIZE, y * TILE_SIZE);
            }
        }

        Chest chest = room.chest();
        if (chest != null) {
            Texture chestTexture = chest.isOpened() ? chestOpenedTexture : chestClosedTexture;
            batch.draw(chestTexture, chest.x() * TILE_SIZE, chest.y() * TILE_SIZE);
        }

        if (room.monster() != null && room.monster().isAlive()) {
            int monsterX = Room.WIDTH / 2 + 4;
            int monsterY = Room.HEIGHT / 2 + 3;
            batch.draw(monsterTexture, monsterX * TILE_SIZE, monsterY * TILE_SIZE);
        }

        batch.draw(playerTexture, playerX * TILE_SIZE, playerY * TILE_SIZE);

        int panelX = Room.WIDTH * TILE_SIZE + 8;
        batch.setColor(1f, 1f, 1f, 0.9f);
        batch.draw(panelTexture, panelX, 8, 320, 620);
        batch.setColor(Color.WHITE);

        int infoX = Room.WIDTH * TILE_SIZE + 16;
        int topY = 610;

        font.draw(batch, "Комната: [" + roomX + ", " + roomY + "]", infoX, topY);
        font.draw(batch, "Здоровье: " + player.health() + "  Атака: " + player.attackPower(), infoX, topY - 24);
        if (room.monster() != null && room.monster().isAlive()) {
            font.draw(batch, "Монстр: " + room.monster().name() + " HP " + room.monster().health(), infoX, topY - 48);
        } else {
            font.draw(batch, "Монстр: нет", infoX, topY - 48);
        }
        font.draw(batch, "Инвентарь: " + player.inventory().size(), infoX, topY - 72);

        int lineY = topY - 120;
        for (String s : log) {
            font.draw(batch, s, infoX, lineY);
            lineY -= 20;
        }
        batch.end();
    }

    private void createTextures() {
        tileTextures.put(TileType.FLOOR, createTileTexture(new Color(0.32f, 0.32f, 0.36f, 1f), new Color(0.28f, 0.28f, 0.32f, 1f)));
        tileTextures.put(TileType.WALL, createTileTexture(new Color(0.14f, 0.14f, 0.16f, 1f), new Color(0.18f, 0.18f, 0.2f, 1f)));
        tileTextures.put(TileType.TRAP, createTrapTexture());
        Texture doorTexture = createDoorTexture();
        tileTextures.put(TileType.DOOR_NORTH, doorTexture);
        tileTextures.put(TileType.DOOR_SOUTH, doorTexture);
        tileTextures.put(TileType.DOOR_EAST, doorTexture);
        tileTextures.put(TileType.DOOR_WEST, doorTexture);

        playerTexture = createPlayerTexture();
        monsterTexture = createMonsterTexture();
        chestClosedTexture = createChestTexture(false);
        chestOpenedTexture = createChestTexture(true);
        panelTexture = createSolidTexture(new Color(0.2f, 0.2f, 0.2f, 1f));
    }

    private Texture createTileTexture(Color base, Color detail) {
        Pixmap pixmap = new Pixmap(TILE_SIZE, TILE_SIZE, Pixmap.Format.RGBA8888);
        pixmap.setColor(base);
        pixmap.fill();

        pixmap.setColor(detail);
        for (int y = 0; y < TILE_SIZE; y++) {
            for (int x = 0; x < TILE_SIZE; x++) {
                if ((x + y) % 4 == 0) {
                    pixmap.drawPixel(x, y);
                }
            }
        }

        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private Texture createTrapTexture() {
        Pixmap pixmap = new Pixmap(TILE_SIZE, TILE_SIZE, Pixmap.Format.RGBA8888);
        pixmap.setColor(0.28f, 0.12f, 0.12f, 1f);
        pixmap.fill();
        pixmap.setColor(0.86f, 0.2f, 0.2f, 1f);
        for (int x = 1; x < TILE_SIZE; x += 3) {
            pixmap.drawLine(x, 0, TILE_SIZE - 1 - x / 2, TILE_SIZE - 1);
        }
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private Texture createDoorTexture() {
        Pixmap pixmap = new Pixmap(TILE_SIZE, TILE_SIZE, Pixmap.Format.RGBA8888);
        pixmap.setColor(0.40f, 0.27f, 0.12f, 1f);
        pixmap.fill();
        pixmap.setColor(0.55f, 0.38f, 0.2f, 1f);
        pixmap.fillRectangle(2, 1, TILE_SIZE - 4, TILE_SIZE - 2);
        pixmap.setColor(0.85f, 0.72f, 0.3f, 1f);
        pixmap.fillRectangle(TILE_SIZE - 5, TILE_SIZE / 2 - 1, 2, 2);
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private Texture createPlayerTexture() {
        Pixmap pixmap = new Pixmap(TILE_SIZE, TILE_SIZE, Pixmap.Format.RGBA8888);
        pixmap.setColor(0f, 0f, 0f, 0f);
        pixmap.fill();
        pixmap.setColor(0.2f, 0.9f, 0.2f, 1f);
        pixmap.fillRectangle(4, 3, 8, 9);
        pixmap.setColor(0.1f, 0.6f, 0.1f, 1f);
        pixmap.fillRectangle(5, 12, 6, 3);
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private Texture createMonsterTexture() {
        Pixmap pixmap = new Pixmap(TILE_SIZE, TILE_SIZE, Pixmap.Format.RGBA8888);
        pixmap.setColor(0f, 0f, 0f, 0f);
        pixmap.fill();
        pixmap.setColor(0.8f, 0.2f, 0.25f, 1f);
        pixmap.fillRectangle(3, 3, 10, 10);
        pixmap.setColor(0.95f, 0.95f, 0.95f, 1f);
        pixmap.fillRectangle(5, 8, 2, 2);
        pixmap.fillRectangle(9, 8, 2, 2);
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private Texture createChestTexture(boolean opened) {
        Pixmap pixmap = new Pixmap(TILE_SIZE, TILE_SIZE, Pixmap.Format.RGBA8888);
        pixmap.setColor(0f, 0f, 0f, 0f);
        pixmap.fill();
        pixmap.setColor(0.45f, 0.28f, 0.12f, 1f);
        pixmap.fillRectangle(2, 2, 12, 11);
        pixmap.setColor(0.66f, 0.43f, 0.19f, 1f);
        if (opened) {
            pixmap.fillRectangle(2, 11, 12, 3);
            pixmap.fillRectangle(2, 6, 12, 2);
        } else {
            pixmap.fillRectangle(2, 10, 12, 4);
        }
        pixmap.setColor(0.85f, 0.72f, 0.3f, 1f);
        pixmap.fillRectangle(7, 6, 2, 3);
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private Texture createSolidTexture(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
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
                addLog("ИГРА ОКОНЧЕНА");
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
        int dx = Math.abs(playerX - chest.x());
        int dy = Math.abs(playerY - chest.y());
        if (dx > 1 || dy > 1) {
            addLog("Подойдите ближе к сундуку.");
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
        batch.dispose();
        font.dispose();
        for (Texture texture : tileTextures.values()) {
            texture.dispose();
        }
        playerTexture.dispose();
        chestClosedTexture.dispose();
        chestOpenedTexture.dispose();
        monsterTexture.dispose();
        panelTexture.dispose();
    }
}
