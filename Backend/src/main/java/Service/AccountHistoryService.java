package Service;

import Config.JpaUtil;
import DAO.AccountTransactionDao;
import DAO.UserDao;
import DAO.WalletDao;
import Entity.AccountTransaction;
import Entity.User;
import Entity.Wallet;
import jakarta.persistence.EntityManager;

import java.util.List;

public class AccountHistoryService {

    public List<AccountTransaction> getTransactionsForUser(Long userId) {
        EntityManager em = JpaUtil.getEntityManager();

        try {
            UserDao userDao = new UserDao(em);
            WalletDao walletDao = new WalletDao(em);
            AccountTransactionDao accountTransactionDao = new AccountTransactionDao(em);

            User user = userDao.findById(userId);
            if (user == null) {
                throw new RuntimeException("Użytkownik o id " + userId + " nie istnieje.");
            }

            Wallet wallet = walletDao.findByUser(user);
            if (wallet == null) {
                throw new RuntimeException("Portfel użytkownika nie istnieje.");
            }

            return accountTransactionDao.findByWallet(wallet);
        } finally {
            em.close();
        }
    }
}