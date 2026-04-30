package DAO;

import Entity.Balance;
import Entity.Wallet;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

import java.util.List;

public class BalanceDao {

    private final EntityManager em;

    public BalanceDao(EntityManager em) {
        this.em = em;
    }

    public void save(Balance balance) {
        em.persist(balance);
    }

    public Balance update(Balance balance) {
        return em.merge(balance);
    }

    public Balance findByWalletAndCurrency(Wallet wallet, String currencyCode) {
        try {
            return em.createQuery(
                            "SELECT b FROM Balance b WHERE b.wallet = :wallet AND b.currencyCode = :currencyCode",
                            Balance.class
                    )
                    .setParameter("wallet", wallet)
                    .setParameter("currencyCode", currencyCode)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<Balance> findByWallet(Wallet wallet) {
        return em.createQuery(
                        "SELECT b FROM Balance b WHERE b.wallet = :wallet",
                        Balance.class
                )
                .setParameter("wallet", wallet)
                .getResultList();
    }

}