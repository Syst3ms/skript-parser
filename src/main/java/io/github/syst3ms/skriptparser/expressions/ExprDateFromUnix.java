package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.util.SkriptDate;
import org.jetbrains.annotations.Nullable;

/**
 * The date from a (unix) timestamp.
 * The default timestamp returns the amount of <b>milliseconds</b> since the Unix Epoch.
 * The unix timestamp returns the amount of <b>seconds</b> since that same date.
 * The Unix Epoch is defined as January 1st 1970.
 *
 * @name Date from Unix
 * @type EXPRESSION
 * @pattern [the] date (from|of) [the] [unix] timestamp %number%
 * @since ALPHA
 * @author Mwexim
 */
public class ExprDateFromUnix implements Expression<SkriptDate> {

	static {
		Parser.getMainRegistration().addExpression(
				ExprDateFromUnix.class,
				SkriptDate.class,
				true,
				"[the] date (from|of) [the] [1:unix] timestamp %number%");
	}

	Expression<Number> timestamp;
	boolean unix;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
		unix = parseContext.getParseMark() == 1;
		timestamp = (Expression<Number>) expressions[0];
		return true;
	}

	@Override
	public SkriptDate[] getValues(TriggerContext ctx) {
		return timestamp.getSingle(ctx)
				.map(
					t -> new SkriptDate[]{
							unix ? new SkriptDate(t.longValue() * 1000) : new SkriptDate(t.longValue())
					}
				).orElse(new SkriptDate[0]);
	}

	@Override
	public String toString(@Nullable TriggerContext ctx, boolean debug) {
		return "date from " + (unix ? "unix " : "") + "timestamp " + timestamp.toString(ctx, debug);
	}
}
