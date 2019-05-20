package net.akami.mask.handler;

import net.akami.mask.affection.CalculationAlteration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface AlterationHandler<T extends CalculationAlteration, R> {

    List<T> getAffections();

    default <S extends T> Optional<S> getAffection(Class<S> type) {
        for(T affection : getAffections()) {
            if(affection.getClass().equals(type))
                return (Optional<S>) Optional.of(affection);
        }
        return Optional.empty();
    }

    default List<T> compatibleAlterationsFor(R... input) {
        List<T> compatibles = new ArrayList<>();

        for(T affection : getAffections()) {
            if(affection.appliesTo(input))
                compatibles.add(affection);
        }

        return compatibles;
    }

    R findResult(R... input);
}
