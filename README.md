# Formal Languages and Grammars

This java project is an implementation of some algorithms used in **Formal Languages and Automatons**

## Algorithms

### Clean grammar
The program can clean a grammar by applying the following steps :
- removing non accessible axioms
- removing non productive axioms
- refactoring axioms matching pattern : X -> X | epsilon
- removing epsilon productions

**Example**
```
Parsed Grammar :
  S -> a | AB | B | aB | C | bCdCeCa | I | K
  A -> aCaaC | B | AD
  B -> BA | ADB
  C -> Ac | cA | c | epsilon | X
  D -> Bd
  E -> epsilon | H
  F -> epsilon
  G -> GA | AGD | F
  H -> h
  I -> i | j
  K -> K | epsilon

Cleaned Grammar :
  S -> a | C | bCdCeCa | I | bdea | bdeCa | bdCea | bdCeCa | bCdea | bCdeCa | bCdCea | epsilon
  A -> aCaaC | aaa | aaaC | aCaa
  C -> Ac | cA | c
  I -> i | j
```

### Put in CNF (Chomsky Normal Form)
It is possible to put a grammar in CNF. [Information about CNF here](https://en.wikipedia.org/wiki/Chomsky_normal_form)

**Prerequisites** : The grammar must be epsilon free

**Example** (Using the above cleaned grammar)
```
S -> a | YF | MF | NF | PF | QF | UF | VF | XF | AG | GA | c | i | j | epsilon
A -> KC | HF | IC | JH
C -> AG | GA | c
B -> b
D -> d
E -> e
F -> a
G -> c
H -> FF
I -> HF
J -> FC
K -> JH
L -> BD
M -> LE
N -> MC
O -> LC
P -> OE
Q -> PC
R -> BC
T -> RD
U -> TE
V -> UC
W -> TC
X -> WE
Y -> XC
```

### Put in GNF (Greibach Normal Form)
TODO

### Applying the CYK (Cocke–Younger–Kasami) algorithm
You can determine whether a word can be generated with a grammar. [Information about CYK here](https://en.wikipedia.org/wiki/CYK_algorithm)

**Prerequisites** : The grammar must be in CNF (and obviously epsilon free)

**Example**
```
Parsed Grammar :
S -> AB
A -> BB | a
B -> AB | b

CYK.isMember(aab) : true
CYK.isMember(aba) : false
```

## Requirements
This program is only compatible with **JAVA 8 and upper**

## Author
Florian Pradines <florian.pradines@gmail.com>

## TODO
- Greibach Normal Form
- Get the tree with the CYK algorithm