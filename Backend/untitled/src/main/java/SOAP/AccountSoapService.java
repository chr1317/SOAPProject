package SOAP;

import Entity.AccountTransaction;
import Entity.Balance;
import Service.AccountHistoryService;
import Service.BalanceService;
import Service.ExchangeService;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@WebService
public class AccountSoapService {

    private final BalanceService balanceService = new BalanceService();
    private final ExchangeService exchangeService = new ExchangeService();
    private final AccountHistoryService accountHistoryService = new AccountHistoryService();

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

    @WebMethod
    public String exchangeCurrency(
            @WebParam(name = "userId") Long userId,
            @WebParam(name = "fromCurrency") String fromCurrency,
            @WebParam(name = "toCurrency") String toCurrency,
            @WebParam(name = "sourceAmount") String sourceAmount
    ) {
        try {
            exchangeService.exchangeCurrency(
                    userId,
                    fromCurrency,
                    toCurrency,
                    new BigDecimal(sourceAmount)
            );
            return "Wymiana walut wykonana poprawnie.";
        } catch (Exception e) {
            return "Błąd wymiany walut: " + e.getMessage();
        }
    }

    @WebMethod
    public List<String> getAvailableCurrencyCodes() {
        try {
            return exchangeService.getAvailableCurrencyCodes();
        } catch (Exception e) {
            return List.of("Błąd pobierania listy walut: " + e.getMessage());
        }
    }

    @WebMethod
    public List<String> getAccountTransactionsForUser(@WebParam(name = "userId") Long userId) {
        try {
            return accountHistoryService.getTransactionsForUser(userId)
                    .stream()
                    .map(t -> {
                        if (t.getTransactionType().name().equals("DEPOSIT")) {
                            return "DEPOSIT | " + t.getCurrencyCode() + " | " + t.getAmount() + " | " + t.getStatus();
                        } else if (t.getTransactionType().name().equals("WITHDRAW")) {
                            return "WITHDRAW | " + t.getCurrencyCode() + " | " + t.getAmount() + " | " + t.getStatus();
                        } else {
                            return "EXCHANGE | " + t.getFromCurrency() + " -> " + t.getToCurrency()
                                    + " | " + t.getSourceAmount() + " -> " + t.getTargetAmount()
                                    + " | rate=" + t.getExchangeRate()
                                    + " | " + t.getStatus();
                        }
                    })
                    .toList();
        } catch (Exception e) {
            return List.of("Błąd pobierania historii operacji: " + e.getMessage());
        }
    }
}