package io.github.syst3ms.skriptparser;

import io.github.syst3ms.skriptparser.classes.SkriptParser;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import io.github.syst3ms.skriptparser.registration.TypeManager;
import io.github.syst3ms.skriptparser.pattern.ChoiceElement;
import io.github.syst3ms.skriptparser.pattern.ChoiceGroup;
import io.github.syst3ms.skriptparser.pattern.CompoundElement;
import io.github.syst3ms.skriptparser.pattern.ExpressionElement;
import io.github.syst3ms.skriptparser.pattern.OptionalGroup;
import io.github.syst3ms.skriptparser.pattern.PatternElement;
import io.github.syst3ms.skriptparser.pattern.RegexGroup;
import io.github.syst3ms.skriptparser.pattern.TextElement;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class PatternParserTest {

    static {
        SkriptRegistration reg = new SkriptRegistration("unit-tests");
        reg.addType(Number.class, "number", "number(?<plural>s)?", Double::parseDouble);
        reg.addType(String.class, "string", "string(?<plural>s)?");
        reg.register();
    }

    @Test
    public void testParsePattern() throws Exception {
        PatternParser parser = new PatternParser();
        assertEquals(new TextElement("syntax"), parser.parsePattern("syntax"));
        assertEquals(new OptionalGroup(new TextElement("optional")), parser.parsePattern("[optional]"));
        PatternElement expected = new OptionalGroup(
            new CompoundElement(
                new TextElement("something "),
                new OptionalGroup(new TextElement("optional")),
                new TextElement(" again")
            )
        );
        assertEquals(expected, parser.parsePattern("[something [optional] again]"));
        assertEquals(new ChoiceGroup(new ChoiceElement(new TextElement("single choice"), 0)), parser.parsePattern("(single choice)"));
        assertEquals(new ChoiceGroup(new ChoiceElement(new TextElement("parse mark"), 1)), parser.parsePattern("(1\u00a6parse mark)"));
        expected = new ChoiceGroup(
            new ChoiceElement(new TextElement("first choice"), 0),
            new ChoiceElement(new TextElement("second choice"), 0)
        );
        assertEquals(expected, parser.parsePattern("(first choice|second choice)"));
        expected = new ChoiceGroup(
            new ChoiceElement(new TextElement("first mark"), 0),
            new ChoiceElement(new TextElement("second mark"), 1)
        );
        assertEquals(expected, parser.parsePattern("(first mark|1\u00a6second mark)"));
        expected = new OptionalGroup(
            new CompoundElement(
                new TextElement("lookie, "),
                new ChoiceGroup(
                    new ChoiceElement(new TextElement("another"), 0),
                    new ChoiceElement(new TextElement("choice"), 1)
                ),
                new TextElement(" !")
            )
        );
        assertEquals(expected, parser.parsePattern("[lookie, (another|1\u00a6choice) !]"));
        assertEquals(new RegexGroup(Pattern.compile(".+")), parser.parsePattern("<.+>"));
        assertEquals(
                new ExpressionElement(
                        Collections.singletonList(TypeManager.getInstance().getPatternType("number")),
                        ExpressionElement.Acceptance.BOTH),
                parser.parsePattern("%number%")
        );
        assertEquals(
                new ExpressionElement(
                    Arrays.asList(
                            TypeManager.getInstance().getPatternType("number"),
                            TypeManager.getInstance().getPatternType("strings")
                    ),
                        ExpressionElement.Acceptance.LITERALS_ONLY
                ),
                parser.parsePattern("%-*number/strings@1%")
        );
        assertNull(parser.parsePattern("(unclosed"));
        assertNull(parser.parsePattern("%unfinished type"));
    }

    @Test
    public void testMatch() throws Exception {
        PatternParser patternParser = new PatternParser();
        PatternElement pattern = patternParser.parsePattern("pattern");
        SkriptParser parser = new SkriptParser(pattern);
        assertEquals(7, pattern.match("pattern", 0, parser));
        pattern = patternParser.parsePattern("pattern [with optional]");
        parser = new SkriptParser(pattern);
        assertEquals(8, pattern.match("pattern", 0, parser));
        assertEquals(21, pattern.match("pattern with optional", 0, parser));
        pattern = patternParser.parsePattern("pattern [with [another] optional]");
        parser = new SkriptParser(pattern);
        assertEquals(22, pattern.match("pattern with optional", 0, parser));
        assertEquals(30, pattern.match("pattern with another optional", 0, parser));
        pattern = patternParser.parsePattern("you must (choose|this|or this)");
        parser = new SkriptParser(pattern);
        assertEquals(15, pattern.match("you must choose", 0, parser));
        assertEquals(13, pattern.match("you must this", 0, parser));
        assertEquals(16, pattern.match("you must or this", 0, parser));
        pattern = patternParser.parsePattern("I choose (1\u00a6this|2\u00a6that)");
        parser = new SkriptParser(pattern);
        assertTrue(pattern.match("I choose this", 0, parser) == 13 && parser.getParseMark() == 1);
        // The real stuff
        pattern = patternParser.parsePattern("say %number% [!]");
        parser = new SkriptParser(pattern);
        pattern.match("say 2", 0, parser);
        assertEquals(1, parser.getParsedExpressions().size());
        parser = new SkriptParser(pattern);
        pattern.match("say 2 !", 0, parser);
        assertEquals(1, parser.getParsedExpressions().size());
    }
}