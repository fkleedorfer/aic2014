package com.github.aic2014.onion.client;

import com.github.aic2014.onion.model.RoutingInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

/**
 * Controller that provides routing information for debugging/demo purposes.
 */
@Controller
public class OnionClientAppController {

    @RequestMapping(value="/sendRequest", method = RequestMethod.GET)
    public String sendRequest(){

        return "";
    }

    @RequestMapping(value="/sendBomb", method = RequestMethod.GET)
    public String sendBomb(){

        return "";
    }

    @RequestMapping(value="/sendHelp", method = RequestMethod.GET)
    public ResponseEntity<String> sendHelp(){
        String a = "Welcome to the Onion Routing Demo! :)\nThis are your commands:\n!send ... sends a request and prints the response to the Console\n!bomb N ... sends N requests multiple parallel threads\n!help ... shows this usage notice\n!exit ... stops the Client";
        return new ResponseEntity<>(a, HttpStatus.OK);
    }

    @RequestMapping(value="/sendExit", method = RequestMethod.GET)
    public String sendExit(){

        return "";
    }

}