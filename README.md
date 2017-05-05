Traces-scalajs-sankey
=============================

Visualisation des parcours (traces) des étudiant·e·s par étapes de
diplômes. En Scala compilé vers javascript (utilise Scala-js).

Exemple d'utilisation : https://mindsized.org/viz/traces/

## Compilation vers html/js

Prérequis : installer `sbt`.

Lancer `sbt`, lancer la compilation continue `~fastOptJS`
(recompilation automatique à chaque modification d'un fichier source).

Le résultat est visible dans : `target/scala-2.11/index-dev.html` (à
ouvrir dans un navigateur en local ou servi sur le réseau), il faut
recharger la page après chaque compilation (on peut aussi améliorer ça
grâce au workbench de Li Haoyi).

Il est nécessaire qu'une copie du répertoire `deps` soit accessible
depuis `index-dev.html`. Copier `deps` à l'endroit où vous comptez
installer les fichiers et remplacer `/tracesdeps` par le répertoire d'installation.

Lorsque le résultat est satisfaisant on peut obtenir une meilleure
optimisation du code javascript en compilant avec `fullOptJS`, le
résultat sera alors écrit dans  `target/scala-2.11/index.html` qui
pointera vers le code js optimisé.


