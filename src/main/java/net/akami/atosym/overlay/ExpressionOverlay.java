package net.akami.atosym.overlay;

import java.util.List;

public interface ExpressionOverlay {

    String[] getEncapsulationString(List<Monomial> elements, int index, List<ExpressionOverlay> others);

}
