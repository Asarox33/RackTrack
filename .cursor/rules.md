# Règles de génération de code — Billard App

Tu travailles sur une app Android native (Kotlin + Jetpack Compose) de scoring de billard américain, mode 10-ball uniquement pour le MVP.

## Avant de générer quoi que ce soit

1. Si la tâche touche à `domain/` (règles de jeu, entités, use cases) : lis d'abord `docs/03-domain-model.md` en entier. Ne code pas une règle de jeu de mémoire.
2. Si la tâche touche à la structure du projet ou au choix de dépendances : lis `docs/04-architecture.md`.
3. Respecte systématiquement `docs/05-conventions.md` (nommage, commits, tests).
4. Si une règle de jeu semble ambiguë ou absente de `docs/02-regles-jeu-de-la-10.md` et `docs/03-domain-model.md` : **demande, ne devine pas**. Le PDF `resources/code-sportif-americain-2026-2027.pdf` fait autorité en dernier recours.

## Contraintes strictes (ne jamais franchir sans confirmation explicite)

- Périmètre : uniquement le mode 10-ball. Ne pas ajouter 8-ball/9-ball/14.1/snooker/blackball "pour anticiper", même si ça semble facile à généraliser.
- Pas de backend, pas de réseau, pas d'authentification en v1.
- Compose uniquement, jamais de layouts XML.
- `domain/` ne doit importer ni `android.*`, ni Room, ni Compose. Zéro exception.
- Ne pas introduire de nouvelle dépendance (librairie externe) sans le signaler explicitement dans la réponse, même si elle semble pratique.

## Niveau du développeur

La personne qui pilote ce projet démarre en développement natif Android/Kotlin. En conséquence :
- Explique brièvement (2-3 lignes) le "pourquoi" d'un choix technique non trivial quand tu l'introduis, pas juste le "quoi".
- Préfère la solution la plus simple et la plus standard à la solution la plus élégante/abstraite, sauf si `docs/04-architecture.md` demande explicitement l'inverse.
- Découpe le travail par petites étapes testables plutôt que de générer un gros bloc de code d'un coup, en t'appuyant sur le découpage de `docs/06-roadmap-todo.md`.

## Tests

Toute nouvelle règle ajoutée dans `domain/rules` doit être accompagnée d'au moins :
- 1 test du cas nominal (règle respectée)
- 1 test du cas de faute correspondant
