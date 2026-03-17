package roguelike;

public record MonsterInstance(Monster monster, int x, int y, int textureVariant) {
    public boolean isAlive() {
        return monster.isAlive();
    }
}
