package net.akami.atosym.operator;

import net.akami.atosym.expression.MathObject;
import net.akami.atosym.expression.SineMathObject;

import java.util.List;
import java.util.function.Function;

public class SineOperator extends TrigonometryOperator {

    public SineOperator() {
        super("sin");
    }

    @Override
    protected Function<Double, Double> getOperation() {
        return Math::sin;
    }

    @Override
    protected MathObject toObject(List<MathObject> input) {
        return new SineMathObject(input);
    }
}
