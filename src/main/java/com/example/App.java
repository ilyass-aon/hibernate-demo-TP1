package com.example;

import com.example.model.Categorie;
import com.example.model.Produit;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.h2.tools.Server;

public class App {

    public static void main(String[] args) {

        try {
            Server.createWebServer("-web", "-webPort", "8082").start();
            System.out.println("Console H2 disponible sur : http://localhost:8082");
        } catch (Exception e) {
            System.out.println("Erreur lors du démarrage de la console H2");
            e.printStackTrace();
        }

        // Création de l'EntityManagerFactory
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hibernate-demo");

        // Insertion des catégories et des produits
        insererCategoriesEtProduits(emf);

        // Lecture initiale
        lireCategories(emf);
        lireProduits(emf);

        // Modification
        modifierProduit(emf, 1L, "Laptop Pro", new BigDecimal("1299.99"));
        modifierCategorie(emf, 1L, "Électronique grand public", "Appareils électroniques du quotidien");
        assignerCategorie(emf, 3L, 2L); // la tablette passe dans "Accessoires"

        // Suppression
        supprimerProduit(emf, 5L);
        supprimerCategorie(emf, 2L); // les produits restants sont détachés, pas supprimés

        // Relecture pour vérifier les changements
        lireCategories(emf);
        lireProduits(emf);

        // Fermeture de l'EntityManagerFactory
        emf.close();
    }


    private static void insererCategoriesEtProduits(EntityManagerFactory emf) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            Categorie electronique = new Categorie("Électronique", "Appareils électroniques");
            Categorie accessoires = new Categorie("Accessoires", "Accessoires informatiques");

            electronique.ajouterProduit(new Produit("Laptop", new BigDecimal("999.99")));
            electronique.ajouterProduit(new Produit("Smartphone", new BigDecimal("499.99")));
            electronique.ajouterProduit(new Produit("Tablette", new BigDecimal("299.99")));
            accessoires.ajouterProduit(new Produit("Souris", new BigDecimal("29.99")));
            accessoires.ajouterProduit(new Produit("Clavier", new BigDecimal("59.99")));

            // PERSIST en cascade : les produits sont persistés avec leur catégorie
            em.persist(electronique);
            em.persist(accessoires);

            em.getTransaction().commit();
            System.out.println("Catégories et produits insérés avec succès !");
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
        } finally {
            em.close();
        }
    }



    private static void lireCategories(EntityManagerFactory emf) {
        EntityManager em = emf.createEntityManager();
        try {
            List<Categorie> categories = em.createQuery(
                            "SELECT DISTINCT c FROM Categorie c LEFT JOIN FETCH c.produits", Categorie.class)
                    .getResultList();

            System.out.println("\nListe des catégories :");
            for (Categorie c : categories) {
                System.out.println(c + " -> " + c.getProduits().size() + " produit(s)");
                for (Produit p : c.getProduits()) {
                    System.out.println("    " + p);
                }
            }
        } finally {
            em.close();
        }
    }

    private static void lireProduits(EntityManagerFactory emf) {
        EntityManager em = emf.createEntityManager();
        try {
            // JOIN FETCH charge la catégorie en même temps (évite LazyInitializationException)
            List<Produit> produits = em.createQuery(
                            "SELECT p FROM Produit p LEFT JOIN FETCH p.categorie", Produit.class)
                    .getResultList();

            System.out.println("\nListe des produits :");
            for (Produit produit : produits) {
                String nomCategorie = produit.getCategorie() != null
                        ? produit.getCategorie().getNom()
                        : "aucune";
                System.out.println(produit + " [catégorie : " + nomCategorie + "]");
            }

            // Recherche d'un produit par ID
            System.out.println("\nRecherche du produit avec ID=2 :");
            Produit produit = em.find(Produit.class, 2L);
            if (produit != null) {
                System.out.println(produit);
            } else {
                System.out.println("Produit non trouvé");
            }
        } finally {
            em.close();
        }
    }

    private static void modifierProduit(EntityManagerFactory emf, Long id, String nouveauNom, BigDecimal nouveauPrix) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            // Le produit est "managed" : toute modification est enregistrée au commit
            Produit produit = em.find(Produit.class, id);
            if (produit != null) {
                produit.setNom(nouveauNom);
                produit.setPrix(nouveauPrix);
                em.getTransaction().commit();
                System.out.println("Produit modifié avec succès : " + produit);
            } else {
                em.getTransaction().rollback();
                System.out.println("Produit avec ID=" + id + " non trouvé, modification impossible");
            }
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    private static void modifierCategorie(EntityManagerFactory emf, Long id, String nouveauNom, String nouvelleDescription) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            Categorie categorie = em.find(Categorie.class, id);
            if (categorie != null) {
                categorie.setNom(nouveauNom);
                categorie.setDescription(nouvelleDescription);
                em.getTransaction().commit();
                System.out.println("Catégorie modifiée avec succès : " + categorie);
            } else {
                em.getTransaction().rollback();
                System.out.println("Catégorie avec ID=" + id + " non trouvée, modification impossible");
            }
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    private static void assignerCategorie(EntityManagerFactory emf, Long produitId, Long categorieId) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            Produit produit = em.find(Produit.class, produitId);
            Categorie categorie = em.find(Categorie.class, categorieId);

            if (produit != null && categorie != null) {
                // Retire le produit de son ancienne catégorie, puis l'ajoute à la nouvelle
                Categorie ancienne = produit.getCategorie();
                if (ancienne != null) {
                    ancienne.retirerProduit(produit);
                }
                categorie.ajouterProduit(produit);
                em.getTransaction().commit();
                System.out.println("Produit ID=" + produitId + " assigné à la catégorie '" + categorie.getNom() + "'");
            } else {
                em.getTransaction().rollback();
                System.out.println("Produit ou catégorie introuvable, assignation impossible");
            }
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    // suppretion

    private static void supprimerProduit(EntityManagerFactory emf, Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            Produit produit = em.find(Produit.class, id);
            if (produit != null) {
                // Retire le produit de sa catégorie pour garder les deux côtés cohérents
                if (produit.getCategorie() != null) {
                    produit.getCategorie().retirerProduit(produit);
                }
                em.remove(produit);
                em.getTransaction().commit();
                System.out.println("Produit avec ID=" + id + " supprimé avec succès");
            } else {
                em.getTransaction().rollback();
                System.out.println("Produit avec ID=" + id + " non trouvé, suppression impossible");
            }
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    private static void supprimerCategorie(EntityManagerFactory emf, Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            Categorie categorie = em.find(Categorie.class, id);
            if (categorie != null) {
                // Détache les produits (categorie_id = NULL) avant la suppression,
                // sinon la contrainte de clé étrangère bloque le DELETE
                for (Produit p : new ArrayList<>(categorie.getProduits())) {
                    categorie.retirerProduit(p);
                }
                em.remove(categorie);
                em.getTransaction().commit();
                System.out.println("Catégorie avec ID=" + id + " supprimée avec succès (produits conservés)");
            } else {
                em.getTransaction().rollback();
                System.out.println("Catégorie avec ID=" + id + " non trouvée, suppression impossible");
            }
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
        } finally {
            em.close();
        }
    }
}