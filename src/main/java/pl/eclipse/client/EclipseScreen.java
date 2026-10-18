package pl.eclipse.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.IntConsumer;
import net.minecraft.class_2561;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_342;
import net.minecraft.class_437;

/**
 * Click GUI: animowane otwieranie/zamykanie, kategorie, wyszukiwarka, przełączniki, suwaki, wybór koloru i keybindy.
 * Kliknięcia i klawisze czytane są z GLFW (jak w AutoRynku), więc nie zależą od sygnatur zdarzeń myszy w grze.
 */
public final class EclipseScreen extends class_437 {
    private static final int PW = 440, PH = 290, SIDE = 92, HEAD = 30, FOOT = 16, ROW = 24, GAP = 3;
    private static final int[] SWATCHES = {0x7C5CFF, 0x3B82F6, 0x00D4FF, 0x00FFB3, 0x7CFF4D, 0xFFD166, 0xFF7A45, 0xFF4D6D};

    private static final class Hit {
        int x, y, w, h;
        Runnable left, right;
        IntConsumer drag;
        String tip;
    }

    private float open = 0f;
    private boolean closing = false;
    private long lastFrame = System.nanoTime();
    private Category cat = Category.HUD;
    private String query = "";
    private Module expanded;
    private float scroll, scrollTarget;
    private int contentH;
    private Module capturing;
    private final boolean[] keyPrev = new boolean[400];
    private boolean pl, pr;
    private Hit drag;
    private final List<Hit> hits = new ArrayList<>();
    private final Map<Object, Float> anim = new HashMap<>();
    private class_342 search;
    private float indY = -1;
    private int clipTop, clipBottom;
    private int px, py, pw, ph;
    private String footerTip = "";

    public EclipseScreen() {
        super(class_2561.method_43470("Eclipse Client"));
    }

    @Override
    protected void method_25426() {
        Draw.setFont(this.field_22793);
        pw = Math.min(PW, this.field_22789 - 16);
        ph = Math.min(PH, this.field_22790 - 16);
        px = (this.field_22789 - pw) / 2;
        py = (this.field_22790 - ph) / 2;
        search = new class_342(this.field_22793, px + pw - 138, py + 5, 128, 20, class_2561.method_43470("Search"));
        search.method_1880(32);
        search.method_47404(class_2561.method_43470("Search..."));
        search.method_1852(query);
        search.method_1863(s -> {
            query = s == null ? "" : s.trim().toLowerCase(Locale.ROOT);
            scrollTarget = 0;
        });
        method_37063(search);
    }

    @Override
    public void method_25432() {
        EclipseConfig.save();
    }

    @Override
    public void method_25419() {
        if (capturing != null) {
            capturing = null;
            return;
        }
        if (!closing) {
            closing = true;
            EclipseConfig.save();
            return;
        }
        super.method_25419();
    }

    /** Kółko myszy (jeśli sygnatura w tej wersji się zgadza); w przeciwnym razie działają strzałki góra/dół. */
    public boolean method_25401(double x, double y, double horizontal, double vertical) {
        scrollTarget -= (float) (vertical * 28);
        return true;
    }

    private static float ease(float t) {
        t = Math.max(0, Math.min(1, t));
        float u = 1 - t;
        return 1 - u * u * u;
    }

    private float a;

    private int ca(int argb) {
        return Draw.alpha(argb, a);
    }

    private int acc() {
        return 0xFF000000 | Interface.accent.rgb();
    }

    private float anim(Object key, boolean on, float dt) {
        float v = anim.getOrDefault(key, on ? 1f : 0f);
        float target = on ? 1f : 0f;
        v += (target - v) * Math.min(1f, dt * 16f);
        anim.put(key, v);
        return v;
    }

