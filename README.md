# Hibernate Demo — Gestion de Produits et Catégories

## Description

Ce projet est une application Java de démonstration qui illustre l'utilisation de **Hibernate / JPA** pour la persistance des données. Il met en œuvre un modèle simple de gestion de **produits** et de **catégories** avec une relation bidirectionnelle **OneToMany / ManyToOne**.

### Fonctionnalités démontrées

- **Création** de catégories et de produits (avec cascade `PERSIST`)
- **Lecture** — requêtes JPQL avec `JOIN FETCH` pour éviter les problèmes de `LazyInitializationException`
- **Modification** — mise à jour d'entités managées (dirty checking)
- **Suppression** — suppression de produits et de catégories (avec détachement préalable des produits)
- **Réassignation** — déplacement d'un produit d'une catégorie à une autre
- **Console H2** — accès à la base de données en mémoire via une interface web

## Technologies utilisées

| Technologie | Version |
|---|---|
| Java | 1.8 |
| Maven | 3.x |
| Hibernate (JPA) | 5.6.5.Final |
| H2 Database | 2.1.214 |
| SLF4J | 1.7.36 |

## Structure du projet

```
hibernate-demo/
├── pom.xml
└── src/main/
    ├── java/com/example/
    │   ├── App.java                  # Point d'entrée — opérations CRUD
    │   └── model/
    │       ├── Categorie.java        # Entité JPA (côté "One")
    │       └── Produit.java          # Entité JPA (côté "Many")
    └── resources/META-INF/
        └── persistence.xml           # Configuration JPA / Hibernate / H2
```

## Prérequis

- **JDK 8** (ou supérieur) installé et configuré (`JAVA_HOME`)
- **Apache Maven 3.x** installé et accessible dans le `PATH`

## Instructions d'exécution

### 1. Cloner le projet

```bash
git clone <url-du-repo>
cd hibernate-demo
```

### 2. Compiler le projet

```bash
mvn clean compile
```

### 3. Exécuter l'application

```bash
mvn exec:java -Dexec.mainClass="com.example.App"
```

> **Note :** L'application démarre une console web H2 accessible à l'adresse **http://localhost:8082**.  
> Pour s'y connecter, utiliser les paramètres suivants :
>
> | Paramètre | Valeur |
> |---|---|
> | JDBC URL | `jdbc:h2:mem:testdb` |
> | User | `sa` |
> | Password | *(vide)* |

### 4. Lancer les tests (optionnel)

```bash
mvn test
```

## Résultat attendu

L'exécution affiche dans la console les opérations CRUD effectuées :

1. Insertion de 2 catégories (*Électronique*, *Accessoires*) et 5 produits
2. Lecture des catégories et des produits
3. Modification d'un produit et d'une catégorie
4. Réassignation d'un produit vers une autre catégorie
5. Suppression d'un produit et d'une catégorie
6. Relecture pour vérifier les changements
