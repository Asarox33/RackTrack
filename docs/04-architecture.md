# Architecture technique

## 1. Stack

- **Langage** : Kotlin
- **UI** : Jetpack Compose, Material 3
- **Persistance locale** : Room
- **Injection de dépendances** : Hilt (ou manuel si trop lourd pour le niveau du projet — à trancher en tâche `#ARCH-1` du roadmap)
- **Tests** : JUnit5 + Turbine (flows) pour la couche domain en priorité
- **Pas de backend en v1**. Architecture prête à en accueillir un plus tard (voir §5).

## 2. Clean Architecture — 3 couches

```
app/
├── presentation/
│   ├── screens/          # un dossier par écran (HomeScreen, NewMatchScreen, RackScreen, HistoryScreen...)
│   ├── components/       # composants Compose réutilisables (Scoreboard, BallTracker, FoulBanner...)
│   └── viewmodels/        # un ViewModel par écran, consomme les use cases
│
├── domain/
│   ├── model/             # Match, Rack, Player, Shot, FoulType, ShotOutcome... (voir docs/03-domain-model.md)
│   ├── usecases/          # StartMatchUseCase, RecordShotUseCase, etc.
│   └── rules/             # le moteur de règles du 10-ball (pure Kotlin, zéro dépendance Android)
│
└── data/
    ├── local/
    │   ├── db/            # Room: entities, DAO, Database
    │   └── mappers/       # mapping entre entités Room et modèles domain
    └── repository/        # implémentation des interfaces de repository définies dans domain/
```

### Règle de dépendance stricte
`presentation` → `domain` ← `data`
`domain` ne dépend **jamais** de `data` ni de `presentation`, ni d'Android (`android.*`). C'est ce qui garantit que le moteur de règles du 10-ball est testable en pur Kotlin, sans émulateur.

## 3. Pourquoi Clean Architecture ici (et pas juste du MVVM basique)

Le domaine du billard a beaucoup de règles métier non triviales (casse, push-out, ordre des billes, fautes consécutives). Isoler ce moteur de règles dans `domain/rules`, sans dépendance UI ni base de données, permet de :
- le tester unitairement de façon exhaustive (cas de fautes, enchaînements) ;
- le réutiliser tel quel si un mode 9-ball ou 8-ball est ajouté en V2, en factorisant les règles communes (cf. `docs/02-regles-jeu-de-la-10.md`, la plupart des fautes de l'article 1.2.09 sont communes à tous les modes américains).

## 4. Flux de données (exemple : enregistrer un coup)

```
RackScreen (Compose)
   → RackViewModel.onShotRecorded(input)
      → RecordShotUseCase(rackId, shotInput)
         → domain/rules : calcule le ShotOutcome à partir de l'état du Rack
         → RackRepository.save(updatedRack)  // implémenté dans data/, persiste via Room
      ← nouvel état du Rack (StateFlow)
   ← RackScreen recompose avec le nouveau score / état
```

## 5. Évolution future (hors v1, pour info seulement)

```
V1 :  Android App → Room → téléphone (aucun réseau)
V2+ : Android App → Repository → API (Ktor ou Supabase) → Backend → Database
```
Ne pas anticiper cette couche dans le code v1 : les interfaces de repository dans `domain/` suffisent à rendre ce changement possible plus tard sans réécrire le domaine.

## 6. UI : composants dynamiques (SDUI léger — optionnel, pas prioritaire)

Si envisagé plus tard : piloter uniquement l'agencement de composants existants (ex: ordre des blocs du `RackScreen`) via une config JSON simple, jamais la génération de l'app entière depuis un serveur. **Ne pas implémenter en v1** — noté ici pour mémoire uniquement, à ne pas faire générer par Cursor tant que ce n'est pas explicitement demandé.

## 7. Ce que Cursor ne doit PAS faire de son propre chef

- Ajouter un backend, une authentification, ou une dépendance réseau.
- Ajouter un mode de jeu autre que le 10-ball.
- Introduire du XML layouts (Compose uniquement).
- Faire dépendre `domain/` d'Android ou de Room directement.
- Inventer des règles de jeu non présentes dans `docs/02-regles-jeu-de-la-10.md` ou `docs/03-domain-model.md`.
