package Service;

import Config.JpaUtil;
import DAO.AccountTransactionDao;
import DAO.BalanceDao;
import DAO.UserDao;
import DAO.WalletDao;
import Entity.AccountTransaction;
import Entity.Balance;
import Entity.TransactionStatus;
import Entity.TransactionType;
import Entity.User;
import Entity.Wallet;
import jakarta.persistence.EntityManager;

import java.math.BigDecimal;
import java.util.List;

public class BalanceService {

    public void addBalanceToUser(Long userId, String currencyCode, BigDecimal amount) {
        EntityManager em = JpaUtil.getEntityManager();

        try {
            em.getTransaction().begin();

            if (amount == null || amount.signum() <= 0) {
                throw new RuntimeException("Kwota musi być większa od zera.");
            }

            UserDao userDao = new UserDao(em);
            WalletDao walletDao = new WalletDao(em);
            BalanceDao balanceDao = new BalanceDao(em);
            AccountTransactionDao accountTransactionDao = new AccountTransactionDao(em);

            User user = userDao.findById(userId);
            if (user == null) {
                throw new RuntimeException("Użytkownik o id " + userId + " nie istnieje.");
            }

            Wallet wallet = walletDao.findByUser(user);
            if (wallet == null) {
                throw new RuntimeException("Portfel użytkownika nie istnieje.");
            }

            String normalizedCurrency = currencyCode.toUpperCase();

            Balance balance = balanceDao.findByWalletAndCurrency(wallet, normalizedCurrency);

            if (balance == null) {
                balance = new Balance(wallet, normalizedCurrency, amount);
                balanceDao.save(balance);
            } else {
                balance.setAmount(balance.getAmount().add(amount));
                balanceDao.update(balance);
            }

            AccountTransaction transaction = new AccountTransaction();
            transaction.setWallet(wallet);
            transaction.setTransactionType(TransactionType.DEPOSIT);
            transaction.setStatus(TransactionStatus.COMPLETED);
            transaction.setCurrencyCode(normalizedCurrency);
            transaction.setAmount(amount);
            transaction.setDescription("Deposit to wallet");

            accountTransactionDao.save(transaction);

            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Błąd podczas dodawania salda.", e);
        } finally {
            em.close();
        }
    }

    public List<Balance> getBalancesForUser(Long userId) {
        EntityManager em = JpaUtil.getEntityManager();

        try {
            UserDao userDao = new UserDao(em);
            WalletDao walletDao = new WalletDao(em);
            BalanceDao balanceDao = new BalanceDao(em);

            User user = userDao.findById(userId);
            if (user == null) {
                throw new RuntimeException("Użytkownik nie istnieje.");
            }

            Wallet wallet = walletDao.findByUser(user);
            if (wallet == null) {
                throw new RuntimeException("Portfel użytkownika nie istnieje.");
            }

            return balanceDao.findByWallet(wallet);
        } finally {
            em.close();
        }
    }

    public Balance getBalanceForUserAndCurrency(Long userId, String currencyCode) {
        EntityManager em = JpaUtil.getEntityManager();

        try {
            UserDao userDao = new UserDao(em);
            WalletDao walletDao = new WalletDao(em);
            BalanceDao balanceDao = new BalanceDao(em);

            User user = userDao.findById(userId);
            if (user == null) {
                throw new RuntimeException("Użytkownik nie istnieje.");
            }

            Wallet wallet = walletDao.findByUser(user);
            if (wallet == null) {
                throw new RuntimeException("Portfel użytkownika nie istnieje.");
            }

            return balanceDao.findByWalletAndCurrency(wallet, currencyCode.toUpperCase());
        } finally {
            em.close();
        }
    }
}