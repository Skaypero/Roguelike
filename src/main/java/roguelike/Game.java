package roguelike;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Game extends ApplicationAdapter {
    private static final int TILE_TEXTURE_SIZE = 64;
    private static final int BOARD_PADDING = 24;
    private static final int PANEL_WIDTH = 420;

    private enum GameState { MENU, PLAYING }

    private final Player player = new Player();
    private final WorldGenerator generator = new WorldGenerator(System.currentTimeMillis());
    private final Map<String, Room> rooms = new HashMap<>();
    private final Map<String, RoomTexturePack> roomTexturePacks = new HashMap<>();
    private final List<String> log = new ArrayList<>();

    private int roomX = 0;
    private int roomY = 0;
    private int playerX = Room.WIDTH / 2;
    private int playerY = Room.HEIGHT / 2;

    private GameState state = GameState.MENU;

    private SpriteBatch batch;
    private BitmapFont font;
    private final GlyphLayout layout = new GlyphLayout();
    private Texture playerTexture;
    private Texture chestClosedTexture;
    private Texture chestOpenedTexture;
    private Texture panelTexture;
    private final Texture[] monsterTextures = new Texture[5];

    @Override
    public void create() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.setColor(Color.WHITE);

        playerTexture = createPlayerTexture();
        chestClosedTexture = createChestTexture(false);
        chestOpenedTexture = createChestTexture(true);
        panelTexture = createSolidTexture(new Color(0.10f, 0.10f, 0.14f, 1f));
        for (int i = 0; i < monsterTextures.length; i++) {
            monsterTextures[i] = createMonsterTexture(i);
        }

        addLog("Welcome! Choose generation mode.");
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0.07f, 0.07f, 0.10f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (state == GameState.MENU) {
            handleMenuInput();
            drawMenu();
            return;
        }

        handleInput();
        drawGame();
    }

    private void handleMenuInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
            startNewGame(WorldGenerator.GenerationMode.RANDOM);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
            startNewGame(WorldGenerator.GenerationMode.PREDEFINED);
        }
    }

    private void startNewGame(WorldGenerator.GenerationMode mode) {
        generator.setMode(mode);
        rooms.clear();
        roomTexturePacks.clear();
        log.clear();
        roomX = 0;
        roomY = 0;
        playerX = Room.WIDTH / 2;
        playerY = Room.HEIGHT / 2;
        state = GameState.PLAYING;
        addLog("Mode: " + mode);
        addLog("Move: WASD / Arrows");
        addLog("Actions: F fight, E open, Q consume");
        addLog("Inventory: TAB / SHIFT+TAB");
        addLog("Esc: back to menu");
    }

    private void drawMenu() {
        int w = Gdx.graphics.getWidth();
        int h = Gdx.graphics.getHeight();

        batch.begin();
        drawCentered("ROGUELIKE", w / 2f, h * 0.70f);
        drawCentered("1 - Random generation", w / 2f, h * 0.60f);
        drawCentered("2 - Predefined template generation", w / 2f, h * 0.55f);
        drawCentered("Fullscreen + centered UI + runtime textures", w / 2f, h * 0.46f);
        drawCentered("Press 1 or 2", w / 2f, h * 0.38f);
        batch.end();
    }

    private void drawCentered(String text, float centerX, float y) {
        layout.setText(font, text);
        font.draw(batch, text, centerX - layout.width / 2f, y);
    }

    private void drawGame() {
        Room room = currentRoom();
        RoomTexturePack textures = currentRoomTextures();

        int screenW = Gdx.graphics.getWidth();
        int screenH = Gdx.graphics.getHeight();
        int availableBoardW = screenW - PANEL_WIDTH - BOARD_PADDING * 3;
        int availableBoardH = screenH - BOARD_PADDING * 2;
        int tileRenderSize = Math.max(12, Math.min(availableBoardW / Room.WIDTH, availableBoardH / Room.HEIGHT));

        int boardPixelW = Room.WIDTH * tileRenderSize;
        int boardPixelH = Room.HEIGHT * tileRenderSize;
        int groupW = boardPixelW + BOARD_PADDING + PANEL_WIDTH;
        int startX = Math.max(BOARD_PADDING, (screenW - groupW) / 2);
        int boardX = startX;
        int boardY = Math.max(BOARD_PADDING, (screenH - boardPixelH) / 2);
        int panelX = boardX + boardPixelW + BOARD_PADDING;
        int panelY = boardY;

        batch.begin();

        for (int y = 0; y < Room.HEIGHT; y++) {
            for (int x = 0; x < Room.WIDTH; x++) {
                Texture tileTexture = textures.tileTextures.get(room.tile(x, y));
                batch.draw(tileTexture,
                        boardX + x * tileRenderSize,
                        boardY + y * tileRenderSize,
                        tileRenderSize,
                        tileRenderSize);
            }
        }

        for (Chest chest : room.chests()) {
            Texture chestTexture = chest.isOpened() ? chestOpenedTexture : chestClosedTexture;
            batch.draw(chestTexture,
                    boardX + chest.x() * tileRenderSize,
                    boardY + chest.y() * tileRenderSize,
                    tileRenderSize,
                    tileRenderSize);
        }

        for (MonsterInstance monster : room.monsters()) {
            if (monster.isAlive()) {
                Texture mTexture = monsterTextures[Math.floorMod(monster.textureVariant(), monsterTextures.length)];
                batch.draw(mTexture,
                        boardX + monster.x() * tileRenderSize,
                        boardY + monster.y() * tileRenderSize,
                        tileRenderSize,
                        tileRenderSize);
            }
        }

        batch.draw(playerTexture,
                boardX + playerX * tileRenderSize,
                boardY + playerY * tileRenderSize,
                tileRenderSize,
                tileRenderSize);

        batch.draw(panelTexture, panelX, panelY, PANEL_WIDTH, boardPixelH);

        int infoX = panelX + 14;
        int topY = panelY + boardPixelH - 12;
        font.draw(batch, "Room: [" + roomX + ", " + roomY + "]", infoX, topY);
        font.draw(batch, "HP: " + player.health() + "  ATK: " + player.attackPower(), infoX, topY - 20);
        long alive = room.monsters().stream().filter(MonsterInstance::isAlive).count();
        font.draw(batch, "Monsters alive: " + alive, infoX, topY - 40);
        font.draw(batch, "Chests: " + room.chests().size(), infoX, topY - 60);

        int inventoryY = topY - 88;
        drawInventory(infoX, inventoryY);

        int logY = panelY + 220;
        font.draw(batch, "Log:", infoX, logY);
        logY -= 16;
        for (String s : log) {
            font.draw(batch, s, infoX, logY);
            logY -= 16;
            if (logY < panelY + 20) {
                break;
            }
        }

        batch.end();
    }

    private void drawInventory(int infoX, int topY) {
        List<Item> inventory = player.inventory();
        font.draw(batch, "Inventory:", infoX, topY);
        if (inventory.isEmpty()) {
            font.draw(batch, "  (empty)", infoX, topY - 16);
            return;
        }

        int selected = player.selectedItemIndex();
        int maxLines = 10;
        int start = Math.max(0, selected - 4);
        if (start + maxLines > inventory.size()) {
            start = Math.max(0, inventory.size() - maxLines);
        }

        for (int i = 0; i < maxLines && start + i < inventory.size(); i++) {
            int index = start + i;
            Item item = inventory.get(index);
            String marker = index == selected ? "> " : "  ";
            String line = marker + (index + 1) + ") " + compactItemText(item);
            font.draw(batch, line, infoX, topY - 16 - (i * 15));
        }
    }

    private String compactItemText(Item item) {
        return switch (item.type()) {
            case WEAPON -> item.name() + " [+" + item.power() + " atk]";
            case CONSUMABLE -> item.name() + " [heal " + item.power() + "]";
            case TREASURE -> item.name() + " [value " + item.power() + "]";
        };
    }

    private void handleInput() {
        if (!player.isAlive()) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                state = GameState.MENU;
            }
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            state = GameState.MENU;
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
            addLog(used ? "Consumable used." : "No consumables available.");
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
            addLog("Moved to north room.");
            return;
        }
        if (target == TileType.DOOR_SOUTH) {
            roomY -= 1;
            playerX = Room.WIDTH / 2;
            playerY = Room.HEIGHT - 2;
            addLog("Moved to south room.");
            return;
        }
        if (target == TileType.DOOR_EAST) {
            roomX += 1;
            playerX = 1;
            playerY = Room.HEIGHT / 2;
            addLog("Moved to east room.");
            return;
        }
        if (target == TileType.DOOR_WEST) {
            roomX -= 1;
            playerX = Room.WIDTH - 2;
            playerY = Room.HEIGHT / 2;
            addLog("Moved to west room.");
            return;
        }

        playerX = nx;
        playerY = ny;
    }

    private void fight() {
        Room room = currentRoom();
        MonsterInstance target = nearestAdjacentMonster(room);
        if (target == null) {
            addLog("No monster nearby.");
            return;
        }

        int playerDamage = player.attackPower();
        target.monster().takeDamage(playerDamage);
        addLog("You hit " + target.monster().name() + " for " + playerDamage + ".");
        if (!target.isAlive()) {
            addLog(target.monster().name() + " is defeated.");
            return;
        }

        player.takeDamage(target.monster().attack());
        addLog(target.monster().name() + " hit back for " + target.monster().attack() + ".");
        if (!player.isAlive()) {
            addLog("GAME OVER");
        }
    }

    private MonsterInstance nearestAdjacentMonster(Room room) {
        for (MonsterInstance monster : room.monsters()) {
            if (!monster.isAlive()) {
                continue;
            }
            int dx = Math.abs(playerX - monster.x());
            int dy = Math.abs(playerY - monster.y());
            if (dx <= 1 && dy <= 1) {
                return monster;
            }
        }
        return null;
    }

    private void openChest() {
        Room room = currentRoom();
        for (Chest chest : room.chests()) {
            int dx = Math.abs(playerX - chest.x());
            int dy = Math.abs(playerY - chest.y());
            if (dx <= 1 && dy <= 1) {
                List<Item> loot = chest.open();
                if (loot.isEmpty()) {
                    addLog("Chest is empty.");
                    return;
                }
                player.addItems(loot);
                addLog("Loot: " + loot);
                return;
            }
        }
        addLog("No chest nearby.");
    }

    private Room currentRoom() {
        String key = roomX + ":" + roomY;
        return rooms.computeIfAbsent(key, ignored -> generator.generate(roomX, roomY));
    }

    private RoomTexturePack currentRoomTextures() {
        String key = roomX + ":" + roomY;
        return roomTexturePacks.computeIfAbsent(key, ignored -> createRoomTexturePack(roomX, roomY));
    }

    private RoomTexturePack createRoomTexturePack(int x, int y) {
        int theme = Math.floorMod(x * 17 + y * 37, 5);
        Color floorA = switch (theme) {
            case 0 -> new Color(0.32f, 0.30f, 0.36f, 1f);
            case 1 -> new Color(0.24f, 0.34f, 0.32f, 1f);
            case 2 -> new Color(0.34f, 0.28f, 0.24f, 1f);
            case 3 -> new Color(0.28f, 0.24f, 0.34f, 1f);
            default -> new Color(0.20f, 0.30f, 0.36f, 1f);
        };
        Color wallA = floorA.cpy().mul(0.52f, 0.52f, 0.52f, 1f);
        Color doorA = new Color(0.50f, 0.34f, 0.18f, 1f);

        Map<TileType, Texture> tiles = new EnumMap<>(TileType.class);
        tiles.put(TileType.FLOOR, createTileTexture(floorA, floorA.cpy().mul(1.12f, 1.12f, 1.12f, 1f)));
        tiles.put(TileType.WALL, createTileTexture(wallA, wallA.cpy().mul(1.24f, 1.24f, 1.24f, 1f)));
        tiles.put(TileType.TRAP, createTileTexture(new Color(0.3f, 0.1f, 0.1f, 1f), new Color(0.7f, 0.15f, 0.15f, 1f)));
        Texture door = createDoorTexture(doorA);
        tiles.put(TileType.DOOR_NORTH, door);
        tiles.put(TileType.DOOR_SOUTH, door);
        tiles.put(TileType.DOOR_EAST, door);
        tiles.put(TileType.DOOR_WEST, door);
        return new RoomTexturePack(tiles);
    }

    private Texture createTileTexture(Color base, Color detail) {
        Pixmap pixmap = new Pixmap(TILE_TEXTURE_SIZE, TILE_TEXTURE_SIZE, Pixmap.Format.RGBA8888);
        pixmap.setColor(base);
        pixmap.fill();
        pixmap.setColor(detail);
        for (int y = 0; y < TILE_TEXTURE_SIZE; y += 8) {
            pixmap.drawLine(0, y, TILE_TEXTURE_SIZE - 1, y);
        }
        for (int x = 0; x < TILE_TEXTURE_SIZE; x += 8) {
            pixmap.drawLine(x, 0, x, TILE_TEXTURE_SIZE - 1);
        }
        for (int i = 0; i < 20; i++) {
            int px = (i * 17 + (int) (base.r * 1000)) % TILE_TEXTURE_SIZE;
            int py = (i * 29 + (int) (base.g * 1000)) % TILE_TEXTURE_SIZE;
            pixmap.fillRectangle(px, py, 2, 2);
        }
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private Texture createDoorTexture(Color color) {
        Pixmap pixmap = new Pixmap(TILE_TEXTURE_SIZE, TILE_TEXTURE_SIZE, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fillRectangle(8, 4, 48, 56);
        pixmap.setColor(color.cpy().mul(1.2f, 1.2f, 1.2f, 1f));
        pixmap.fillRectangle(40, 30, 6, 6);
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private Texture createPlayerTexture() {
        Pixmap pixmap = new Pixmap(TILE_TEXTURE_SIZE, TILE_TEXTURE_SIZE, Pixmap.Format.RGBA8888);
        pixmap.setColor(0f, 0f, 0f, 0f);
        pixmap.fill();
        pixmap.setColor(0.18f, 0.88f, 0.24f, 1f);
        pixmap.fillRectangle(16, 12, 32, 36);
        pixmap.setColor(0.1f, 0.65f, 0.18f, 1f);
        pixmap.fillRectangle(20, 48, 24, 12);
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private Texture createMonsterTexture(int variant) {
        Color body = switch (variant) {
            case 0 -> new Color(0.75f, 0.2f, 0.22f, 1f);
            case 1 -> new Color(0.2f, 0.7f, 0.28f, 1f);
            case 2 -> new Color(0.25f, 0.45f, 0.8f, 1f);
            case 3 -> new Color(0.7f, 0.32f, 0.75f, 1f);
            default -> new Color(0.8f, 0.55f, 0.22f, 1f);
        };
        Pixmap pixmap = new Pixmap(TILE_TEXTURE_SIZE, TILE_TEXTURE_SIZE, Pixmap.Format.RGBA8888);
        pixmap.setColor(0f, 0f, 0f, 0f);
        pixmap.fill();
        pixmap.setColor(body);
        pixmap.fillRectangle(12, 10, 40, 42);
        pixmap.setColor(Color.WHITE);
        pixmap.fillRectangle(20, 28, 6, 6);
        pixmap.fillRectangle(38, 28, 6, 6);
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private Texture createChestTexture(boolean opened) {
        Pixmap pixmap = new Pixmap(TILE_TEXTURE_SIZE, TILE_TEXTURE_SIZE, Pixmap.Format.RGBA8888);
        pixmap.setColor(0f, 0f, 0f, 0f);
        pixmap.fill();
        pixmap.setColor(0.44f, 0.24f, 0.09f, 1f);
        pixmap.fillRectangle(8, 14, 48, 40);
        pixmap.setColor(0.62f, 0.38f, 0.16f, 1f);
        if (opened) {
            pixmap.fillRectangle(8, 42, 48, 8);
            pixmap.fillRectangle(8, 24, 48, 6);
        } else {
            pixmap.fillRectangle(8, 40, 48, 12);
        }
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
        playerTexture.dispose();
        chestClosedTexture.dispose();
        chestOpenedTexture.dispose();
        panelTexture.dispose();
        for (Texture t : monsterTextures) {
            t.dispose();
        }
        for (RoomTexturePack pack : roomTexturePacks.values()) {
            for (Texture t : pack.tileTextures.values()) {
                t.dispose();
            }
        }
    }

    private record RoomTexturePack(Map<TileType, Texture> tileTextures) { }
}
