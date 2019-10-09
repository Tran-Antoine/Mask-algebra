package net.akami.atosym.utils;

import net.akami.atosym.core.MaskContext;
import net.akami.atosym.expression.MathObject;
import net.akami.atosym.expression.NumberExpression;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.function.BiFunction;

public class NumericUtils {

    private NumericUtils(){}

    public static float sum(float a, float b, MaskContext context) {
        return operation(a, b, context, BigDecimal::add);
    }

    public static float mult(float a, float b, MaskContext context) {
        return operation(a, b, context, BigDecimal::multiply);
    }

    private static float operation(float a, float b, MaskContext context, BiFunction<BigDecimal, BigDecimal, BigDecimal> func) {
        MathContext mathContext = context.getMathContext();
        BigDecimal bigA = new BigDecimal(a, mathContext);
        BigDecimal bigB = new BigDecimal(b, mathContext);
        return func.apply(bigA, bigB).floatValue();
    }

    public static boolean isZero(MathObject object) {
        return object instanceof NumberExpression && ((NumberExpression) object).getValue() == 0;
    }

    public static boolean isNotZero(MathObject object) {
        return !isZero(object);
    }
}
