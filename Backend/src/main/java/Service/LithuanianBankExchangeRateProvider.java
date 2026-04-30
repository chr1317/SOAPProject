package Service;

import External.lbRates.FxRates;
import External.lbRates.FxRatesSoap;
import External.lbRates.GetCurrentFxRatesResponse;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class LithuanianBankExchangeRateProvider implements ExchangeRateProvider {

    private static final String RATE_TYPE = "EU";

    @Override
    public BigDecimal getExchangeRate(String fromCurrency, String toCurrency) {
        String from = fromCurrency.toUpperCase();
        String to = toCurrency.toUpperCase();

        if (from.equals(to)) {
            return BigDecimal.ONE;
        }

        Map<String, BigDecimal> eurRates = loadEurRates();

        BigDecimal eurToFrom = getEurRate(eurRates, from);
        BigDecimal eurToTo = getEurRate(eurRates, to);

        // EUR -> X
        if (from.equals("EUR")) {
            return eurToTo;
        }

        // X -> EUR
        if (to.equals("EUR")) {
            return BigDecimal.ONE.divide(eurToFrom, 8, RoundingMode.HALF_UP);
        }

        // X -> Y = (EUR -> Y) / (EUR -> X)
        return eurToTo.divide(eurToFrom, 8, RoundingMode.HALF_UP);
    }

    private Map<String, BigDecimal> loadEurRates() {
        try {
            FxRates service = new FxRates();
            FxRatesSoap port = service.getFxRatesSoap();

            GetCurrentFxRatesResponse.GetCurrentFxRatesResult result =
                    port.getCurrentFxRates(RATE_TYPE);

            Map<String, BigDecimal> rates = new HashMap<>();
            rates.put("EUR", BigDecimal.ONE);

            for (Object item : result.getContent()) {
                if (item instanceof Element rootElement) {
                    parseRates(rootElement, rates);
                }
            }

            return rates;
        } catch (Exception e) {
            throw new RuntimeException("Błąd pobierania kursów z Banku Litwy.", e);
        }
    }

    private void parseRates(Element rootElement, Map<String, BigDecimal> rates) {
        NodeList fxRateNodes = rootElement.getElementsByTagName("FxRate");

        for (int i = 0; i < fxRateNodes.getLength(); i++) {
            Element fxRate = (Element) fxRateNodes.item(i);
            NodeList ccyAmtNodes = fxRate.getElementsByTagName("CcyAmt");

            String currency = null;
            BigDecimal amount = null;

            for (int j = 0; j < ccyAmtNodes.getLength(); j++) {
                Element ccyAmt = (Element) ccyAmtNodes.item(j);

                String ccy = getText(ccyAmt, "Ccy");
                String amt = getText(ccyAmt, "Amt");

                if (!"EUR".equalsIgnoreCase(ccy)) {
                    currency = ccy.toUpperCase();
                    amount = new BigDecimal(amt);
                }
            }

            if (currency != null && amount != null) {
                rates.put(currency, amount);
            }
        }
    }

    @Override
    public List<String> getAvailableCurrencyCodes() {
        Map<String, BigDecimal> rates = loadEurRates();

        List<String> codes = new ArrayList<>(rates.keySet());
        Collections.sort(codes);

        return codes;
    }

    private BigDecimal getEurRate(Map<String, BigDecimal> rates, String currency) {
        BigDecimal rate = rates.get(currency);

        if (rate == null) {
            throw new RuntimeException("Brak kursu dla waluty: " + currency);
        }

        return rate;
    }

    private String getText(Element parent, String tagName) {
        NodeList list = parent.getElementsByTagName(tagName);

        if (list.getLength() == 0) {
            throw new RuntimeException("Brak elementu XML: " + tagName);
        }

        return list.item(0).getTextContent();
    }
}