    // --- rysowanie z przycinaniem do obszaru zawartości ---
    private void fc(class_332 c, int x1, int y1, int x2, int y2, int col) {
        y1 = Math.max(y1, clipTop);
        y2 = Math.min(y2, clipBottom);
        if (y2 > y1) Draw.fill(c, x1, y1, x2, y2, ca(col));
    }

    private void tc(class_332 c, String s, int x, int y, int col) {
        if (y >= clipTop && y + 8 <= clipBottom) Draw.text(c, this.field_22793, s, x, y, ca(col));
    }

    private boolean visible(int y, int h) {
        return y + h > clipTop && y < clipBottom;
    }

    private Hit hit(int x, int y, int w, int h, String tip) {
        Hit hh = new Hit();
        hh.x = x;
        hh.y = Math.max(y, clipTop);
        hh.w = w;
        hh.h = Math.min(y + h, clipBottom) - hh.y;
        hh.tip = tip;
        if (hh.h > 0) hits.add(hh);
        return hh;
    }

    private void sw(class_332 c, int x, int y, float t) {
        int track = Draw.lerp(0xFF3A3F52, acc(), t);
        fc(c, x, y, x + 26, y + 12, track);
        int kx = x + 2 + (int) (14 * t);
        fc(c, kx, y + 2, kx + 10, y + 10, 0xFFFFFFFF);
    }

