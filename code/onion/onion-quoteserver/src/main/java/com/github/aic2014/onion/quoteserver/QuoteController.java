package com.github.aic2014.onion.quoteserver;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;

@PropertySource("file:${ONION_CONF_DIR}/quoteserver.properties")
@Controller
public class QuoteController {
    
    //ToDo - klappt nicht mit dem Property - geht das nur im config?
    @Value("${quotesUrl}")
    private String quotesUrl = "conf.local" + File.separator + "quotes.txt";
    
    private List<String> RSSFeed;

    //hier wird die Quote-Liste geladen - denk aber das wird anders initialisiert - aber wie? klären wir am Montag
    public QuoteController()
    {
        this.RSSFeed = LoadRSSFeed();
    }


    @RequestMapping(
            value = "/quote",
            method = RequestMethod.GET)
    public ResponseEntity getQuote() {
        return new ResponseEntity<String>(fetchRandomQuote(), HttpStatus.OK);
    }

    /**
     * Fetches a random quote.
     *
     * @return
     */
    private String fetchRandomQuote() {

        Random randomGenerator = new Random();
        int randomInt = randomGenerator.nextInt(this.RSSFeed.size());
        String poem = this.RSSFeed.get(randomInt);
        return poem;
    }

    private List<String> LoadRSSFeed()
    {
        List<String> PoemList = new ArrayList<String>();
        try
        {

            BufferedReader in = new BufferedReader(new FileReader(quotesUrl));

            String s, quote = "";
            Boolean Started = false, reading = false, isUpperCase = false;
            char c;
            while (in.ready()) {
                s = in.readLine();
                if (!Started && s.contains("---------------------------------------------"))
                  Started = true;

                if (Started && (!s.trim().equals("")) && !s.contains("---------------------------------------------") )
                {
                    isUpperCase = true;
                    for (int i = 0; i < s.trim().length(); i ++)
                    {
                        c = s.charAt(i);
                        if (!Character.isUpperCase(c) && !Character.isWhitespace(c) )
                        {
                            isUpperCase = false;
                            break;
                        }
                    }
                    if (!isUpperCase)
                    {
                        quote += s + " ";
                        reading = true;
                    }
                }
                if (reading && (s.contains("---------------------------------------------")|| s.trim().equals("")) )
                {
                    reading = false;
                    PoemList.add(quote.trim());
                    quote = "";
                }
            }
            in.close();

            return PoemList;

        } catch (Exception  e) {
            e.printStackTrace();
            return PoemList;
        }
    }



}
