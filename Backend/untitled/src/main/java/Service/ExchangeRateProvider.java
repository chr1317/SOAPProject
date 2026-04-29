package Service;

import java.math.BigDecimal;
import java.util.List;

public interface ExchangeRateProvider {
    BigDecimal getExchangeRate(String fromCurrency, String toCurrency);

    List<String> getAvailableCurrencyCodes();
}