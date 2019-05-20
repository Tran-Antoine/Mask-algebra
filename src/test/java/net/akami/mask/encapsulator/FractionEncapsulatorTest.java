package net.akami.mask.encapsulator;

import net.akami.mask.expression.ExpressionElement;
import net.akami.mask.expression.NumberElement;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class FractionEncapsulatorTest {

    @Test
    public void formatTest() {
        List<ExpressionElement> list = Arrays.asList(
                new NumberElement(4)
        );
        List<ExpressionElement> list2 = Arrays.asList(
                new NumberElement(4),
                new NumberElement(5)
        );

        FractionEncapsulator encapsulator = new FractionEncapsulator(5);
        FractionEncapsulator encapsulator2 = new FractionEncapsulator(list2);
        assertFormat(encapsulator, list, "/5.0");
        assertFormat(encapsulator, list2, "()/5.0");
        assertFormat(encapsulator2, list, "/(4.0+5.0)");
        assertFormat(encapsulator2, list2, "()/(4.0+5.0)");
    }

    private void assertFormat(FractionEncapsulator frac, List<ExpressionElement> list, String r) {
        assertThat(String.join("", frac.getEncapsulationString(list, 0, null))).isEqualTo(r);
    }
}
