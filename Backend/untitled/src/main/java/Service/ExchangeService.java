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
import java.math.RoundingMode;

public class ExchangeService {

    public void exchangeCurrency(Long userId,
                                 String fromCurrency,
                                 String toCurrency,
                                 BigDecimal sourceAmount,
                                 BigDecimal exchangeRate) {

        EntityManager em = JpaUtil.getEntityManager();

        try {
            em.getTransaction().begin();

            if (userId == null) {
                throw new RuntimeException("User ID nie może być nullem.");
            }

            if (fromCurrency == null || toCurrency == null) {
                throw new RuntimeException("Waluty nie mogą być puste.");
            }

            if (sourceAmount == null || sourceAmount.signum() <= 0) {
                throw new RuntimeException("Kwota źródłowa musi być większa od zera.");
            }

            if (exchangeRate == null || exchangeRate.signum() <= 0) {
                throw new RuntimeException("Kurs wymiany musi być większy od zera.");
            }

            String normalizedFrom = fromCurrency.toUpperCase();
            String normalizedTo = toCurrency.toUpperCase();

            if (normalizedFrom.equals(normalizedTo)) {
                throw new RuntimeException("Waluta źródłowa i docelowa nie mogą być takie same.");
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

            Balance sourceBalance = balanceDao.findByWalletAndCurrency(wallet, normalizedFrom);
            if (sourceBalance == null) {
                throw new RuntimeException("Brak salda dla waluty " + normalizedFrom + ".");
            }

            if (sourceBalance.getAmount().compareTo(sourceAmount) < 0) {
                throw new RuntimeException("Niewystarczające środki w walucie " + normalizedFrom + ".");
            }

            BigDecimal targetAmount = sourceAmount.multiply(exchangeRate)
                    .setScale(4, RoundingMode.HALF_UP);

            sourceBalance.setAmount(sourceBalance.getAmount().subtract(sourceAmount));
            balanceDao.update(sourceBalance);

            Balance targetBalance = balanceDao.findByWalletAndCurrency(wallet, normalizedTo);
            if (targetBalance == null) {
                targetBalance = new Balance(wallet, normalizedTo, targetAmount);
                balanceDao.save(targetBalance);
            } else {
                targetBalance.setAmount(targetBalance.getAmount().add(targetAmount));
                balanceDao.update(targetBalance);
            }

            AccountTransaction accountTransaction = new AccountTransaction();
            accountTransaction.setWallet(wallet);
            accountTransaction.setTransactionType(TransactionType.EXCHANGE);
            accountTransaction.setStatus(TransactionStatus.COMPLETED);
            accountTransaction.setFromCurrency(normalizedFrom);
            accountTransaction.setToCurrency(normalizedTo);
            accountTransaction.setSourceAmount(sourceAmount);
            accountTransaction.setTargetAmount(targetAmount);
            accountTransaction.setExchangeRate(exchangeRate);
            accountTransaction.setDescription("Currency exchange");

            accountTransactionDao.save(accountTransaction);

            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Błąd podczas wymiany walut.", e);
        } finally {
            em.close();
        }
    }
}