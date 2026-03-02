package roguelike;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

public class DesktopLauncher {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Roguelike libGDX");
        config.setWindowedMode(1420, 980);
        config.useVsync(true);
        new Lwjgl3Application(new Game(), config);
    }
}
