package pl.eclipse.client.modules;

import java.util.List;
import pl.eclipse.client.*;
import pl.eclipse.client.Module;

public final class FpsModule extends Module {
    private final Setting.Num px = num("X", 1, 0, 100, 1);
    private final Setting.Num py = num("Y", 1, 0, 100, 1);

    public FpsModule() {
        super("FPS", "Licznik klatek na sekunde", Category.HUD);
        enabled = true;
    }

    @Override public void onHud(Hud h) {
        h.box(px, py, List.of("FPS: " + ModuleManager.fps));
    }
}
