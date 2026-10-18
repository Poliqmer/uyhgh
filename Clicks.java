package pl.eclipse.client;

import java.util.ArrayDeque;
import java.util.Deque;
import net.minecraft.class_310;

/** Zlicza kliknięcia myszy (LPM/PPM) odczytywane z GLFW - baza dla CPS, Combat HUD i modułów treningowych. */
public final class Clicks {
    public static final Deque<Long> left = new ArrayDeque<>();
    public static final Deque<Long> right = new ArrayDeque<>();
    public static int leftTotal, rightTotal, leftPeak;
    private static boolean pl, pr;

    private Clicks() {}

    /** Wołane co klatkę HUD i co tick. Liczy tylko, gdy nie ma otwartego ekranu. */
    public static void poll(class_310 mc) {
        long now = System.nanoTime();
        boolean inGame = mc.field_1755 == null;
        boolean l = inGame && Input.mouse(0), r = inGame && Input.mouse(1);
        if (l && !pl) {
            left.addLast(now);
            leftTotal++;
            ClickTrainer.onLeft(now);
        }
        if (r && !pr) {
            right.addLast(now);
            rightTotal++;
            PlacementTrainer.onRight(now);
        }
        pl = l;
        pr = r;
        trim(left, now, 1_000_000_000L);
        trim(right, now, 1_000_000_000L);
        leftPeak = Math.max(leftPeak, left.size());
    }

    private static void trim(Deque<Long> q, long now, long window) {
        while (!q.isEmpty() && now - q.peekFirst() > window) q.pollFirst();
    }

    public static int cpsLeft() { return left.size(); }

    public static int cpsRight() { return right.size(); }

    public static void reset() {
        left.clear();
        right.clear();
        leftTotal = rightTotal = leftPeak = 0;
    }
}
