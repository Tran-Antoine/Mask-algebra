package net.akami.atosym.handler;

import net.akami.atosym.core.MaskContext;
import net.akami.atosym.expression.Expression;
import net.akami.atosym.expression.FunctionSign;
import net.akami.atosym.expression.Monomial;
import net.akami.atosym.function.MathFunction;
import net.akami.atosym.merge.FairMerge;
import net.akami.atosym.merge.MonomialAdditionMerge;
import net.akami.atosym.merge.MonomialMultiplicationMerge;
import net.akami.atosym.merge.SequencedMerge;
import net.akami.atosym.merge.property.FairOverallMergeProperty;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Multiplier extends BinaryOperationHandler {

    public Multiplier(MaskContext context) {
        super(context);
    }

    @Override
    public Expression operate(Expression a, Expression b) {

        List<Monomial> aMonomials = a.getElements();
        List<Monomial> bMonomials = b.getElements();

        LOGGER.debug("Operating mult {} * {}", a, b);
        if(a.length() != 0 && aMonomials.get(0) instanceof FunctionSign) {
            return functionOperation(a, b);
        }

        if(b.length() != 0 && bMonomials.get(0) instanceof FunctionSign) {
            return functionOperation(b, a);
        }

        List<Monomial> reducedResult = resolveMult(aMonomials, bMonomials);
        return new Expression(reducedResult);
    }

    private Expression functionOperation(Expression bindingExpression, Expression target) {
        char binding = bindingExpression.get(0).getExpression().charAt(0);
        Optional<MathFunction> optional = context.getFunctionByBinding(binding);
        if(optional.isPresent()) return optional.get().rawOperate(target);
        else throw new IllegalStateException("Unknown binding");
    }

    private List<Monomial> resolveMult(List<Monomial> aMonomials, List<Monomial> bMonomials) {

        int initialCapacity = aMonomials.size() * bMonomials.size();

        FairMerge<Monomial, FairOverallMergeProperty<Monomial>> multBehavior = new MonomialMultiplicationMerge(context);
        SequencedMerge<Monomial> additionBehavior = new MonomialAdditionMerge(context);

        List<Monomial> rawResult = new ArrayList<>(initialCapacity);

        for(Monomial a : aMonomials) {
            for(Monomial b : bMonomials) {
                rawResult.add(multBehavior.merge(a, b, false));
            }
        }
        List<Monomial> reducedResult = additionBehavior.merge(rawResult, rawResult, true);
        Collections.sort(reducedResult);
        return reducedResult;
    }

    public float mult(float a, float b) {
        BigDecimal bigA = new BigDecimal(a, context.getMathContext());
        BigDecimal bigB = new BigDecimal(b, context.getMathContext());
        return bigA.multiply(bigB).floatValue();
    }
}
