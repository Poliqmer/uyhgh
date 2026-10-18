package pl.eclipse.client.modules;

import java.util.List;
import pl.eclipse.client.*;
import pl.eclipse.client.Module;

public final class CombatHudModule extends Module {
    private final Setting.Num px = num("X", 99, 0, 100, 1);
    private final Setting.Num py = num("Y", 1, 0, 100, 1);

    public CombatHudModule() {
        super("Combat HUD", "CPS LPM/PPM, rekord CPS i licznik klikniec w sesji", Category.COMBAT);
    }

    @Override public void onEnable() { Clicks.reset(); }

    @Override public void onHud(Hud h) {
        h.box(px, py, List.of(
                "Combat HUD",
                "CPS: " + Clicks.cpsLeft() + " | " + Clicks.cpsRight(),
                "Rekord CPS: " + Clicks.leftPeak,
                "Klikniecia: " + Clicks.leftTotal + " / " + Clicks.rightTotal));
    }
}
