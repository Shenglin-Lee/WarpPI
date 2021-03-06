package rules.functions;
/*
SETTINGS: (please don't move this part)
 PATH=functions.RootRule
*/

import java.math.BigDecimal;
import java.math.BigInteger;

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.FunctionOperator;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.Number;
import it.cavallium.warppi.math.functions.Root;
import it.cavallium.warppi.math.functions.RootSquare;
import it.cavallium.warppi.math.rules.Rule;
import it.cavallium.warppi.math.rules.RuleType;
import it.cavallium.warppi.util.Error;
import it.cavallium.warppi.util.Errors;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Root
 * a√b = c
 *
 * @author Andrea Cavalli
 *
 */
public class RootRule implements Rule {
	// Rule name
	@Override
	public String getRuleName() {
		return "Root";
	}

	// Rule type
	@Override
	public RuleType getRuleType() {
		return RuleType.CALCULATION;
	}

	/* Rule function
	   Returns:
	     - null if it's not executable on the function "f"
	     - An ObjectArrayList<Function> if it did something
	*/
	@Override
	public ObjectArrayList<Function> execute(final Function f) throws Error, InterruptedException {
		boolean isSquare = false;
		if ((isSquare = f instanceof RootSquare) || f instanceof Root) {
			final ObjectArrayList<Function> result = new ObjectArrayList<>();
			final MathContext mathContext = f.getMathContext();
			final Function variable1 = ((FunctionOperator) f).getParameter1();
			final Function variable2 = ((FunctionOperator) f).getParameter2();
			boolean isSolvable = false, canBePorted = false;
			if (variable1 instanceof Number && variable2 instanceof Number) {
				if (mathContext.exactMode) {
					result.add(((Number) variable1).pow(new Number(mathContext, BigDecimal.ONE).divide((Number) variable1)));
					return result;
				}
				isSolvable = isSolvable | !mathContext.exactMode;
				if (!isSolvable)
					try {
						final Function resultVar = ((Number) variable2).pow(new Number(mathContext, BigDecimal.ONE).divide((Number) variable1));
						final Function originalVariable = ((Number) resultVar).pow(new Number(mathContext, 2));
						if (originalVariable.equals(((FunctionOperator) f).getParameter2()))
							isSolvable = true;
					} catch (final Exception ex) {
						throw (Error) new Error(Errors.ERROR, ex.getMessage()).initCause(ex);
					}
			}
			if (!isSquare && !isSolvable && variable1 instanceof Number && variable1.equals(new Number(mathContext, 2)))
				canBePorted = true;

			if (isSolvable) {
				result.add(((Number) variable2).pow(new Number(mathContext, BigInteger.ONE).divide((Number) variable1)));
				return result;
			}
			if (canBePorted)
				result.add(new RootSquare(mathContext, variable2));
		}
		return null;
	}
}