    @Override
    public void method_25394(class_332 c, int mx, int my, float delta) {
        long now = System.nanoTime();
        float dt = Math.min(0.05f, (now - lastFrame) / 1_000_000_000f);
        lastFrame = now;
        float speed = (float) Interface.animSpeed.value;
        open = Math.max(0f, Math.min(1f, open + (closing ? -1 : 1) * dt * speed / 0.18f));
        if (closing && open <= 0f) {
            super.method_25419();
            return;
        }
        a = ease(open);
        pw = Math.min(PW, this.field_22789 - 16);
        ph = Math.min(PH, this.field_22790 - 16);
        px = (this.field_22789 - pw) / 2;
        py = (this.field_22790 - ph) / 2 + (int) ((1 - a) * 18);
        hits.clear();
        footerTip = "";

        // tło + panel
        Draw.fill(c, 0, 0, this.field_22789, this.field_22790, ca(0x99000000));
        int accDim = Draw.lerp(acc(), 0xFF000000, 0.55f);
        Draw.fill(c, px - 1, py - 1, px + pw + 1, py + ph + 1, ca(accDim));
        Draw.vgrad(c, px, py, pw, ph, ca(0xFF171A26), ca(0xFF0B0D13));

        // zawartość (przycinana), potem nakładki nagłówka/stopki
        int cx = px + SIDE + 8, cw = pw - SIDE - 16;
        clipTop = py + HEAD + 4;
        clipBottom = py + ph - FOOT - 2;
        List<Module> list = new ArrayList<>();
        for (Module m : ModuleManager.modules) {
            if (query.isEmpty() ? m.category == cat
                    : (m.name.toLowerCase(Locale.ROOT).contains(query) || m.description.toLowerCase(Locale.ROOT).contains(query))) {
                list.add(m);
            }
        }
        scrollTarget = Math.max(0, Math.min(Math.max(0, contentH - (clipBottom - clipTop)), scrollTarget));
        if (Input.key(265)) scrollTarget -= 260 * dt;
        if (Input.key(264)) scrollTarget += 260 * dt;
        scroll += (scrollTarget - scroll) * Math.min(1f, dt * 14f);
        int y = clipTop - Math.round(scroll);
        int startY = y;
        for (Module m : list) {
            y = drawModule(c, m, cx, y, cw, dt, mx, my);
            y += GAP;
        }
        if (list.isEmpty()) tc(c, "Brak wynikow.", cx + 4, clipTop + 6, 0xFF8A90A5);
        contentH = y - startY;

        // nagłówek
        Draw.vgrad(c, px, py, pw, HEAD, ca(0xFF222638), ca(0xFF161926));
        Draw.hgrad(c, px, py + HEAD - 1, (int) (pw * a), 1, ca(acc()), ca(Draw.alpha(acc(), 0.0f)));
        Draw.text(c, this.field_22793, "ECLIPSE", px + 10, py + 11, ca(acc()));
        Draw.text(c, this.field_22793, "CLIENT", px + 10 + Draw.width(this.field_22793, "ECLIPSE ") , py + 11, ca(0xFF8A90A5));
        if (open > 0.97f && !closing) {
            search.method_25394(c, mx, my, delta);
        } else {
            Draw.fill(c, px + pw - 138, py + 5, px + pw - 10, py + 25, ca(0xFF0E1018));
        }

        // pasek boczny
        Draw.fill(c, px, py + HEAD, px + SIDE, py + ph - FOOT, ca(0x77000000));
        Category[] cats = Category.values();
        int selIdx = 0;
        for (int i = 0; i < cats.length; i++) if (cats[i] == cat) selIdx = i;
        float targetY = py + HEAD + 8 + selIdx * 26;
        indY = indY < 0 ? targetY : indY + (targetY - indY) * Math.min(1f, dt * 14f);
        if (query.isEmpty()) {
            Draw.fill(c, px, (int) indY, px + 2, (int) indY + 22, ca(acc()));
            Draw.fill(c, px + 2, (int) indY, px + SIDE, (int) indY + 22, ca(Draw.alpha(acc(), 0.18f)));
        }
        for (int i = 0; i < cats.length; i++) {
            final Category k = cats[i];
            int ry = py + HEAD + 8 + i * 26;
            float hov = anim("cat" + i, mx >= px && mx < px + SIDE && my >= ry && my < ry + 22, dt);
            if (hov > 0.01f) Draw.fill(c, px + 2, ry, px + SIDE, ry + 22, ca(Draw.alpha(0xFFFFFFFF, 0.06f * hov)));
            int on = 0;
            int total = 0;
            for (Module m : ModuleManager.modules) if (m.category == k) {
                total++;
                if (m.enabled && !m.alwaysOn) on++;
            }
            boolean sel = query.isEmpty() && k == cat;
            Draw.text(c, this.field_22793, k.label, px + 12, ry + 7, ca(sel ? 0xFFFFFFFF : 0xFFB0B6CA));
            String cnt = on + "/" + total;
            Draw.text(c, this.field_22793, cnt, px + SIDE - 8 - Draw.width(this.field_22793, cnt), ry + 7, ca(0xFF6E748A));
            Hit h = new Hit();
            h.x = px;
            h.y = ry;
            h.w = SIDE;
            h.h = 22;
            h.left = () -> {
                cat = k;
                query = "";
                if (search != null) search.method_1852("");
                scrollTarget = 0;
                expanded = null;
            };
            h.tip = k.label;
            hits.add(h);
        }

        // stopka
        Draw.fill(c, px, py + ph - FOOT, px + pw, py + ph, ca(0xFF10121A));
        String tip = footerTip.isEmpty()
                ? "LPM: wlacz/wylacz | PPM lub strzalka: ustawienia | kolko/strzalki: przewijanie | ESC: zamknij"
                : footerTip;
        Draw.text(c, this.field_22793, tip, px + 8, py + ph - FOOT + 4, ca(0xFF8A90A5));

        handleInput(mx, my);
    }

