package com.github.aic2014.onion.quoteserver;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class QuoteController
{
  @RequestMapping(
    value="/quote",
    method = RequestMethod.GET)
  public ResponseEntity getQuote(){
    return new ResponseEntity<String>(fetchRandomQuote(),HttpStatus.OK);
  }

  /**
   * Fetches a random quote.
   * @return
   */
  private String fetchRandomQuote(){
    //TODO: make more random
    return "A poem is never finished, only abandoned. " +
      "- Paul Valery";
  }
}
