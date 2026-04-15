package SOAP;

import Entity.Balance;
import Service.BalanceService;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@WebService
public class AccountSoapService {

    private final BalanceService balanceService = new BalanceService();

    @WebMethod
    public String ping() {
        return "Account SOAP działa";
    }

    @WebMethod
    public String addBalanceToUser(
            @WebParam(name = "userId") Long userId,
            @WebParam(name = "currencyCode") String currencyCode,
            @WebParam(name = "amount") String amount
    ) {
        try {
            balanceService.addBalanceToUser(userId, currencyCode, new BigDecimal(amount));
            return "Saldo dodane poprawnie.";
        } catch (Exception e) {
            return "Błąd dodawania salda: " + e.getMessage();
        }
    }

    @WebMethod
    public String getBalanceForUserAndCurrency(
            @WebParam(name = "userId") Long userId,
            @WebParam(name = "currencyCode") String currencyCode
    ) {
        try {
            Balance balance = balanceService.getBalanceForUserAndCurrency(userId, currencyCode);

            if (balance == null) {
                return "Brak salda dla waluty " + currencyCode.toUpperCase();
            }

            return balance.getCurrencyCode() + ": " + balance.getAmount();
        } catch (Exception e) {
            return "Błąd pobierania salda: " + e.getMessage();
        }
    }

    @WebMethod
    public List<String> getBalancesForUser(@WebParam(name = "userId") Long userId) {
        try {
            List<Balance> balances = balanceService.getBalancesForUser(userId);

            return balances.stream()
                    .map(b -> b.getCurrencyCode() + ": " + b.getAmount())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return List.of("Błąd pobierania sald: " + e.getMessage());
        }
    }
}