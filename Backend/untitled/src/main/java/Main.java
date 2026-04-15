import Entity.Balance;
import Service.BalanceService;

import java.math.BigDecimal;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        BalanceService balanceService = new BalanceService();

        balanceService.addBalanceToUser(6L, "PLN", new BigDecimal("1000.00"));
        balanceService.addBalanceToUser(6L, "USD", new BigDecimal("250.00"));

        Balance usd = balanceService.getBalanceForUserAndCurrency(6L, "USD");
        if (usd != null) {
            System.out.println("Saldo USD: " + usd.getAmount());
        }

        List<Balance> balances = balanceService.getBalancesForUser(6L);
        System.out.println("Liczba sald: " + balances.size());
    }
}