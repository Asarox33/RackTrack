# Conventions de projet

## 1. Nommage Kotlin

- Classes / objets / enums : `PascalCase` (`RackPhase`, `RecordShotUseCase`)
- Fonctions / variables : `camelCase`
- Constantes top-level : `UPPER_SNAKE_CASE`
- Fichiers : un fichier = une classe publique principale, même nom (`RecordShotUseCase.kt`)
- Composables Compose : `PascalCase`, suffixe explicite pas obligatoire mais préférer un nom qui décrit ce qui est affiché (`ScoreboardCard`, pas `ScoreboardComponent1`)
- Use cases : verbe à l'infinitif + `UseCase` (`StartMatchUseCase`, pas `MatchStarter`)

## 2. Organisation des commits Git

Format : `type(scope): description courte à l'impératif`

Types autorisés :
- `feat` — nouvelle fonctionnalité
- `fix` — correction de bug
- `refactor` — changement de code sans changement de comportement
- `test` — ajout/modification de tests
- `docs` — documentation uniquement
- `chore` — config, dépendances, outillage

Exemples :
```
feat(domain): implémenter la règle des 3 fautes consécutives
fix(rules): corriger le respot de la bille 10 après push-out fauté
docs(readme): mettre à jour le périmètre du MVP
```

Scope conseillé = nom du dossier concerné (`domain`, `data`, `presentation`, `rules`, `readme`...).

## 3. Structure d'une Pull Request / d'un lot de travail

Même en solo, garder cette discipline aide à relire son propre travail avec Cursor :
1. Une tâche du roadmap = une branche = un ensemble de commits cohérents.
2. Ne jamais mélanger changement de règle métier et changement d'UI dans le même commit.
3. Tout ajout dans `domain/rules` doit être accompagné d'un test (même minimal) qui vérifie le cas nominal et au moins un cas de faute.

## 4. Tests

- Priorité absolue : `domain/rules` doit avoir la couverture de tests la plus élevée du projet — c'est la partie qui encode les règles FFB et où une erreur est la plus coûteuse.
- Nommage des tests : `given_X_when_Y_then_Z` ou équivalent descriptif en français si plus clair pour le porteur du projet (ex: `casse irrégulière donne bille en main à l'adversaire`).
- Pas de test end-to-end obligatoire en v1 ; les tests unitaires du domaine priment.

## 5. Style Compose

- Un `@Composable` = une responsabilité d'affichage claire. Si un composable dépasse ~80 lignes, envisager de le découper.
- État géré par le `ViewModel` (StateFlow), jamais de logique métier dans un composable.
- Utiliser `Material 3` par défaut ; toute personnalisation de thème centralisée dans un seul fichier `Theme.kt`.

## 6. Documentation dans le code

- Les fonctions de `domain/rules` qui encodent une règle FFB précise doivent citer l'article correspondant en commentaire, par exemple :
```kotlin
// Règle FFB 10-ball, art. 1.5.03 : la casse est irrégulière si aucune bille
// n'est empochée et que moins de 4 billes de but n'ont touché de bande.
fun isBreakLegal(...): Boolean { ... }
```
Cela permet de retrouver rapidement la source en cas de doute ou de correction.

## 7. Ce qu'on demande explicitement à Cursor de respecter

- Toujours lire `docs/03-domain-model.md` avant de toucher à `domain/`.
- Ne jamais introduire de dépendance externe non listée dans `docs/04-architecture.md` sans qu'on le demande explicitement.
- Poser une question plutôt que de deviner une règle de jeu ambiguë — renvoyer vers `docs/02-regles-jeu-de-la-10.md` / le PDF source.
