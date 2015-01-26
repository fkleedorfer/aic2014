package com.github.aic2014.onion.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.aic2014.onion.exception.OnionRoutingException;
import com.github.aic2014.onion.model.ChainNodeRoutingStats;
import com.github.aic2014.onion.model.Message;
import com.github.aic2014.onion.model.RoutingInfo;

/**
 * Utilities for mapping Message objects to/from JSON.
 */
public class JsonUtils {
  public static Message fromJSON(String json){
    ObjectMapper mapper = new ObjectMapper();
    try {
      return mapper.readValue(json, Message.class);
    } catch (Exception e) {
      throw new OnionRoutingException("error serializing to JSON",e);
    }
  }
   public static ChainNodeRoutingStats routingInfoFromJson(String json){
       ObjectMapper mapper = new ObjectMapper();
       try {
           return mapper.readValue(json, ChainNodeRoutingStats.class);
       } catch (Exception e) {
           throw new OnionRoutingException("error serializing to JSON",e);
       }
   }

  public static String toJSON(Message message){
    ObjectMapper mapper = new ObjectMapper();
    try {
      return mapper.writeValueAsString(message);
    } catch (Exception e) {
      throw new OnionRoutingException("error serializing to JSON",e);
    }
  }
}
