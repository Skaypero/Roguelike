package roguelike;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Game extends JFrame {
    private final Player player = new Player();
    private final WorldGenerator generator = new WorldGenerator(System.currentTimeMillis());
    private final Map<String, Room> rooms = new HashMap<>();
    private final List<String> combatLog = new ArrayList<>();

    private int x = 0;
    private int y = 0;

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            Game game = new Game();
            game.setVisible(true);
        });
    }

    public Game() {
        super("Java Roguelike (Swing)");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        GamePanel panel = new GamePanel();
        panel.setPreferredSize(new Dimension(920, 620));
        setLayout(new BorderLayout());
        add(panel, BorderLayout.CENTER);

        bindControls(panel);
        pack();
        setLocationRelativeTo(null);

        log("Добро пожаловать! Стрелки/WASD: движение, F: атака, E: сундук, Q: зелье.");
    }

    private void bindControls(JPanel panel) {
        bind(panel, "UP", () -> move(Direction.NORTH));
        bind(panel, "DOWN", () -> move(Direction.SOUTH));
        bind(panel, "LEFT", () -> move(Direction.WEST));
        bind(panel, "RIGHT", () -> move(Direction.EAST));
        bind(panel, "W", () -> move(Direction.NORTH));
        bind(panel, "S", () -> move(Direction.SOUTH));
        bind(panel, "A", () -> move(Direction.WEST));
        bind(panel, "D", () -> move(Direction.EAST));

        bind(panel, "F", this::fight);
        bind(panel, "E", this::openChest);
        bind(panel, "Q", this::usePotion);
    }

    private void bind(JPanel panel, String key, Runnable action) {
        String name = "action_" + key;
        panel.getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(key), name);
        panel.getActionMap().put(name, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!player.isAlive()) {
                    return;
                }
                action.run();
                panel.repaint();
            }
        });
    }

    private void move(Direction direction) {
        x += direction.dx();
        y += direction.dy();
        Room room = currentRoom();
        log("Вы вошли в комнату [" + x + ", " + y + "].");
        if (room.monster() != null && room.monster().isAlive()) {
            log("Вас встречает " + room.monster().name() + " (HP: " + room.monster().health() + ").");
        }
    }

    private void fight() {
        Room room = currentRoom();
        Monster monster = room.monster();
        if (monster == null || !monster.isAlive()) {
            log("В этой комнате нет живых монстров.");
            return;
        }

        int damage = player.attackPower();
        monster.takeDamage(damage);
        log("Вы нанесли " + damage + " урона монстру " + monster.name() + ".");

        if (!monster.isAlive()) {
            log("Монстр побеждён!");
            return;
        }

        player.takeDamage(monster.attack());
        log(monster.name() + " наносит " + monster.attack() + " урона. HP героя: " + player.health());
        if (!player.isAlive()) {
            log("Герой пал. Нажмите Alt+F4, чтобы закрыть окно.");
        }
    }

    private void openChest() {
        Room room = currentRoom();
        Chest chest = room.chest();
        if (chest == null) {
            log("Сундука в комнате нет.");
            return;
        }

        List<Item> loot = chest.open();
        if (loot.isEmpty()) {
            log("Сундук уже открыт.");
            return;
        }

        player.addItems(loot);
        log("Вы открыли сундук и нашли: " + loot);
    }

    private void usePotion() {
        boolean used = player.usePotionIfAny();
        log(used ? "Вы использовали лечащий предмет." : "В инвентаре нет лечащих предметов.");
    }

    private Room currentRoom() {
        String key = x + ":" + y;
        return rooms.computeIfAbsent(key, ignored -> generator.generate(x, y));
    }

    private void log(String message) {
        combatLog.add(0, message);
        if (combatLog.size() > 14) {
            combatLog.remove(combatLog.size() - 1);
        }
    }

    private class GamePanel extends JPanel {
        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics;
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            g.setColor(new Color(20, 20, 28));
            g.fillRect(0, 0, getWidth(), getHeight());

            Room room = currentRoom();

            g.setColor(Color.WHITE);
            g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 21));
            g.drawString("Roguelike (не консольная версия)", 25, 40);

            g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
            g.drawString("Комната: [" + x + ", " + y + "]", 25, 80);
            g.drawString("HP героя: " + player.health(), 25, 110);
            g.drawString("Сила атаки: " + player.attackPower(), 25, 140);
            g.drawString("Инвентарь: " + player.inventory().size() + " предметов", 25, 170);

            g.drawString("Управление: WASD/стрелки - ходить, F - атаковать, E - сундук, Q - зелье", 25, 210);

            g.setColor(new Color(60, 90, 130));
            g.fillRoundRect(25, 250, 360, 220, 16, 16);
            g.setColor(Color.WHITE);
            g.drawString("Состояние комнаты", 40, 280);
            g.drawString("Двери: север, восток, юг, запад", 40, 310);
            if (room.monster() != null && room.monster().isAlive()) {
                g.drawString("Монстр: " + room.monster().name() + " (HP " + room.monster().health() + ")", 40, 340);
            } else {
                g.drawString("Монстр: нет", 40, 340);
            }
            if (room.chest() != null && !room.chest().isOpened()) {
                g.drawString("Сундук: закрыт", 40, 370);
            } else if (room.chest() != null) {
                g.drawString("Сундук: открыт", 40, 370);
            } else {
                g.drawString("Сундук: нет", 40, 370);
            }

            g.setColor(new Color(45, 45, 45));
            g.fillRoundRect(420, 70, 470, 530, 16, 16);
            g.setColor(Color.WHITE);
            g.drawString("Журнал событий", 440, 100);

            int lineY = 130;
            for (String message : combatLog) {
                g.drawString("- " + message, 440, lineY);
                lineY += 30;
            }

            if (!player.isAlive()) {
                g.setColor(new Color(170, 20, 40));
                g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 36));
                g.drawString("GAME OVER", 325, 520);
            }
        }
    }
}
