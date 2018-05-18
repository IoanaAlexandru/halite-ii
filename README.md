	 _           _                          
	│ │__  _   _│ │_ ___   _ __ ___   ___ _ 
	│ '_ \│ │ │ │ __/ _ \ │ '_ ` _ \ / _ (_)
	│ │_) │ │_│ │ ││  __/ │ │ │ │ │ │  __/_ 
	│_.__/ \__, │\__\___│ │_│ │_│ │_│\___( )
	       │___/                         │/

# Membrii echipei

  * ALEXANDRU Ioana (324CB)
  * GRIGORE Edwin-Mark (324CB)
  * MITOCARU Irina (324CB)
  * MITRAN Andreea (324CB)

<!--
# Structură proiect

Structura este bazată pe bot-ul din starter pack. Fișierele cu [*] au
fost modificate de noi.
.
├── hlt
│   ├── Collision.java
│   ├── Constants.java
│   ├── DockMove.java
│   ├── Entity.java [*]
│   ├── GameMap.java [*]
│   ├── Log.java
│   ├── Metadata.java
│   ├── MetadataParser.java
│   ├── Move.java
│   ├── Navigation.java [*]
│   ├── Networking.java [*]
│   ├── Planet.java
│   ├── Player.java
│   ├── Position.java
│   ├── Ship.java [*]
│   ├── ThrustMove.java
│   ├── UndockMove.java
│   └── Util.java
└── MyBot.java [*]
-->

# Descriere proces

## Task 1

Citind regulile și tutorialele de pe site-ul oficial, am ales să
urmăm sfaturile legate de [primii pași](https://halite.io/learn-
programming-challenge/downloads-and-starter-kits/improve-basic-bot)
care ar trebui urmați în vederea îmbunătățirii bot-ului de bază oferit
de starter pack.

Prima modificare făcută a fost sortarea planetelor în funcție de
distanța față de rachetă, în locul iterării prin lista default de
planete de pe hartă. Am modificat puțin și funcția din hlt/ care
ordonează entitățile după distanță, pentru a trata cazul în care
există două entități la aceeași distanță față de punctul de referință
(în locul unui 
`Map<Double, Entity>` avem un 
`Map<Double, LinkedList<Entity>>`).

A doua modificare a fost în sensul atribuirii unei direcții diferite
fiecărui bot, pentru a nu merge toți către aceeași planetă. Lucrul
acesta scade timpul necesar pentru ocuparea tuturor planetelor și,
de asemenea, rezolvă (temporar, pe cazul Single Player) problema
ciocnirilor cu propriile nave.

## Task 2

Având în vedere strategia implementată de bot-ul din checker,
ne-am gândit că o strategie bună ar fi să ne axăm pe ofensivă.
Prin urmare, fiecare rachetă se îndreaptă spre cea mai apropiată
navă dintre cele ale inamicului care nu a fost țintită deja de o
altă rachetă.

## Task 3

Pentru a îmbunătăți strategia aleasă la task-ul trecut, și anume de a
ataca cele mai apropiate nave inamice, ne-am gândit că ar trebui să
intrăm și în defensivă, cu un fel de strategie "apocaliptică". Atunci
când bot-ul realizează faptul că nu poate câștiga, fuge de inamici,
ca să evite să fie lovit (deoarece într-un joc cu 4P, în orice
situație, un player care are la final o navă va avea un scor mai mare
decât unul care nu mai are nici o navă).

Astfel, am implementat în clasa MyBot următoarele strategii ce se
exclud reciproc:
-> Survival Mode
    - pe baza strategiei [de aici](https://recursive.cc/blog/halite-
    ii-post-mortem.html), verificăm dacă bot-ul trebuie să intre
    în Survival Mode (atunci când nu mai are șanse de câștig - i.e.,
    în jurul fiecărei planete colonizate există mai multe nave inamice
    decât aliate care nu sunt docate), caz în care acesta își va
    trimite navele în cel mai apropiat colț al hărții;
    - am creat funcții ajutătoare care să numere navele inamice/aliate
    dintr-o anumită rază față de un punct de referință, și care să
    găsească colțurile hărții (dintre care se va alege cel mai
    apropiat)
-> Normal Mode
    - dacă bot-ul incă poate câștiga, se implementează strategia pur
    ofensivă de la Task-ul 2.