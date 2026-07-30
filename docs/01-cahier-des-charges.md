# Cahier des charges — Billard App v0.1

## 1. Nom du projet

Billard App (nom définitif à trouver)

## 2. Objectif

Fournir une application Android simple et fiable pour scorer des parties de billard américain, en respectant les règles officielles FFB, en commençant par le jeu de la 10.

## 3. Utilisateurs cibles

- Joueur amateur ou licencié FFB qui veut suivre son score dans un match informel ou en club
- Éventuellement, à terme : arbitre ou marqueur bénévole en tournoi de club (hors périmètre v1)

## 4. Problème résolu

Compter le score, les fautes et les cas particuliers (casse irrégulière, push-out, bille 10 sortie prématurément, 3 fautes consécutives) à la main est source d'erreurs et de contestations. L'app encode les règles pour fiabiliser le comptage et fluidifier la partie.

## 5. Modes de jeu

### V1 (MVP)
- **Jeu de la 10** (règles FFB, saison 2026-2027)

### V2 (après validation du MVP)
- Jeu de la 9
- Jeu de la 8
- (14/1 continu et Artistic pool : évalués plus tard, complexité de scoring nettement supérieure)

## 6. Fonctions du MVP

- Créer une partie (2 joueurs, nom des joueurs)
- Choisir le format (nombre de manches / racks à gagner)
- Gérer la casse (régulière / irrégulière, choix de l'adversaire en cas de casse irrégulière)
- Gérer le push-out après casse régulière
- Annoncer bille + poche à chaque coup
- Empocher une bille (score +1), gérer l'ordre croissant obligatoire
- Gérer les fautes (liste FFB), avec ball-in-hand pour l'adversaire
- Gérer le cas de la bille 10 (empochée avant l'heure / hors annonce / sortie de table → replacée)
- Compteur de fautes consécutives (3 fautes consécutives = perte de la manche)
- Fin de manche / fin de match (score au meilleur des N manches)
- Historique des matchs joués
- Statistiques simples (manches gagnées/perdues par joueur, moyenne succincte)

## 7. Explicitement hors périmètre v1

- Comptes utilisateurs, authentification
- Cloud, synchronisation multi-appareils
- Mode réseau / multijoueur à distance
- Autres modes de jeu que le 10-ball
- Chronométrage des coups (article 1.2.13 du code sportif — trop complexe pour le MVP)
- Gestion des cartons/pénalités disciplinaires (article 1.2.17)
- Tout ce qui concerne les compétitions fédérales (Titre III du code sportif : catégories, ligues, classements nationaux) — hors sujet pour une app de scoring de partie
- iOS

## 8. Contraintes

- L'utilisateur (développeur du projet) part de zéro en développement natif Android. Le rythme de développement doit rester réaliste ; le MVP doit pouvoir être complété par itérations courtes et testables.
- Les règles doivent être fidèles au code sportif FFB pour le 10-ball (voir `docs/02-regles-jeu-de-la-10.md` et le PDF source dans `resources/`).

## 9. Critères de succès du MVP

- Une partie de 10-ball complète peut être scorée du début à la fin sans quitter les règles FFB de base (hors chronométrage et disciplinaire, explicitement hors scope).
- L'app gère correctement au minimum : casse régulière/irrégulière, push-out, ordre croissant des billes, cas de la bille 10, 3 fautes consécutives, ball-in-hand.
- Utilisation fluide à une main, en conditions réelles de jeu (gros boutons, peu de texte à lire).
