package pl.eclipse.client.modules;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.class_746;
import pl.eclipse.client.*;
import pl.eclipse.client.Module;

public final class WaypointsModule extends Module {
    public static final class Point {
        public String name;
        public double x, y, z;

        public Point(String name, double x, double y, double z) {
            this.name = name;
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    public static final List<Point> points = new ArrayList<>();
    private final Setting.Num px = num("X", 99, 0, 100, 1);
    private final Setting.Num py = num("Y", 40, 0, 100, 1);
    private final Setting.Num count = num("Max shown", 5, 1, 10, 1);

    public WaypointsModule() {
        super("Waypoints", "Lista waypointow z dystansem i strzalka. Komendy: /eclipse wp add|del|list", Category.WORLD);
    }

    @Override public void onHud(Hud h) {
        class_746 p = h.mc.field_1724;
        double ox = p.method_23317(), oz = p.method_23321();
        List<Point> sorted = new ArrayList<>(points);
        sorted.sort(Comparator.comparingDouble(w -> Math.hypot(w.x - ox, w.z - oz)));
        List<String> lines = new ArrayList<>();
        lines.add("Waypoints (" + points.size() + ")");
        if (points.isEmpty()) lines.add("/eclipse wp add <nazwa>");
        float yaw = Float.NaN;
        try {
            yaw = p.method_36454();
        } catch (Throwable ignored) {
            // bez kierunku - sam dystans
        }
        String[] arrows = {"↑", "↗", "→", "↘", "↓", "↙", "←", "↖"};
        for (int i = 0; i < Math.min(count.i(), sorted.size()); i++) {
            Point w = sorted.get(i);
            double dx = w.x - ox, dz = w.z - oz;
            String a = "";
            if (!Float.isNaN(yaw)) {
                double target = Math.toDegrees(Math.atan2(-dx, dz));
                double rel = (((target - yaw) % 360) + 360) % 360;
                a = arrows[(int) Math.round(rel / 45.0) % 8] + " ";
            }
            lines.add(a + w.name + " " + Math.round(Math.hypot(dx, dz)) + "m");
        }
        h.box(px, py, lines);
    }
}
