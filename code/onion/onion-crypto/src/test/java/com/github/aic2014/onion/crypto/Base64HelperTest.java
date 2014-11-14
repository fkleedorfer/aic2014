package com.github.aic2014.onion.crypto;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

public class Base64HelperTest {
    public static String vulcan = "Eyjafjallaj\u00f6kull";
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    public void BasicTest()
    {
        String e = Base64Helper.encodeString(vulcan);
        logger.info("base64({}): {}", vulcan, e);
        String d = Base64Helper.decodeString(e);
        assertEquals(vulcan, d);
    }
}
