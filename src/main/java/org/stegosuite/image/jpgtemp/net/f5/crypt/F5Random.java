package org.stegosuite.image.jpgtemp.net.f5.crypt;

import java.security.SecureRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import java.util.Random;

public class F5Random {

    private final SecureRandom random;

    private static final Logger LOG = LoggerFactory.getLogger(F5Random.class);

    private final byte[] b;

    public F5Random(final String password) {
        this.random = new SecureRandom();
        long seed = stringToNumber(password);
        LOG.debug("Seed: {}", seed);
        this.random.setSeed(seed);
        this.b = new byte[1];
    }

    // get a random byte
    public int getNextByte() {
        this.random.nextBytes(this.b);
        return this.b[0];
    }

    public static long stringToNumber(String s) {
        long result = 0;
        for (int i = 0; i < s.length(); i++) {
            final char ch = s.charAt(i);
            result += (int) ch;
        }
        return result;
    }

    // get a random integer 0 ... (maxValue-1)
    public int getNextValue(final int maxValue) {
        int retVal = getNextByte() | getNextByte() << 8 | getNextByte() << 16 | getNextByte() << 24;
        retVal %= maxValue;
        if (retVal < 0) {
            retVal += maxValue;
        }
        return retVal;
    }
}
