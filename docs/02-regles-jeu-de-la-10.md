# Règles du jeu de la 10 (FFB, saison 2026-2027) — reformulées pour modélisation

> ⚠️ Ce document est une **reformulation à usage de spécification technique**, pas le texte réglementaire. En cas de litige ou d'ambiguïté, la seule source qui fait autorité est `resources/code-sportif-americain-2026-2027.pdf` (Chapitre 5, articles 1.5.01 à 1.5.07, complété par les règles générales du Chapitre 2, articles 1.2.01 à 1.2.20).

## 1. Objectif de la partie

- 10 billes numérotées (1 à 10) + bille blanche.
- Les billes doivent être touchées/jouées dans l'ordre croissant de leur numéro (on peut viser une bille plus haute par carambolage tant que la bille de plus petit numéro est touchée en premier).
- Chaque coup doit être **annoncé** : bille visée + poche visée.
- La partie est gagnée en empochant régulièrement la bille n°10, **en dernier**, dans la poche annoncée.
- Un match se joue en plusieurs manches (nombre défini avant le match).

## 2. Mise en place

- Triangle dédié 10-ball, bille n°1 en tête sur le point de replacement, bille n°10 au centre, autres billes placées librement autour (position différente à chaque rack).

## 3. Casse de départ

- Le joueur a bille en main derrière la ligne de départ.
- Doit obligatoirement toucher la bille n°1 en premier, sinon → **faute**.
- Si aucune bille n'est empochée à la casse : au moins 4 billes de but doivent toucher une bande, sinon → **faute** ("casse irrégulière").
- En cas de faute à la casse : l'adversaire a bille en main sur toute la table.
- *(Catégorie Masters uniquement : règle de la "break box" — casse obligatoire depuis une zone définie. Hors scope MVP, à ignorer sauf si demandé plus tard.)*

## 4. Le push-out (coup de dégagement)

- Disponible uniquement après une **casse régulière**, pour le joueur qui a la main.
- Doit être annoncé explicitement avant d'être joué.
- Pendant ce coup, les règles normales de "doit toucher la bille la plus basse" et "doit envoyer une bille en bande après contact" ne s'appliquent pas.
- Si la bille 10 est empochée pendant le push-out → elle est replacée (respot), les autres billes empochées restent empochées.
- Si le push-out est joué sans faute : l'**adversaire** choisit de jouer la table telle quelle, ou de laisser rejouer l'auteur du push-out.
- Si le push-out est fauté : l'adversaire a bille en main sur toute la table.

## 5. Déroulement normal d'une manche

- Le joueur qui a la main doit annoncer bille + poche à chaque coup.
- Il doit toucher en premier la bille de plus petit numéro encore sur la table (peut empocher une autre bille par carambolage/combinaison si l'annonce correspond).
- Tant qu'il empoche régulièrement la bille annoncée, il continue de jouer.
- S'il empoche une bille non annoncée, ou dans la mauvaise poche : ce n'est pas une faute, mais la main passe à l'adversaire (billes non replacées, sauf la 10 — voir §6).
- **Défense/Safe** : le joueur peut annoncer "Défense" au lieu d'annoncer une empoche ; la main passe à l'adversaire à la fin du coup. S'il empoche une bille en défense, l'adversaire choisit de jouer la table telle quelle ou de le laisser rejouer.

## 6. Cas particulier de la bille n°10

La bille 10 ne doit être empochée qu'en tout dernier, régulièrement, dans la poche annoncée. Dans tous les autres cas où elle est empochée ou sort de la table avant l'heure :
- Elle est **replacée sur le point de replacement** (ou juste derrière si le point est occupé).
- Les autres billes empochées lors du même coup restent empochées.
- La main passe à l'adversaire (sauf si elle a été empochée régulièrement de façon anticipée dans le cadre d'un coup par ailleurs valide et annoncée comme telle — cf. règle générale ci-dessus).

## 7. Fautes (liste applicable au 10-ball)

Une faute entraîne : **bille en main pour l'adversaire, n'importe où sur la table** (sauf cas spécifiques de la casse déjà traités ci-dessus).

Fautes principales à modéliser (cf. article 1.2.09 du code sportif, liste complète dans le PDF) :
- Bille blanche empochée ou éjectée de la table
- Mauvaise bille touchée en premier (non-respect de l'ordre croissant)
- Aucune bande touchée après contact, si aucune bille n'est empochée
- Pied du joueur non au sol pendant le tir
- Bille de but éjectée de la table
- Bille touchée/déplacée accidentellement hors d'un tir normal
- Jouer alors que des billes sont encore en mouvement
- Mauvais placement de la bille en main (devant la ligne de départ interdit lors d'un ball-in-hand "derrière la ligne")
- Jouer hors de son tour
- Jouer avec une bille de but au lieu de la bille blanche

*(Simplification MVP : le comportement antisportif, le jeu lent/chronométré et les cas de litige nécessitant un arbitre humain ne sont pas modélisés — l'app suppose un usage en confiance entre joueurs ou avec auto-arbitrage.)*

## 8. Règle des trois fautes consécutives

- Si un joueur commet 3 fautes consécutives (non entrecoupées d'un coup régulier), il **perd la manche immédiatement**.
- Après la 2e faute consécutive, l'app doit avertir le joueur (à l'écran) qu'il est à un coup de perdre la manche.
- Un coup régulier remet le compteur de fautes consécutives à zéro.

## 9. Fin de manche / fin de match

- Une manche se termine dès que la bille 10 est empochée régulièrement en dernier, ou dès qu'un joueur perd suite à 3 fautes consécutives.
- Le match se termine quand un joueur atteint le nombre de manches gagnantes défini au départ.

## 10. Points volontairement non modélisés en v1

- Chronométrage des coups (article 1.2.13)
- Gestion des cartons et sanctions disciplinaires (article 1.2.17)
- Règles spécifiques à la catégorie Masters (break box)
- Contexte fédéral (ligues, classements, compétitions officielles — Titre III du code sportif)
