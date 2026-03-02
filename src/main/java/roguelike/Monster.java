package roguelike;

public class Monster {
    private final String name;
    private final int attack;
    private int health;

    public Monster(String name, int health, int attack) {
        this.name = name;
        this.health = health;
        this.attack = attack;
    }

    public String name() {
        return name;
    }

    public int attack() {
        return attack;
    }

    public int health() {
        return health;
    }

    public boolean isAlive() {
        return health > 0;
    }

    public void takeDamage(int value) {
        health -= value;
    }
}
