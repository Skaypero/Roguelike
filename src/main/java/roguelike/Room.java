package roguelike;

public class Room {
    private final int x;
    private final int y;
    private final Monster monster;
    private final Chest chest;

    public Room(int x, int y, Monster monster, Chest chest) {
        this.x = x;
        this.y = y;
        this.monster = monster;
        this.chest = chest;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public Monster monster() {
        return monster;
    }

    public Chest chest() {
        return chest;
    }

    public String description() {
        StringBuilder sb = new StringBuilder();
        sb.append("Комната [").append(x).append(", ").append(y).append("]\n");
        sb.append("Есть 4 двери: север, восток, юг, запад.\n");
        if (monster != null && monster.isAlive()) {
            sb.append("Монстр: ").append(monster.name()).append(" (HP ").append(monster.health()).append(")\n");
        } else {
            sb.append("Монстров не видно.\n");
        }
        if (chest != null && !chest.isOpened()) {
            sb.append("В комнате есть сундук.\n");
        }
        return sb.toString();
    }
}
