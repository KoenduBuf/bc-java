package org.bouncycastle.crypto.modes.gcm;

import java.util.ArrayList;
import java.util.List;

public class Tables1kGCMExponentiator
    implements GCMExponentiator
{
    // A lookup table of the power-of-two powers of 'x'
    // - lookupPowX2[i] = x^(2^i)
    private List lookupPowX2;

    public void init(byte[] x)
    {
        long[] y = GCMUtil.asLongs(x);
        if (lookupPowX2 != null && 0L != GCMUtil.areEqual(y, (long[])lookupPowX2.get(0)))
        {
            return;
        }

        lookupPowX2 = new ArrayList(8);
        lookupPowX2.add(y);
    }

    public void exponentiateX(long pow, byte[] output)
    {
        long[] y = GCMUtil.oneAsLongs();
        int bit = 0;
        while (pow > 0)
        {
            if ((pow & 1L) != 0)
            {
                ensureAvailable(bit);
                GCMUtil.multiply(y, getMultiplier(bit));
            }
            ++bit;
            pow >>>= 1;
        }

        GCMUtil.asBytes(y, output);
    }

    private synchronized long[] getMultiplier(int bit)
    {
        ensureAvailable(bit);

        return (long[])lookupPowX2.get(bit);
    }

    private void ensureAvailable(int bit)
    {
        int last = lookupPowX2.size() - 1;
        if (last < bit)
        {
            long[] prev = (long[])lookupPowX2.get(last);
            do
            {
                long[] next = new long[GCMUtil.SIZE_LONGS];
                GCMUtil.square(prev, next);
                lookupPowX2.add(next);
                prev = next;
            }
            while (++last < bit);
        }
    }
}
