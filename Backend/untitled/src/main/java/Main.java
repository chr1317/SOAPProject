import Service.ExchangeService;

import java.math.BigDecimal;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        ExchangeService exchangeService = new ExchangeService();

        exchangeService.exchangeCurrency(
                6L,                 // podstaw istniejące ID usera
                "PLN",
                "USD",
                new BigDecimal("100.00"),
                new BigDecimal("0.25000000")
        );

        List<ExchangeTransaction> transactions = exchangeService.getTransactionsForUser(3L);

        System.out.println("Liczba transakcji: " + transactions.size());
        for (ExchangeTransaction t : transactions) {
            System.out.println(
                    t.getId() + " | "
                            + t.getFromCurrency() + " -> " + t.getToCurrency()
                            + " | " + t.getSourceAmount()
                            + " | " + t.getTargetAmount()
                            + " | " + t.getExchangeRate()
            );
        }
    }
}