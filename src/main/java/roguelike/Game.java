package roguelike;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.utils.ScreenUtils;

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
        font = createReadableFont();

        createTextures();
        addLog("Рогалик: WASD/стрелки — движение, F — бой, E — сундук, Q — использовать предмет");
        addLog("Tab/Shift+Tab — переключение предметов в инвентаре.");
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
        batch.setColor(1f, 1f, 1f, 0.95f);
        batch.draw(panelTexture, panelX, 8, 330, 640);
        batch.setColor(Color.WHITE);

        int infoX = Room.WIDTH * TILE_SIZE + 16;
        int topY = 642;

        font.draw(batch, "Комната: [" + roomX + ", " + roomY + "]", infoX, topY);
        font.draw(batch, "Здоровье: " + player.health() + "  Атака: " + player.attackPower(), infoX, topY - 24);
        if (room.monster() != null && room.monster().isAlive()) {
            font.draw(batch, "Монстр: " + room.monster().name() + " HP " + room.monster().health(), infoX, topY - 48);
        } else {
            font.draw(batch, "Монстр: нет", infoX, topY - 48);
        }

        drawInventory(infoX, topY - 78);

        int lineY = 224;
        font.draw(batch, "Лог:", infoX, lineY);
        lineY -= 18;
        for (String s : log) {
            font.draw(batch, s, infoX, lineY);
            lineY -= 16;
            if (lineY < 20) {
                break;
            }
        }
        batch.end();
    }

    private void drawInventory(int infoX, int topY) {
        List<Item> inventory = player.inventory();
        font.draw(batch, "Инвентарь (Tab / Shift+Tab):", infoX, topY);
        if (inventory.isEmpty()) {
            font.draw(batch, "  (пусто)", infoX, topY - 18);
            return;
        }

        int selected = player.selectedItemIndex();
        int maxLines = 9;
        int start = Math.max(0, selected - 4);
        if (start + maxLines > inventory.size()) {
            start = Math.max(0, inventory.size() - maxLines);
        }

        for (int i = 0; i < maxLines && start + i < inventory.size(); i++) {
            int index = start + i;
            Item item = inventory.get(index);
            String marker = index == selected ? "> " : "  ";
            String line = marker + (index + 1) + ") " + compactItemText(item);
            font.draw(batch, line, infoX, topY - 18 - (i * 16));
        }
    }

    private String compactItemText(Item item) {
        return switch (item.type()) {
            case WEAPON -> item.name() + " [оружие +" + item.power() + "]";
            case CONSUMABLE -> item.name() + " [лечение " + item.power() + "]";
            case TREASURE -> item.name() + " [ценность " + item.power() + "]";
        };
    }

    private BitmapFont createReadableFont() {
        String[] fontCandidates = {
                "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
                "/usr/share/fonts/truetype/freefont/FreeSans.ttf"
        };

        for (String path : fontCandidates) {
            if (new java.io.File(path).exists()) {
                FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.absolute(path));
                FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
                parameter.size = 14;
                parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS +
                        "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ" +
                        "абвгдеёжзийклмнопрстуфхцчшщъыьэюя" +
                        "—«»№";
                BitmapFont generated = generator.generateFont(parameter);
                generator.dispose();
                generated.setColor(Color.WHITE);
                return generated;
            }
        }

        BitmapFont fallback = new BitmapFont();
        fallback.setColor(Color.WHITE);
        return fallback;
    }

    private void createTextures() {
        tileTextures.put(TileType.FLOOR, createFloorTexture());
        tileTextures.put(TileType.WALL, createWallTexture());
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
        panelTexture = createSolidTexture(new Color(0.12f, 0.12f, 0.16f, 1f));

        exportTexturePngs();
    }

    private Texture createFloorTexture() {
        Pixmap pixmap = new Pixmap(TILE_SIZE, TILE_SIZE, Pixmap.Format.RGBA8888);
        pixmap.setColor(0.28f, 0.28f, 0.33f, 1f);
        pixmap.fill();
        pixmap.setColor(0.36f, 0.36f, 0.42f, 1f);
        pixmap.fillRectangle(1, 1, 14, 14);
        pixmap.setColor(0.24f, 0.24f, 0.28f, 1f);
        for (int y = 2; y < TILE_SIZE - 2; y += 4) {
            pixmap.drawLine(2, y, TILE_SIZE - 3, y);
        }
        return textureFrom(pixmap);
    }

    private Texture createWallTexture() {
        Pixmap pixmap = new Pixmap(TILE_SIZE, TILE_SIZE, Pixmap.Format.RGBA8888);
        pixmap.setColor(0.14f, 0.14f, 0.17f, 1f);
        pixmap.fill();
        pixmap.setColor(0.2f, 0.2f, 0.24f, 1f);
        pixmap.fillRectangle(1, 1, 14, 14);
        pixmap.setColor(0.08f, 0.08f, 0.1f, 1f);
        for (int by = 2; by < TILE_SIZE - 2; by += 4) {
            for (int bx = 2; bx < TILE_SIZE - 2; bx += 6) {
                pixmap.fillRectangle(bx, by, 4, 2);
            }
        }
        return textureFrom(pixmap);
    }

    private Texture createTrapTexture() {
        Pixmap pixmap = new Pixmap(TILE_SIZE, TILE_SIZE, Pixmap.Format.RGBA8888);
        pixmap.setColor(0.26f, 0.08f, 0.08f, 1f);
        pixmap.fill();
        pixmap.setColor(0.76f, 0.15f, 0.15f, 1f);
        for (int x = 1; x < TILE_SIZE; x += 2) {
            int endX = Math.min(TILE_SIZE - 1, x + 2);
            pixmap.drawLine(x, 0, endX, TILE_SIZE - 1);
        }
        pixmap.setColor(0.95f, 0.8f, 0.2f, 1f);
        pixmap.fillRectangle(7, 7, 2, 2);
        return textureFrom(pixmap);
    }

    private Texture createDoorTexture() {
        Pixmap pixmap = new Pixmap(TILE_SIZE, TILE_SIZE, Pixmap.Format.RGBA8888);
        pixmap.setColor(0.35f, 0.2f, 0.07f, 1f);
        pixmap.fill();
        pixmap.setColor(0.5f, 0.32f, 0.14f, 1f);
        pixmap.fillRectangle(2, 1, 12, 14);
        pixmap.setColor(0.72f, 0.52f, 0.18f, 1f);
        pixmap.fillRectangle(11, 7, 2, 2);
        return textureFrom(pixmap);
    }

    private Texture createPlayerTexture() {
        Pixmap pixmap = new Pixmap(TILE_SIZE, TILE_SIZE, Pixmap.Format.RGBA8888);
        pixmap.setColor(0f, 0f, 0f, 0f);
        pixmap.fill();
        pixmap.setColor(0.12f, 0.82f, 0.2f, 1f);
        pixmap.fillRectangle(4, 3, 8, 9);
        pixmap.setColor(0.06f, 0.58f, 0.11f, 1f);
        pixmap.fillRectangle(5, 12, 6, 3);
        pixmap.setColor(0.92f, 0.92f, 0.92f, 1f);
        pixmap.fillRectangle(6, 8, 1, 1);
        pixmap.fillRectangle(9, 8, 1, 1);
        return textureFrom(pixmap);
    }

    private Texture createMonsterTexture() {
        Pixmap pixmap = new Pixmap(TILE_SIZE, TILE_SIZE, Pixmap.Format.RGBA8888);
        pixmap.setColor(0f, 0f, 0f, 0f);
        pixmap.fill();
        pixmap.setColor(0.72f, 0.14f, 0.2f, 1f);
        pixmap.fillRectangle(3, 3, 10, 10);
        pixmap.setColor(0.95f, 0.9f, 0.9f, 1f);
        pixmap.fillRectangle(5, 8, 2, 2);
        pixmap.fillRectangle(9, 8, 2, 2);
        return textureFrom(pixmap);
    }

    private Texture createChestTexture(boolean opened) {
        Pixmap pixmap = new Pixmap(TILE_SIZE, TILE_SIZE, Pixmap.Format.RGBA8888);
        pixmap.setColor(0f, 0f, 0f, 0f);
        pixmap.fill();
        pixmap.setColor(0.4f, 0.23f, 0.08f, 1f);
        pixmap.fillRectangle(2, 3, 12, 10);
        pixmap.setColor(0.62f, 0.38f, 0.16f, 1f);
        if (opened) {
            pixmap.fillRectangle(2, 11, 12, 2);
            pixmap.fillRectangle(2, 6, 12, 2);
        } else {
            pixmap.fillRectangle(2, 10, 12, 3);
        }
        pixmap.setColor(0.9f, 0.74f, 0.22f, 1f);
        pixmap.fillRectangle(7, 7, 2, 2);
        return textureFrom(pixmap);
    }

    private Texture createSolidTexture(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        return textureFrom(pixmap);
    }

    private Texture textureFrom(Pixmap pixmap) {
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private void exportTexturePngs() {
        savePng("floor", tileTextures.get(TileType.FLOOR));
        savePng("wall", tileTextures.get(TileType.WALL));
        savePng("trap", tileTextures.get(TileType.TRAP));
        savePng("door", tileTextures.get(TileType.DOOR_NORTH));
        savePng("player", playerTexture);
        savePng("monster", monsterTexture);
        savePng("chest_closed", chestClosedTexture);
        savePng("chest_opened", chestOpenedTexture);
    }

    private void savePng(String name, Texture texture) {
        try {
            var data = texture.getTextureData();
            if (!data.isPrepared()) {
                data.prepare();
            }
            Pixmap pixmap = data.consumePixmap();
            PixmapIO.writePNG(Gdx.files.local("generated-textures/" + name + ".png"), pixmap);
            if (data.disposePixmap()) {
                pixmap.dispose();
            }
        } catch (Exception ignored) {
        }
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

        if (Gdx.input.isKeyJustPressed(Input.Keys.TAB)) {
            if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) {
                player.selectPreviousItem();
            } else {
                player.selectNextItem();
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.F)) {
            fight();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            openChest();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
            boolean used = player.useSelectedConsumable();
            if (!used) {
                used = player.usePotionIfAny();
            }
            addLog(used ? "Использован лечебный предмет." : "Лечебных предметов нет.");
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F12)) {
            Pixmap screenshot = ScreenUtils.getFrameBufferPixmap(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
            PixmapIO.writePNG(Gdx.files.local("generated-textures/screenshot.png"), screenshot);
            screenshot.dispose();
            addLog("Скриншот сохранён в generated-textures/screenshot.png");
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
        if (log.size() > 12) {
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
