package com.example;

import com.example.model.Reservation;
import com.example.service.ReservationService;
import com.example.service.ReservationServiceImpl;
import org.h2.tools.Server;

import javax.persistence.OptimisticLockException;
import java.time.LocalDateTime;
import java.util.Scanner;

public class ConcurrentReservationSimulator {

    public static void main(String[] args) {
        Server server = null;
        try {
            // Démarrage du serveur web H2
            server = Server.createWebServer("-webPort", "8082", "-tcpAllowOthers").start();

            System.out.println("=================================================================");
            System.out.println("🔗 Console Web H2 démarrée !");
            System.out.println("👉 Lien vers la console : http://localhost:8082");
            System.out.println("-----------------------------------------------------------------");
            System.out.println("Paramètres de connexion :");
            System.out.println("  • JDBC URL  : jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
            System.out.println("  • User Name : sa");
            System.out.println("  • Password  : (laisser vide)");
            System.out.println("=================================================================\n");

            ReservationService service = new ReservationServiceImpl();

            // Création initiale d'une réservation
            Reservation reservation = new Reservation("Salle A", LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2));
            reservation = service.creerReservation(reservation);

            // Simulation de deux transactions concurrentes
            // Transaction 1 : récupère la réservation
            Reservation tx1 = service.trouverParId(reservation.getId());

            // Transaction 2 : récupère également la réservation
            Reservation tx2 = service.trouverParId(reservation.getId());

            // Transaction 1 modifie la réservation
            tx1.setSalle("Salle B");
            service.modifierReservation(tx1);
            System.out.println("Transaction 1 : modification réussie.");

            // Transaction 2 tente de modifier la réservation avec l'ancienne version
            try {
                tx2.setSalle("Salle C");
                service.modifierReservation(tx2);
                System.out.println("Transaction 2 : modification réussie.");
            } catch (OptimisticLockException e) {
                System.err.println("Transaction 2 : conflit détecté - " + e.getMessage());
            }

            System.out.println("\n-----------------------------------------------------------------");
            System.out.println("✅ Exécution terminée.");
            System.out.println("🌐 Vous pouvez explorer la base sur : http://localhost:8082");
            System.out.println("⌨️  Appuyez sur ENTRÉE dans cette console pour arrêter le programme...");
            System.out.println("-----------------------------------------------------------------");
            new Scanner(System.in).nextLine();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (server != null) {
                server.stop();
                System.out.println("Serveur Web H2 arrêté.");
            }
        }
    }
}
