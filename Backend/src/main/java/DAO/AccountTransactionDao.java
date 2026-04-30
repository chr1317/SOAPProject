package DAO;

import Entity.AccountTransaction;
import Entity.Wallet;
import jakarta.persistence.EntityManager;

import java.util.List;

public class AccountTransactionDao {

    private final EntityManager em;

    public AccountTransactionDao(EntityManager em) {
        this.em = em;
    }

    public void save(AccountTransaction transaction) {
        em.persist(transaction);
    }

    public List<AccountTransaction> findByWallet(Wallet wallet) {
        return em.createQuery(
                        "SELECT t FROM AccountTransaction t WHERE t.wallet = :wallet ORDER BY t.createdAt DESC",
                        AccountTransaction.class
                )
                .setParameter("wallet", wallet)
                .getResultList();
    }
}