    /** Rysuje wiersz modułu (i jego ustawienia, jeśli rozwinięty). Zwraca y za końcem bloku. */
    private int drawModule(class_332 c, Module m, int x, int y, int w, float dt, int mx, int my) {
        boolean exp = expanded == m;
        int blockH = ROW;
        if (exp) blockH += settingsHeight(m);
        if (!visible(y, blockH)) return y + blockH;
        boolean over = mx >= x && mx < x + w && my >= Math.max(y, clipTop) && my < Math.min(y + ROW, clipBottom);
        float hov = anim(m.name + "#h", over, dt);
        float t = anim(m, m.enabled, dt);
        int base = Draw.lerp(0xFF1B1F2B, 0xFF262B3D, hov);
        base = Draw.lerp(base, Draw.alpha(acc(), 1f), 0.10f * t);
        fc(c, x, y, x + w, y + ROW, base);
        fc(c, x, y, x + 2, y + ROW, Draw.lerp(0xFF2B3045, acc(), t));
        tc(c, m.name, x + 10, y + 8, m.failed ? 0xFFFF6B6B : 0xFFF0F2FA);
        int rx = x + w - 6;
        // chevron
        String chev = exp ? "^" : "v";
        tc(c, chev, rx - 8, y + 8, 0xFF8A90A5);
        Hit chevHit = hit(rx - 16, y, 16, ROW, "Ustawienia: " + m.name);
        chevHit.left = () -> expanded = (expanded == m) ? null : m;
        rx -= 20;
        if (!m.alwaysOn) {
            sw(c, rx - 26, y + 6, t);
            rx -= 32;
            if (m.key >= 0) {
                String kn = Input.keyName(m.key);
                tc(c, kn, rx - Draw.width(this.field_22793, kn), y + 8, 0xFF8A90A5);
            }
        }
        Hit row = hit(x, y, w - 20, ROW, m.failed ? "Blad: " + m.failReason : m.description);
        row.left = m.alwaysOn ? () -> expanded = (expanded == m ? null : m) : m::toggle;
        row.right = () -> expanded = (expanded == m) ? null : m;

        int cy = y + ROW;
        if (exp) {
            fc(c, x, cy, x + w, cy + settingsHeight(m), 0xFF131621);
            int ix = x + 12, iw = w - 24;
            if (!m.alwaysOn) {
                tc(c, "Keybind", ix, cy + 7, 0xFFB0B6CA);
                String label = capturing == m ? "nacisnij klawisz..." : "[" + Input.keyName(m.key) + "]";
                int bw = Math.max(60, Draw.width(this.field_22793, label) + 12);
                int bx = ix + iw - bw;
                fc(c, bx, cy + 3, bx + bw, cy + 17, capturing == m ? Draw.alpha(acc(), 0.5f) | 0xFF000000 : 0xFF232838);
                tc(c, label, bx + (bw - Draw.width(this.field_22793, label)) / 2, cy + 6, 0xFFFFFFFF);
                Hit kh = hit(bx, cy + 3, bw, 14, "Kliknij i nacisnij klawisz (Backspace = brak)");
                kh.left = () -> arm(m);
                cy += 20;
            }
            for (Setting s : m.settings) {
                if (s instanceof Setting.Bool b) {
                    tc(c, b.name, ix, cy + 5, 0xFFD5D9E8);
                    sw(c, ix + iw - 26, cy + 4, anim(b, b.value, dt));
                    Hit hh = hit(ix, cy, iw, 20, b.name);
                    hh.left = () -> b.value = !b.value;
                    cy += 20;
                } else if (s instanceof Setting.Num n) {
                    tc(c, n.name, ix, cy + 3, 0xFFD5D9E8);
                    String v = n.step >= 1 ? String.valueOf(n.i()) : String.format(Locale.ROOT, "%.2f", n.value);
                    tc(c, v, ix + iw - Draw.width(this.field_22793, v), cy + 3, 0xFFFFFFFF);
                    int tx = ix, tw = iw, ty = cy + 15;
                    fc(c, tx, ty, tx + tw, ty + 4, 0xFF2A2F42);
                    float f = (float) ((n.value - n.min) / (n.max - n.min));
                    fc(c, tx, ty, tx + (int) (tw * f), ty + 4, acc());
                    fc(c, tx + (int) (tw * f) - 2, ty - 2, tx + (int) (tw * f) + 2, ty + 6, 0xFFFFFFFF);
                    Hit hh = hit(tx - 2, cy + 10, tw + 4, 14, n.name);
                    hh.drag = mxx -> n.set(n.min + (n.max - n.min) * Math.max(0, Math.min(1, (mxx - tx) / (double) tw)));
                    cy += 26;
                } else if (s instanceof Setting.Color col) {
                    tc(c, col.name, ix, cy + 3, 0xFFD5D9E8);
                    for (int i = 0; i < SWATCHES.length; i++) {
                        final int rgb = SWATCHES[i];
                        int sx = ix + iw - (SWATCHES.length - i) * 15;
                        fc(c, sx, cy + 1, sx + 12, cy + 13, 0xFF000000 | rgb);
                        if ((col.rgb() & 0xFFFFFF) == rgb) {
                            fc(c, sx, cy + 13, sx + 12, cy + 14, 0xFFFFFFFF);
                        }
                        hit(sx, cy + 1, 12, 12, col.name).left = () -> col.setRgb(rgb);
                    }
                    int tx = ix, tw = iw, ty = cy + 20;
                    int segs = 36;
                    for (int i = 0; i < segs; i++) {
                        int x1 = tx + tw * i / segs, x2 = tx + tw * (i + 1) / segs;
                        fc(c, x1, ty, x2, ty + 6, 0xFF000000 | Setting.Color.hsb(i / (float) segs, 1f, 1f));
                    }
                    int kx = tx + (int) (tw * col.hue);
                    fc(c, kx - 1, ty - 2, kx + 2, ty + 8, 0xFFFFFFFF);
                    int pv = 0xFF000000 | col.rgb();
                    fc(c, ix, cy + 31, ix + iw, cy + 33, pv);
                    Hit hh = hit(tx - 2, ty - 3, tw + 4, 12, col.name);
                    hh.drag = mxx -> {
                        col.hue = (float) Math.max(0, Math.min(0.999, (mxx - tx) / (double) tw));
                        if (col.sat < 0.2f) col.sat = 0.75f;
                        if (col.bri < 0.5f) col.bri = 0.9f;
                    };
                    cy += 38;
                }
            }
        }
        return y + blockH;
    }

