package com.github.aic2014.onion.chainnode;

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
public class RoutingInfoController {

    @Autowired
    private RoutingInfoService routingInfoService;

    @RequestMapping(value="/routingInfo", method = RequestMethod.GET)
    public ResponseEntity<List<RoutingInfo>> getLatestRoutingInfo(){
        return new ResponseEntity<>(routingInfoService.getRoutingInfo(), HttpStatus.OK);
    }
}
