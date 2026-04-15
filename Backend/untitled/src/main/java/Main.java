import Service.AccountHistoryService;
import Service.BalanceService;
import Service.ExchangeService;
import Service.UserService;
import Entity.AccountTransaction;
import Entity.Balance;

import java.math.BigDecimal;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        try {
            UserService userService = new UserService();
            BalanceService balanceService = new BalanceService();
            ExchangeService exchangeService = new ExchangeService();
            AccountHistoryService historyService = new AccountHistoryService();

            // ============================
            // 1. Tworzenie użytkownika
            // ============================
            Long userId = userService.createUser(
                    "Jan",
                    "Testowy",
                    "test@test.com",
                    "123456"
            );

            System.out.println("Utworzono usera ID: " + userId);

            // ============================
            // 2. Dodanie salda
            // ============================
            balanceService.addBalanceToUser(userId, "PLN", new BigDecimal("1000"));
            balanceService.addBalanceToUser(userId, "USD", new BigDecimal("100"));

            System.out.println("Dodano saldo.");

            // ============================
            // 3. Exchange
            // ============================
            exchangeService.exchangeCurrency(
                    userId,
                    "USD",
                    "PLN",
                    new BigDecimal("50"),
                    new BigDecimal("4.0")
            );

            System.out.println("Zrobiono exchange.");

            // ============================
            // 4. Salda
            // ============================
            List<Balance> balances = balanceService.getBalancesForUser(userId);

            System.out.println("\nSALDA:");
            for (Balance b : balances) {
                System.out.println(b.getCurrencyCode() + " = " + b.getAmount());
            }

            // ============================
            // 5. Historia
            // ============================
            List<AccountTransaction> transactions = historyService.getTransactionsForUser(userId);

            System.out.println("\nTRANSAKCJE:");
            for (AccountTransaction t : transactions) {

                switch (t.getTransactionType()) {

                    case DEPOSIT -> System.out.println(
                            "DEPOSIT | " +
                                    t.getCurrencyCode() + " | " +
                                    t.getAmount()
                    );

                    case WITHDRAW -> System.out.println(
                            "WITHDRAW | " +
                                    t.getCurrencyCode() + " | " +
                                    t.getAmount()
                    );

                    case EXCHANGE -> System.out.println(
                            "EXCHANGE | " +
                                    t.getFromCurrency() + " -> " +
                                    t.getToCurrency() + " | " +
                                    t.getSourceAmount() + " -> " +
                                    t.getTargetAmount() +
                                    " | rate=" + t.getExchangeRate()
                    );
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}