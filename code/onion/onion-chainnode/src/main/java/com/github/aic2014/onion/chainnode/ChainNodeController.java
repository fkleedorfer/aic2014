package com.github.aic2014.onion.chainnode;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Controller that offers onion chain node functionality.
 */
@Controller
public class ChainNodeController
{
  @RequestMapping("/route")
  public ResponseEntity<String> performOnionRoutingStep(HttpServletRequest request, HttpServletResponse response)
    throws IOException {
    //TODO: return type must probably be changed, too!
    //TODO: implement onion routing step
    return new ResponseEntity<String>("hello onion!", HttpStatus.OK);
  }
}
