# ☕ CoffeeTime

Application Android de commande de boissons depuis une table de café.
Travaux pratiques **TP1 / TP2** : Activities, Intents, Bundle, LinearLayout, RelativeLayout, ressources multilingues.

**Réalisé par :** _Nom Prénom_ — _Groupe_ — _Année universitaire_

## Fonctionnalités

1. **MainActivity** : saisie du nom et du numéro de table (validation + Toast).
2. **MenuActivity** : 3 boissons (Espresso 2,50 DT, Cappuccino 3,50 DT, Latte 4,00 DT), quantité de 0 à 5, total en direct.
3. **CartActivity** : récapitulatif, remarque, choix du paiement (espèces / carte), numéro de commande aléatoire.
4. **ProgressActivity** : barre de progression (5 s par boisson, 30 s maximum).
5. **TicketActivity** : ticket final et retour à l'accueil.

## Concepts utilisés

- Layouts **imbriqués** : `RelativeLayout` ⊃ `LinearLayout` ⊃ `RelativeLayout`.
- Navigation par **Intent** et transfert de données par **Bundle** (clés centralisées dans `Keys.java`).
- Ressources **multilingues** : `values/` (français) et `values-en/` (anglais).
- `Handler` + cycle de vie (`onResume` / `onPause` / `onDestroy`) pour la barre de progression.

## Flux des données

```
Main ──(nom, table)──▶ Menu ──(+ quantités)──▶ Cart ──(+ total, paiement, note, n°, durée)──▶ Progress ──▶ Ticket
```

## Structure du projet

```
CoffeeTime/
├── app/
│   ├── build.gradle
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/example/coffeetime/
│       │   ├── Keys.java  Carte.java
│       │   ├── MainActivity.java   MenuActivity.java
│       │   └── CartActivity.java   ProgressActivity.java   TicketActivity.java
│       └── res/
│           ├── layout/   (activity_main, menu, cart, progress, ticket)
│           ├── values/strings.xml       (fr)
│           └── values-en/strings.xml    (en)
├── extras/   (énoncé + défi 1 : version à une seule activité)
├── build.gradle  settings.gradle  gradle.properties
└── README.md
```

## Lancer le projet

1. Installer **Android Studio** (Koala ou plus récent).
2. `File ▸ Open` et choisir le dossier `CoffeeTime`.
3. Attendre la synchronisation Gradle, puis cliquer sur ▶ **Run** (émulateur ou téléphone, Android 7.0 / API 24 minimum).

## Configuration

- `compileSdk` / `targetSdk` : 34 — `minSdk` : 24
- Dépendance : `androidx.appcompat:appcompat:1.6.1`
- Langage : Java 8
