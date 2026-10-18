# Eclipse Client (Fabric, Minecraft 1.21.11)

Klientowy mod Fabric: animowane Click GUI (prawy Shift albo `/eclipse`) i moduły HUD / treningowe.

## Moduły
Interface (kolor akcentu, przezroczystość, tempo animacji), FPS, Coordinates, CPS Counter, Keystrokes, Armor HUD,
Custom Crosshair, Waypoints (`/eclipse wp add|del|list`), Combat HUD, Click Trainer, Placement Trainer, Toggle Sprint (eksperymentalny).
Konfiguracja: `config/eclipse_client.json`.

Nie ma (jeszcze): Fullbright, Tracers, ESP, FullBox ESP, Item/Storage ESP, Nametags, Chunk Borders, Zoom, Auto Tool, Ping, Potion HUD, Surround/Cobweb Trainer.

## Jak dostać gotowy plik JAR
1. Utwórz repozytorium na GitHubie i wgraj do niego całą zawartość tego folderu (razem z `.github`, `gradlew` i `gradle/`).
2. Wejdź w zakładkę **Actions** -> workflow **Build EclipseClient** (uruchamia się sam po każdym pushu; można też kliknąć **Run workflow**).
3. Po zielonym przebiegu otwórz go i na dole w **Artifacts** pobierz `EclipseClient-1.21.11` (ZIP z plikiem `EclipseClient-1.21.11.jar`).
4. Wypakuj JAR do folderu `mods` (potrzebny Fabric Loader >= 0.16 i Fabric API dla 1.21.11).

Lokalnie: `./gradlew build` -> `build/libs/EclipseClient-1.21.11.jar` (Java 21).

## Ważne ograniczenie
Kod odwołuje się do klas Minecrafta po nazwach pośrednich (`class_310`, `method_25294` ...), a kompilacja używa atrap
z `src/stubs/java`. Build więc sprawdza składnię i spójność kodu, ale **nie weryfikuje nazw względem prawdziwego Minecrafta**.
Nazwy oznaczone w kodzie jako „z pamięci” (pozycja gracza, pancerz, czcionka HUD) mogą nie pasować - moduł wtedy sam się wyłącza
i zapisuje błąd w `logs/latest.log` (linie `EclipseClient`). Mod nie był uruchamiany w grze.

## Ręczne testy w grze (do wykonania)
- GUI: otwarcie (prawy Shift / `/eclipse`), zamknięcie (ESC), animacje, wyszukiwarka, przewijanie strzałkami.
- Włączanie/wyłączanie modułów z GUI i keybindem; zmiana ustawień (suwak, kolor) widoczna na HUD.
- HUD: FPS, koordynaty, CPS, Keystrokes, Armor HUD, celownik.
- Waypoints: `/eclipse wp add test`, odległość i strzałka na HUD.
- Click Trainer / Placement Trainer: pomiar po kliknięciach.
- Zapis konfiguracji: zmień ustawienia, zamknij grę, uruchom ponownie.
- Zmiana świata, rozłączenie i ponowne połączenie.
