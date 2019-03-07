package net.akami.mask.operation;

import net.akami.mask.utils.ExpressionUtils;
import net.akami.mask.utils.FormatterFactory;
import net.akami.mask.utils.MathUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class Sum extends BinaryOperationHandler {

    private static final Sum INSTANCE = new Sum();

    @Override
    public String operate(String a, String b) {
        LOGGER.info("Sum process of {} |+| {}: \n", a, b);
        List<String> monomials = ExpressionUtils.toMonomials(a);
        monomials.addAll(ExpressionUtils.toMonomials(b));
        LOGGER.info("Monomials : {}", monomials);
        String result = monomialSum(monomials, false);
        LOGGER.info("---> Sum result of {} |+| {}: {}", a, b, result);
        return result;
    }

    /*public String monomialSum(List<String> monomials, boolean needsFormatting) {

        Map<String, Float> parts = new HashMap<>();

        for(String monomial : monomials) {
            if(monomial == null || monomial.isEmpty()) continue;

            String numericValue = ExpressionUtils.toNumericValue(monomial);
            String vars = monomial.replaceAll(Pattern.quote(numericValue), "");
            while (ExpressionUtils.areEdgesBracketsConnected(vars))
                vars = vars.substring(1, vars.length()-1);
            numericValue = FormatterFactory.removeFractions(numericValue);

            LOGGER.info("Vars from {} : {}", monomial, vars);
            if(!parts.containsKey(vars)) {
                parts.put(vars, Float.parseFloat(numericValue));
            } else {
                parts.put(vars, parts.get(vars) + Float.parseFloat(numericValue));
            }
        }
        clearBuilder();
        LOGGER.info("Map : {}", parts);
        for(String vars : parts.keySet()) {
            float numericValue = parts.get(vars);
            vars = vars.startsWith("+") ? vars.substring(1) : vars;
            if (numericValue == 0)
                continue;

            if(numericValue == 1 && !vars.isEmpty()) {
                BUILDER.append('+').append(vars);
            } else if(numericValue == -1 && !vars.isEmpty()) {
                BUILDER.append("-").append(vars);
            } else if(numericValue < 0) {
                BUILDER.append(MathUtils.cutSignificantZero(""+numericValue)).append(vars);
            } else {
                BUILDER.append('+').append(MathUtils.cutSignificantZero(""+numericValue)).append(vars);
            }
        }
        return BUILDER.toString().substring(1);
    }*/
    public String monomialSum(List<String> monomials, boolean needsFormatting) {

        List<String> finalMonomials = new ArrayList<>();

        for (int i = 0; i < monomials.size(); i++) {
            String part = monomials.get(i);
            LOGGER.info("Analyzing {}", part);
            if (part == null || part.isEmpty())
                continue;
            fillMonomialList(part, i, monomials, finalMonomials);
        }

        LOGGER.info("Inter step : {} and {}", monomials, finalMonomials);

        // All the monomials that couldn't be calculated because their unknown part was unique are eventually added
        finalMonomials.addAll(monomials);
        clearBuilder();
        for (String rest : finalMonomials) {
            if (rest == null)
                continue;

            if (rest.startsWith("+") || rest.startsWith("-")) {
                BUILDER.append(rest);
            } else {
                BUILDER.append("+" + rest);
            }
        }
        String result = BUILDER.toString();
        result = result.startsWith("+") ? result.substring(1) : result;
        LOGGER.debug("- Result of monomialSum / subtraction : {}", result);
        return needsFormatting ? outFormat(result) : result;
    }

    // TODO : Stop using this map
    private void fillMonomialList(String monomial, int i, List<String> initialMonomials, List<String> finalMonomials) {
        String vars = ExpressionUtils.toVariables(monomial);
        LOGGER.debug("Analyzing monomial {} : {}, found \"{}\" as variables", i, monomial, vars);
        // Adding all the "additionable" parts to the map, with their value and their index
        Map<BigDecimal, Integer> compatibleParts = new HashMap<>();

        for (int j = 0; j < initialMonomials.size(); j++) {
            // We don't want to add the part itself
            if (i == j)
                continue;

            String part2 = initialMonomials.get(j);
            if (part2 == null)
                continue;

            // If the unknown part is similar, we can add them
            if (ExpressionUtils.toVariables(part2).equals(vars)) {
                LOGGER.error("Numeric value of {} : {}", part2, ExpressionUtils.toNumericValue(part2));
                BigDecimal toAdd = new BigDecimal(ExpressionUtils.toNumericValue(part2));
                if (compatibleParts.containsKey(toAdd)) {
                    LOGGER.debug("Found copy in the map. Doubling the original.");
                    int index = compatibleParts.get(toAdd);
                    compatibleParts.remove(toAdd, index);
                    compatibleParts.put(toAdd.multiply(new BigDecimal("2")), index);
                    initialMonomials.set(j, null);
                    LOGGER.info("After copy : {}", compatibleParts);
                } else {
                    compatibleParts.put(toAdd, j);
                }
            }
        }
        LOGGER.info("Part for var {} : {}", vars, compatibleParts);
        replaceMonomialsByResult(monomial, vars, i, compatibleParts, initialMonomials, finalMonomials);
    }


    /**
     * Calculates the monomialSum of all numeric values of the monomials having vars as their unknown part, then
     * removes the calculated values from the initial list, and adds the result into the final list.
     *
     * Example :
     *
     * 2x + 2x + 3x
     * -> Compatible unknown part : x. Hence, initialMonomial is 2x, others contains 2x and 3x, it removes
     * the three monomials to the initial list, and adds 7x to the final list
     * @param initialMonomial
     * @param vars
     * @param index
     * @param others
     * @param initialMonomials
     * @param finalMonomials
     */
    private void replaceMonomialsByResult(String initialMonomial, String vars, int index, Map<BigDecimal, Integer> others,
                                          List<String> initialMonomials, List<String> finalMonomials) {
        LOGGER.debug("Init : {}, Final : {}. Vars : {}", initialMonomial, finalMonomials, vars);
        while(ExpressionUtils.areEdgesBracketsConnected(initialMonomial))
            initialMonomial = initialMonomial.substring(1, initialMonomial.length()-1);
        LOGGER.info("Numeric value of {} : {}", initialMonomial, ExpressionUtils.toNumericValue(initialMonomial));
        BigDecimal finalTotal = new BigDecimal(ExpressionUtils.toNumericValue(initialMonomial));
        for (BigDecimal value : others.keySet()) {
            LOGGER.debug("Value : " + value);
            finalTotal = finalTotal.add(value);
            // The compatible part is set to null in the list
            initialMonomials.set(others.get(value), null);
        }
        // The part itself is also set to null in the list
        initialMonomials.set(index, null);

        String numericTotal = finalTotal.toString();
        if (numericTotal.equals("1") && !vars.isEmpty()) {
            finalMonomials.add(vars);
        } else if (numericTotal.equals("-1") && !vars.isEmpty()) {
            finalMonomials.add("-" + vars);
        } else if(!numericTotal.matches("0\\.0+") && !numericTotal.equals("0")){
            finalMonomials.add(MathUtils.cutSignificantZero(numericTotal + vars));
        }
        LOGGER.info("Init : {}, Final : {}", initialMonomial, finalMonomials);
    }

    @Override
    public String inFormat(String origin) {
        String result = FormatterFactory.removeFractions(origin);
        LOGGER.info("{} became {}", origin, result);
        return result;
    }

    @Override
    public String outFormat(String origin) {
        if(origin.isEmpty()) {
            LOGGER.debug("RETURNED 0");
            return "0";
        }
        return ExpressionUtils.addMultShortcut(origin);
    }

    public static Sum getInstance() {
        return INSTANCE;
    }
}
