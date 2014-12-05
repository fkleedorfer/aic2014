package com.github.aic2014.onion.quoteserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@PropertySource("file:${ONION_CONF_DIR}/quoteserver.properties")
@Controller
public class QuoteController {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private List<String> quotes = new ArrayList<>();
    private Random random = new Random();

    @Value("${quotesFilename}")
    private String quotesFilename;

    @RequestMapping(
            value = "/quote",
            method = RequestMethod.GET)
    public ResponseEntity<Quote> getQuote() {
        return new ResponseEntity<Quote>(getRandomQuote(), HttpStatus.OK);
    }

    /**
     * Fetches a random quote.
     *
     * @return
     */
    private Quote getRandomQuote() {
        if (this.quotes.isEmpty()) {
            logger.error("No quotes here");
            return new Quote("Oops, ran out of quotes.");
        }

        int randomInt = random.nextInt(this.quotes.size());
        return new Quote(quotes.get(randomInt));
    }

    private String getConfDir() {
        return System.getProperty("ONION_CONF_DIR");
    }

    private File getQuotesFile() {
        File quotes = new File(quotesFilename);
        if (!quotes.isAbsolute() && getConfDir() != null)
            quotes = new File(getConfDir(), quotesFilename);
        return quotes;
    }

    private void LoadQuotes() {
        quotes.clear();
        File quotesFile = getQuotesFile();

        try (BufferedReader in = new BufferedReader(new FileReader(quotesFile))) {
            logger.info("Loading quotes from {}", quotesFile);

            String s, quote = "";
            Boolean started = false, reading = false, isUpperCase;
            char c;
            while (in.ready()) {
                s = in.readLine();
                if (!started && s.contains("---------------------------------------------"))
                    started = true;

                if (started && (!s.trim().equals("")) && !s.contains("---------------------------------------------")) {
                    isUpperCase = true;
                    for (int i = 0; i < s.trim().length(); i++) {
                        c = s.charAt(i);
                        if (!Character.isUpperCase(c) && !Character.isWhitespace(c)) {
                            isUpperCase = false;
                            break;
                        }
                    }
                    if (!isUpperCase) {
                        quote += s + " ";
                        reading = true;
                    }
                }
                if (reading && (s.contains("---------------------------------------------") || s.trim().equals(""))) {
                    reading = false;
                    quotes.add(quote.trim());
                    quote = "";
                }
            }
            logger.info("Loaded {} quotes", quotes.size());

        } catch (Exception e) {
            logger.error("Quotes loading", e);
        }
    }

    @PostConstruct
    private void loadOneLineQuotes() {
        quotes.clear();
        File quotesFile = getQuotesFile();

        try (BufferedReader in = new BufferedReader(new FileReader(quotesFile))) {
            logger.info("Loading quotes from {}", quotesFile);

            String s, quote = "";
            Boolean started = false, reading = false, isUpperCase;
            while (in.ready()) {
                s = in.readLine();
                quotes.add(s);
            }
            logger.info("Loaded {} quotes", quotes.size());

        } catch (Exception e) {
            logger.error("Quotes loading", e);
        }
    }


}
