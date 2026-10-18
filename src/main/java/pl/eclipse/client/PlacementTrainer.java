package pl.eclipse.client;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/** Moduł treningowy do stawiania bloków: mierzy rytm kliknięć PPM (tempo, odstępy, równomierność). */
public final class PlacementTrainer extends Module {
    private static PlacementTrainer self;
    private final Setting.Num window = num("Window seconds", 5, 2, 15, 1);
    private final Setting.Num px = num("X", 50, 0, 100, 1);
    private final Setting.Num py = num("Y", 30, 0, 100, 1);
    private final Deque<Long> times = new ArrayDeque<>();
    private double bestMs = Double.MAX_VALUE;

    public PlacementTrainer() {
        super("Placement Trainer", "Mierzy rytm stawiania blokow (PPM): CPS, odstepy, stabilnosc", Category.COMBAT);
        self = this;
    }

    static void onRight(long now) {
        PlacementTrainer t = self;
        if (t == null || !t.enabled) return;
        if (!t.times.isEmpty()) {
            double ms = (now - t.times.peekLast()) / 1e6;
            if (ms > 20 && ms < 1000) t.bestMs = Math.min(t.bestMs, ms);
        }
        t.times.addLast(now);
    }

    @Override public void onEnable() {
        times.clear();
        bestMs = Double.MAX_VALUE;
    }

    @Override public void onHud(Hud h) {
        long now = System.nanoTime();
        long win = (long) (window.value * 1e9);
        while (!times.isEmpty() && now - times.peekFirst() > win) times.pollFirst();
        List<String> l = new ArrayList<>();
        l.add("Placement Trainer (" + window.i() + "s)");
        int n = times.size();
        l.add(String.format("Bloki: %d | %.1f /s", n, n / window.value));
        if (n >= 3) {
            Long[] a = times.toArray(new Long[0]);
            double sum = 0, sq = 0;
            for (int i = 1; i < a.length; i++) {
                double d = (a[i] - a[i - 1]) / 1e6;
                sum += d;
                sq += d * d;
            }
            int m = a.length - 1;
            double mean = sum / m, sd = Math.sqrt(Math.max(0, sq / m - mean * mean));
            l.add(String.format("Odstep: %.0f ms (+-%.0f)", mean, sd));
        } else {
            l.add("Stawiaj bloki PPM...");
        }
        if (bestMs < Double.MAX_VALUE) l.add(String.format("Najszybszy odstep: %.0f ms", bestMs));
        h.box(px, py, l);
    }
}
