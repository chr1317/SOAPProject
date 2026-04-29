import Service.ExchangeService;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            ExchangeService exchangeService = new ExchangeService();

            List<String> codes = exchangeService.getAvailableCurrencyCodes();

            System.out.println("Dostępne waluty:");
            System.out.println(codes);

            System.out.println("Liczba walut: " + codes.size());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}