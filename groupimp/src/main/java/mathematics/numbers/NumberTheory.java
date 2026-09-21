package mathematics.numbers;

import mathematics.core.MathFailure;
import java.math.BigInteger;

public final class NumberTheory {
    private NumberTheory() { }
    public static BigInteger lcm(BigInteger a, BigInteger b) {
        return a.signum() == 0 || b.signum() == 0 ? BigInteger.ZERO : a.divide(a.gcd(b)).multiply(b).abs();
    }
    /** Exact bounded integer primality, without probabilistic certainty labels. */
    public static boolean isPrime(int n) {
        if (n < 2) return false;
        for (int d=2; (long)d*d <= n; d++) if (n%d == 0) return false;
        return true;
    }
    public static BigInteger factorial(int n) {
        if (n < 0) throw MathFailure.invalid("Factorial requires a nonnegative integer");
        BigInteger result=BigInteger.ONE;
        for (long i=2;i<=n;i++) result=result.multiply(BigInteger.valueOf(i));
        return result;
    }
}
