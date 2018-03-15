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
│   ├── Entity.java
│   ├── GameMap.java [*]
│   ├── Log.java
│   ├── Metadata.java
│   ├── MetadataParser.java
│   ├── Move.java
│   ├── Navigation.java
│   ├── Networking.java
│   ├── Planet.java
│   ├── Player.java
│   ├── Position.java
│   ├── Ship.java
│   ├── ThrustMove.java
│   ├── UndockMove.java
│   └── Util.java
└── MyBot.java [*]
-->

# Descriere proces

## Task 1

Citind regulile și tutorialele de pe site-ul oficial, am ales să
urmăm sfaturile legate de [primii pași](https://halite.io/learn-programming-challenge/downloads-and-starter-kits/improve-basic-bot)
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