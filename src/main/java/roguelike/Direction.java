package roguelike;

public enum Direction {
    NORTH(0, 1),
    EAST(1, 0),
    SOUTH(0, -1),
    WEST(-1, 0);

    private final int dx;
    private final int dy;

    Direction(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    public int dx() {
        return dx;
    }

    public int dy() {
        return dy;
    }

    public static Direction fromInput(String raw) {
        return switch (raw.trim().toLowerCase()) {
            case "n", "north", "с", "север" -> NORTH;
            case "e", "east", "в", "восток" -> EAST;
            case "s", "south", "ю", "юг" -> SOUTH;
            case "w", "west", "з", "запад" -> WEST;
            default -> null;
        };
    }
}
