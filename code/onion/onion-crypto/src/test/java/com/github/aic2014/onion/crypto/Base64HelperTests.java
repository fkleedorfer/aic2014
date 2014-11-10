package com.github.aic2014.onion.crypto;

import org.junit.Test;
import static org.junit.Assert.*;

public class Base64HelperTests {
    public static String vulcan = "Eyjafjallaj\u00f6kull";

    @Test
    public void Base64BasicTest()
    {
        String e = Base64Helper.encodeString(vulcan);
        System.out.printf("base64(%s): %s%n", vulcan, e);
        String d = Base64Helper.decodeString(e);
        assertEquals(vulcan, d);
    }
}
