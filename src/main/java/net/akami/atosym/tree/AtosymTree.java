package net.akami.atosym.tree;

import net.akami.atosym.expression.Expression;
import net.akami.atosym.expression.MathObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public abstract class AtosymTree<T extends AtosymBranch> implements Iterable<T> {

    public static final Logger LOGGER = LoggerFactory.getLogger(BinaryTree.class);
    private List<T> branches;

    public AtosymTree(String expression) {
        this.branches = new ArrayList<>();
        load(expression);
    }

    /**
     * Loads a new branch for the given expression. Note that {@link BinaryTree#generate(String)} should
     * never be used outside binary classes, since it should only return a branch from the given expression, whereas
     * the load() method performs necessary actions for splitting the tree.
     * @param expression the given expression
     * @return a branch created from the expression given
     */
    public T load(String expression) {
        T initial = generate(expression);
        branches.add(initial);
        begin(initial);
        return initial;
    }

    /**
     * Defines how the splitting of a defined branch must be planned. <br>
     * In other words, begin must call the split() method one or more times according to the different splitters.
     * @param self the branch itself
     */
    protected abstract void begin(T self);

    /**
     * Defines how each branch must be split. Note that checks concerning size / values of the char array are
     * usually not needed, since the user is supposed to know what the array must be.
     * @param self the current branch not split yet
     * @param by the 'splitters', which are used to determine the left and right part of the branch
     *
     * @return whether the branch could be split with the chars given or not
     */
    protected abstract boolean split(T self, char... by);

    /**
     * Allows the class itself to instantiate branches from a given expression
     * @param origin the string the branch must be based on
     * @return a branch getting along with the kind of tree being used, from the given origin
     */
    protected abstract T generate(String origin);

    /**
     * Defines how a branch must be evaluated.
     * Note that if the branch type used hasn't redefined the {@code canBeEvaluated} method, you are guaranteed that
     * the branch has a left and a right part.
     * @param self the branch itself
     */
    protected abstract void evalBranch(T self);

    /**
     * Merges the whole tree. The usual behavior is to go from the last branch to the first one,
     * see if the current actually is calculable, and if yes then calls the {@link BinaryTree#evalBranch(Branch)}
     * method. <br>
     * If the finalResult method does not return an empty optional, the value found is returned <br>
     * Note that the merge method can be redefined if the behavior does not suits the tree.
     * @return the reduced value of the first branch, while finalResult() is not redefined
     */
    public MathObject merge() {

        //Merging the branches from the last one to the first one (this initial expression)
        for (int i = getBranches().size() - 1; i >= 0; i--) {

            T self = getBranches().get(i);
            self.merge();

            if(finalResult().isPresent()) {
                return finalResult().get();
            }
        }
        return null;
    }

    /**
     * Defines whether the final findResult has been calculated or not.
     * @return the final findResult if calculated, otherwise an empty optional.
     */
    public Optional<MathObject> finalResult() {
        T first = getBranches().get(0);
        if (first.hasReducedValue()) {
            return Optional.of(first.getReducedValue());
        }
        return Optional.empty();
    }

    /**
     *
     * @return the iterator of the branches' list
     */
    @Override
    public final Iterator<T> iterator() {
        return branches.iterator();
    }

    public List<T> getBranches() {
        return branches;
    }
}
