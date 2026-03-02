package roguelike;

public record Item(String name, ItemType type, int power) {
    @Override
    public String toString() {
        return switch (type) {
            case WEAPON -> name + " (оружие, урон +" + power + ")";
            case CONSUMABLE -> name + " (предмет, лечение " + power + ")";
            case TREASURE -> name + " (сокровище, ценность " + power + ")";
        };
    }
}