    private int settingsHeight(Module m) {
        int h = m.alwaysOn ? 0 : 20;
        for (Setting s : m.settings) {
            h += s instanceof Setting.Num ? 26 : s instanceof Setting.Color ? 38 : 20;
        }
        return h + 4;
    }

    private void arm(Module m) {
        capturing = m;
        for (int k = 32; k < keyPrev.length; k++) keyPrev[k] = Input.key(k);
    }

    private void handleInput(int mx, int my) {
        boolean l = Input.mouse(0), r = Input.mouse(1);
        boolean live = open > 0.9f && !closing;
        if (live) {
            Hit top = null;
            for (int i = hits.size() - 1; i >= 0; i--) {
                Hit h = hits.get(i);
                if (mx >= h.x && mx < h.x + h.w && my >= h.y && my < h.y + h.h) {
                    top = h;
                    break;
                }
            }
            if (top != null && top.tip != null) footerTip = top.tip;
            if (l && !pl && top != null) {
                if (top.drag != null) {
                    drag = top;
                    top.drag.accept(mx);
                } else if (top.left != null) {
                    top.left.run();
                }
            }
            if (r && !pr && top != null && top.right != null) top.right.run();
            if (l && drag != null && drag.drag != null) drag.drag.accept(mx);
            if (!l && drag != null) {
                drag = null;
                EclipseConfig.save();
            }
            if (capturing != null) {
                for (int k = 32; k < keyPrev.length; k++) {
                    boolean down = Input.key(k);
                    if (down && !keyPrev[k] && k != 256) {
                        capturing.key = (k == 259 || k == 261) ? -1 : k;
                        capturing = null;
                        EclipseConfig.save();
                        break;
                    }
                    keyPrev[k] = down;
                }
            }
        }
        pl = l;
        pr = r;
    }
}
