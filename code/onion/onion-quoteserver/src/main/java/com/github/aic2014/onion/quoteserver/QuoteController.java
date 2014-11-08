package com.github.aic2014.onion.quoteserver;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


@Controller
public class QuoteController {

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

            Document doc = Jsoup.connect("http://www.textfiles.com/science/").get();
            Elements tableElements = doc.select("table");
            Elements tableRowElements = tableElements.get(0).select(":not(thead) tr");

            for (int i = 1; i < tableRowElements.size(); i++) {
                Element row = tableRowElements.get(i);
                Elements rowItems = row.select("td");
                if (rowItems.size() == 3) {
                    PoemList.add("Filename: " + rowItems.get(0).text() + " Size: " + rowItems.get(1).text() +
                            " Description: " + rowItems.get(2).text());
                }
            }

            return PoemList;

        } catch (Exception  e) {
            e.printStackTrace();
            return PoemList;
        }
    }



}
