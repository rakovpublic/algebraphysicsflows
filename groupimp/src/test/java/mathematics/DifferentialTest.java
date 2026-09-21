package mathematics;
import mathematics.numbers.Rational;
import mathematics.linear.RationalMatrix;
import org.junit.Test;
import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import static org.junit.Assert.*;
public class DifferentialTest {
    private Rational rational(String text) {
        String[] pair=text.split("/");
        return new Rational(new BigInteger(pair[0]),pair.length==2?new BigInteger(pair[1]):BigInteger.ONE);
    }
    @Test public void agreesWithIndependentFractionAndLeibnizFixtures() throws Exception {
        int cases=0;
        try(BufferedReader input=new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/mathematics/reference.tsv"),StandardCharsets.UTF_8))) {
            String line;
            while((line=input.readLine())!=null) {
                if(line.startsWith("#")) continue;
                String[] fields=line.split("\\t");
                if(fields[0].equals("rational")) {
                    Rational a=rational(fields[1]),b=rational(fields[2]);
                    assertEquals(line,rational(fields[3]),a.add(b));
                    assertEquals(line,rational(fields[4]),a.multiply(b));
                    assertEquals(line,rational(fields[5]),a.divide(b));
                } else {
                    int n=Integer.parseInt(fields[1]); String[] entries=fields[2].split(",");
                    Rational[][] matrix=new Rational[n][n];
                    for(int r=0;r<n;r++) for(int c=0;c<n;c++) matrix[r][c]=rational(entries[r*n+c]);
                    assertEquals(line,rational(fields[3]),new RationalMatrix(matrix).determinant());
                }
                cases++;
            }
        }
        assertEquals(104,cases);
    }
}
