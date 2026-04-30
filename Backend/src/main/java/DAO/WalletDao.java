package DAO;

import Entity.User;
import Entity.Wallet;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

public class WalletDao {

    private final EntityManager em;

    public WalletDao(EntityManager em) {
        this.em = em;
    }

    public void save(Wallet wallet) {
        em.persist(wallet);
    }

    public Wallet findByUser(User user) {
        try {
            return em.createQuery(
                            "SELECT w FROM Wallet w WHERE w.user = :user",
                            Wallet.class
                    )
                    .setParameter("user", user)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}