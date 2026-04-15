package Service;

import Config.JpaUtil;
import DAO.BalanceDao;
import DAO.UserDao;
import DAO.WalletDao;
import Entity.Balance;
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

            UserDao userDao = new UserDao(em);
            WalletDao walletDao = new WalletDao(em);
            BalanceDao balanceDao = new BalanceDao(em);

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
            if (amount == null || amount.signum() <= 0) {
                throw new RuntimeException("Kwota musi być większa od zera.");
            }
            if (balance == null) {
                balance = new Balance(wallet, normalizedCurrency, amount);
                balanceDao.save(balance);
            } else {
                balance.setAmount(balance.getAmount().add(amount));
                balanceDao.update(balance);
            }

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