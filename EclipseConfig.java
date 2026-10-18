package pl.eclipse.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import net.fabricmc.loader.api.FabricLoader;
import pl.eclipse.client.modules.WaypointsModule;

/** Zapis i odczyt konfiguracji: config/eclipse_client.json. */
public final class EclipseConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private EclipseConfig() {}

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("eclipse_client.json");
    }

    public static synchronized void load() {
        Path p = path();
        if (!Files.exists(p)) return;
        try (Reader r = Files.newBufferedReader(p, StandardCharsets.UTF_8)) {
            JsonObject root = GSON.fromJson(r, JsonObject.class);
            if (root == null) return;
            JsonObject mods = root.has("modules") ? root.getAsJsonObject("modules") : new JsonObject();
            for (Module m : ModuleManager.modules) {
                if (!mods.has(m.name)) continue;
                JsonObject o = mods.getAsJsonObject(m.name);
                if (o.has("key")) m.key = o.get("key").getAsInt();
                if (o.has("enabled") && !m.alwaysOn) m.enabled = o.get("enabled").getAsBoolean();
                if (o.has("settings")) {
                    JsonObject so = o.getAsJsonObject("settings");
                    for (Setting s : m.settings) {
                        if (!so.has(s.name)) continue;
                        JsonPrimitive jp = so.get(s.name).getAsJsonPrimitive();
                        s.load(jp.isBoolean() ? (Object) jp.getAsBoolean() : jp.isNumber() ? (Object) jp.getAsDouble() : jp.getAsString());
                    }
                }
            }
            WaypointsModule.points.clear();
            if (root.has("waypoints")) {
                for (JsonElement e : root.getAsJsonArray("waypoints")) {
                    JsonObject w = e.getAsJsonObject();
                    WaypointsModule.points.add(new WaypointsModule.Point(w.get("name").getAsString(),
                            w.get("x").getAsDouble(), w.get("y").getAsDouble(), w.get("z").getAsDouble()));
                }
            }
        } catch (Exception e) {
            EclipseClient.LOGGER.error("Nie udalo sie wczytac konfiguracji, uzywam domyslnych", e);
            try {
                Files.move(p, p.resolveSibling("eclipse_client.json.uszkodzony"), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ignored) {
                // nic wiecej nie zrobimy
            }
        }
    }

    public static synchronized void save() {
        JsonObject root = new JsonObject();
        JsonObject mods = new JsonObject();
        for (Module m : ModuleManager.modules) {
            JsonObject o = new JsonObject();
            o.addProperty("enabled", m.enabled);
            o.addProperty("key", m.key);
            JsonObject so = new JsonObject();
            for (Setting s : m.settings) {
                Object v = s.save();
                if (v instanceof Boolean b) so.addProperty(s.name, b);
                else if (v instanceof Number n) so.addProperty(s.name, n);
                else so.addProperty(s.name, String.valueOf(v));
            }
            o.add("settings", so);
            mods.add(m.name, o);
        }
        root.add("modules", mods);
        JsonArray wps = new JsonArray();
        for (WaypointsModule.Point w : WaypointsModule.points) {
            JsonObject o = new JsonObject();
            o.addProperty("name", w.name);
            o.addProperty("x", w.x);
            o.addProperty("y", w.y);
            o.addProperty("z", w.z);
            wps.add(o);
        }
        root.add("waypoints", wps);
        Path p = path();
        Path tmp = p.resolveSibling("eclipse_client.json.tmp");
        try {
            Files.createDirectories(p.getParent());
            try (Writer w = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8)) {
                GSON.toJson(root, w);
            }
            try {
                Files.move(tmp, p, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException atomicFailed) {
                Files.move(tmp, p, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            EclipseClient.LOGGER.error("Nie udalo sie zapisac konfiguracji", e);
        }
    }
}
