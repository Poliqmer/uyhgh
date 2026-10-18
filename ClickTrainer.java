package pl.eclipse.client;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_310;

/** Moduł treningowy: 10-sekundowy test CPS (LPM) z porównaniem do celu. */
public final class ClickTrainer extends Module {
    private static ClickTrainer self;
    private final Setting.Num target = num("Target CPS", 10, 4, 20, 1);
    private final Setting.Num duration = num("Test seconds", 10, 3, 30, 1);
    private final Setting.Num px = num("X", 50, 0, 100, 1);
    private final Setting.Num py = num("Y", 12, 0, 100, 1);
    private long startNs;
    private int count;
    private int state; // 0 = czeka, 1 = trwa, 2 = wynik
    private double result;
    private long doneNs;

    public ClickTrainer() {
        super("Click Trainer", "Test CPS: kliknij LPM, test startuje przy pierwszym kliknieciu", Category.COMBAT);
        self = this;
    }

    static void onLeft(long now) {
        ClickTrainer t = self;
        if (t == null || !t.enabled) return;
        if (t.state == 2 && now - t.doneNs > 3_000_000_000L) t.state = 0;
        if (t.state == 0) {
            t.state = 1;
            t.startNs = now;
            t.count = 0;
        }
        if (t.state == 1) t.count++;
    }

    @Override public void onEnable() { state = 0; }

    @Override public void onHud(Hud h) {
        long now = System.nanoTime();
        if (state == 1 && now - startNs >= (long) (duration.value * 1e9)) {
            state = 2;
            doneNs = now;
            result = count / duration.value;
        }
        List<String> l = new ArrayList<>();
        l.add("Click Trainer - cel " + target.i() + " CPS");
        if (state == 0) {
            l.add("Kliknij LPM, aby zaczac test");
        } else if (state == 1) {
            double left = duration.value - (now - startNs) / 1e9;
            double avg = count / Math.max(0.2, (now - startNs) / 1e9);
            l.add(String.format("Czas: %.1fs | klikniec: %d", Math.max(0, left), count));
            l.add(String.format("Srednio: %.1f CPS %s", avg, bar(avg, target.value)));
        } else {
            l.add(String.format("Wynik: %.2f CPS (%d klikniec)", result, count));
            l.add(result >= target.value ? "Cel osiagniety" : String.format("Brakuje %.1f CPS", target.value - result));
        }
        h.box(px, py, l);
    }

    private static String bar(double v, double goal) {
        int n = (int) Math.max(0, Math.min(20, Math.round(v / goal * 10)));
        return "[" + "|".repeat(n) + ".".repeat(20 - n) + "]";
    }
}
