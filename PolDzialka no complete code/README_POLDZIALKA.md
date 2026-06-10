# Plugin poldzialka - Dokumentacja

## Opis
Plugin **poldzialka** to zaawansowany system zarządzania działkami dla serwera Minecraft 1.21 (Spigot/Paper API). Integruje się z WorldGuard oraz WorldEdit w celu tworzenia i zarządzania działkami z pełnym systemem flag bezpieczeństwa.

## Wymagania
- Minecraft 1.21
- Paper/Spigot API 1.21
- WorldGuard 7.0.9+
- WorldEdit 7.3.0+
- Java 21+

## Instalacja

1. Umieść `poldzialka.jar` w folderze `/plugins/`
2. Restart serwera
3. Plugin automatycznie utworzy folder `/plugins/poldzialka/` z plikami konfiguracyjnymi

## Struktura Plików

```
plugins/poldzialka/
├── config.yml           # Główna konfiguracja
├── messages_pl.yml      # Wiadomości polskie
├── messages_en.yml      # Wiadomości angielskie
└── dzialki.yml          # Baza danych działek
```

## Konfiguracja (config.yml)

### Podstawowe Ustawienia
```yaml
language: pl                    # Język: pl lub en
plot-block-material: DIAMOND_BLOCK    # Material bloku działki
plot-item-material: DIAMOND_BLOCK     # Material przedmiotu
```

### Rozmiary Działek
```yaml
allowed-sizes:
  - 11    # 11x11
  - 15    # 15x15
  - 21    # 21x21
  - 31    # 31x31
```

### Flagi WorldGuard
```yaml
default-flags:
  - 'pvp: DENY'
  - 'entry: ALLOW'
  - 'mob-spawning: DENY'
  - 'chest-access: ALLOW'
  - 'ride: ALLOW'
```

## Komendy

### /dzialka
Otwiera menu główne działki (gracz musi stać na swojej działce)

### /dzialka daj <rozmiar>
Wydaje blok działki o podanym rozmiarze (tylko dla admina)
- Wymagana permisja: `poldzialka.daj`
- Dozwolone rozmiary: 11, 15, 21, 31

**Przykład:**
```
/dzialka daj 21
```

## Interakcja z Blokiem

### Prawy Klik (PPM)
Kliknięcie PPM na blok środkowy działki otwiera główne menu zarządzania.

### Lewi Klik (LPM)
Zniszczenie bloku środkowego (tylko przez właściciela) usuwa działkę i zwraca blok do ekwipunku.

## System Menu (GUI)

### Główne Menu (54 sloty)
Slot 11 - **Zarządzaj Członkami**
- Dodaj nowych członków
- Usuń istniejących członków

Slot 13 - **Ustawienia Flag**
- Zarządzaj flagami WorldGuard
- Przełączaj: PVP, Entry, Mob Spawning, Chest Access, Ride

Slot 15 - **Informacje o Działce**
- Wyświetla detale działki
- Współrzędne, rozmiar, właściciel
- Lista członków

Slot 31 - **Usuń Działkę**
- Bezpieczne usunięcie działki
- Potwierdzenie przed usunięciem

## Permisje

```
poldzialka.use           # Używanie komendy /dzialka (default: true)
poldzialka.daj           # Wydawanie bloków działek (default: op)
```

## Architektura Pluginu

### Główne Klasy

**pl.poldzialka.Poldzialka** - Główna klasa pluginu

**pl.poldzialka.managers:**
- `PlotManager` - Zarządzanie działkami
- `WorldGuardManager` - Integracja z WorldGuard

**pl.poldzialka.config:**
- `ConfigManager` - Zarządzanie konfiguracją
- `MessageManager` - Zarządzanie wiadomościami

**pl.poldzialka.gui:**
- `PlotMenu` - System menu GUI
- `PlotInventoryHolder` - Holder dla inventorów
- `MenuType` - Typy menu

**pl.poldzialka.listeners:**
- `PlayerInteractListener` - Interakcja gracza z blokami
- `PlotPlaceListener` - Umieszczanie bloków działek
- `PlotBreakListener` - Niszczenie bloków działek
- `MenuClickListener` - Kliknięcia w GUI
- `PlayerChatListener` - Wiadomości gracza (dla input)

**pl.poldzialka.commands:**
- `DzialkaCommand` - Obsługa komendy /dzialka

**pl.poldzialka.model:**
- `PlotData` - Model danych działki

**pl.poldzialka.storage:**
- `PlotStorage` - Przechowywanie danych w YAML

## Baza Danych (dzialki.yml)

Format zapisu działki:
```yaml
plots:
  dzialka_nickname_id:
    owner: "uuid-gracza"
    owner-name: "nick"
    world: "world"
    center-x: 100
    center-y: 64
    center-z: 100
    size: 21
    members:
      - "member1"
      - "member2"
```

## Flagi WorldGuard

Działka automatycznie tworzy region WorldGuard z następującymi flagami:

| Flaga | Opis | Domyślnie |
|-------|------|----------|
| pvp | Walka na działce | DENY |
| entry | Wchodzenie na działkę | ALLOW |
| mob-spawning | Spawnowanie mobów | DENY |
| chest-access | Dostęp do skrzyń | ALLOW |
| ride | Pojazdy/Wózki | ALLOW |

## Walidacja Lokalizacji

### Anty-Nachodzenie
- Plugin automatycznie sprawdza czy nowa działka nie koliduje z istniejącymi
- Możliwość ustawienia minimalnego odstępu między działkami

### Wymagania
- Blok musi być postawiony na solidnym bloku (nie powietrze)
- Działka zajmuje pełny segment od dna do góry świata
- Rozmiar musi być liczbą nieparzystą

## Zarządzanie Członkami

1. Otwórz menu główne działki (PPM na bloku lub /dzialka)
2. Kliknij "Zarządzaj Członkami"
3. Wybierz "Dodaj Członka" i wpisz nick
4. Gracz otrzyma uprawnienia do działki w WorldGuard

## Usuwanie Działki

1. Otwórz menu główne działki
2. Kliknij "Usuń Działkę"
3. Potwierdź usunięcie (Zielona wełna)
4. Blok działki zostanie zwrócony do ekwipunku

## Wiadomości

Wszystkie wiadomości mogą być edytowane w plikach:
- `messages_pl.yml` - Polskie wiadomości
- `messages_en.yml` - Angielskie wiadomości

Obsługiwane kolory (`&` prefix):
- `&a` - Zielony
- `&c` - Czerwony
- `&e` - Żółty
- `&b` - Niebieski
- itd.

## Rozwiązywanie Problemów

### Działka nie tworzy się
- Sprawdź czy WorldGuard jest zainstalowany
- Sprawdź czy blok jest postawiony na solidnym bloku
- Sprawdź czy gracz ma wystarczająco miejsca do wymiarów działki

### Menu się nie otwiera
- Upewnij się że gracz stoi na swojej działce
- Sprawdź czy został załadowany plik konfiguracyjny
- Sprawdź logi serwera pod kątem błędów

### Flagi nie działają
- Sprawdź czy WorldGuard jest prawidłowo zainstalowany
- Zweryfikuj uprawnienia gracza w WorldGuard
- Zresetuj region poleceniem /dzialka

## Licencja
Plugin dostarcza się "as is" bez gwarancji.

## Autor
mcmodstudio

## Wersja
1.22
