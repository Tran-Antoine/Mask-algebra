package net.akami.atosym.expression;

import net.akami.atosym.display.visitor.DisplayerVisitor;
import net.akami.atosym.display.visitor.DivisionDisplayer;

import java.util.List;

public class DivisionMathObject extends FunctionObject {

    private DisplayerVisitor displayer;

    public DivisionMathObject(List<MathObject> children) {
        super(children, 2);
        this.displayer = new DivisionDisplayer(children, this);
    }

    @Override
    public MathObjectType getType() {
        return MathObjectType.DIV;
    }

    @Override
    public int priority() {
        return 1;
    }

    @Override
    public DisplayerVisitor getDisplayer() {
        return displayer;
    }
}
