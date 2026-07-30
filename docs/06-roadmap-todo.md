# Roadmap & TODO — MVP v1 (10-ball)

Statut global : 🚧 Pas encore commencé (cadrage terminé, code à démarrer)

Légende : ⬜ à faire · 🟨 en cours · ✅ fait

## Étape 0 — Setup projet
- ⬜ `#SETUP-1` Créer le projet Android (Kotlin + Compose), package name, min SDK
- ⬜ `#SETUP-2` Configurer Git, `.gitignore` Android standard
- ⬜ `#SETUP-3` Ajouter les dépendances de base (Compose, Room, tests) — voir `docs/04-architecture.md`
- ⬜ `#ARCH-1` Décider Hilt vs DI manuelle (trancher selon simplicité pour un débutant Kotlin)

## Étape 1 — Domaine (priorité absolue, avant toute UI)
- ⬜ `#DOM-1` Modéliser `Player`, `Match`, `Rack`, `Shot`, `FoulType`, `ShotOutcome` (cf. `docs/03-domain-model.md`)
- ⬜ `#DOM-2` Implémenter la logique de casse (régulière / irrégulière)
- ⬜ `#DOM-3` Implémenter le push-out
- ⬜ `#DOM-4` Implémenter le déroulement normal d'un coup (annonce, ordre croissant, empoche)
- ⬜ `#DOM-5` Implémenter le cas particulier de la bille 10 (respot)
- ⬜ `#DOM-6` Implémenter le compteur de fautes consécutives + perte de manche
- ⬜ `#DOM-7` Implémenter la détection de fin de manche / fin de match
- ⬜ `#DOM-TEST` Couvrir chaque règle ci-dessus d'au moins 1 test nominal + 1 test de faute

## Étape 2 — Persistance
- ⬜ `#DATA-1` Modéliser les entités Room (Match, Rack, Shot historisé)
- ⬜ `#DATA-2` Implémenter les repositories (interfaces définies dans `domain/`, implémentation dans `data/`)
- ⬜ `#DATA-3` Mapper entités Room ↔ modèles domain

## Étape 3 — UI (après que le domaine est testé et fiable)
- ⬜ `#UI-1` Écran Accueil (Nouvelle partie / Historique / Statistiques)
- ⬜ `#UI-2` Écran Configuration de partie (joueurs, nombre de manches)
- ⬜ `#UI-3` Écran Casse (bouton "casse régulière/irrégulière", choix adversaire si irrégulière)
- ⬜ `#UI-4` Écran Match — scoreboard principal (score, bille en cours la plus basse, annonce)
- ⬜ `#UI-5` Écran Match — saisie d'un coup (bille+poche annoncées, résultat)
- ⬜ `#UI-6` Bandeau d'alerte "2 fautes consécutives" / fin de manche
- ⬜ `#UI-7` Écran Résultat de manche / de match
- ⬜ `#UI-8` Écran Historique des matchs
- ⬜ `#UI-9` Écran Statistiques simples

## Étape 4 — Finitions MVP
- ⬜ `#POLISH-1` Gros boutons / ergonomie une main (usage debout à côté de la table)
- ⬜ `#POLISH-2` Undo du dernier coup (erreur de saisie fréquente en usage réel)
- ⬜ `#POLISH-3` Icône et nom d'app définitifs

## V2 — Après validation du MVP par l'usage réel
- ⬜ Ajouter le mode 9-ball (règles proches, réutiliser le moteur commun)
- ⬜ Ajouter le mode 8-ball
- ⬜ Évaluer besoin réel de cloud/sync avant de l'implémenter
- ⬜ Évaluer le 14/1 continu (scoring nettement plus complexe — reformation du rack, replacement de billes)

## Notes de suivi

_Ajouter ici au fil de l'eau les décisions prises, les blocages rencontrés, les questions posées à trancher plus tard._

- (exemple) 2026-07-30 : choix du 10-ball comme mode MVP, cadrage produit et architecture posés avant tout code.
