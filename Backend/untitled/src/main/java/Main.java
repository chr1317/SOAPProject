import Service.BalanceService;

public class Main {
    public static void main(String[] args) {
        BalanceService balanceService = new BalanceService();

        System.out.println(balanceService.getUserCurrencies(3L));
    }
}