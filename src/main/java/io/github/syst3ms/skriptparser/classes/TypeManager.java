package io.github.syst3ms.skriptparser.classes;

import org.intellij.lang.annotations.Language;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * Manages all different types.
 */
public class TypeManager {
	private static Map<String, Type<?>> nameToType = new HashMap<>();

	public static Type<?> getByExactName(String name) {
		return nameToType.get(name);
	}

	public static Type<?> getByName(String name) {
		for (Type<?> t : nameToType.values()) {
			Matcher m = t.getSyntaxPattern().matcher(name);
			if (m.matches()) {
				return t;
			}
		}
		return null;
	}

	public static PatternType<?> getPatternType(String name) {
		if (nameToType.containsKey(name)) { // Might as well avoid the for loop in this case
			return new PatternType<>(nameToType.get(name), false);
		}
		for (Type<?> t : nameToType.values()) {
			Matcher m = t.getSyntaxPattern().matcher(name);
			if (m.matches()) {
				String pluralGroup = m.group("plural");
				return new PatternType<>(t, pluralGroup == null);
			}
		}
		return null;
	}

	public static <T> void registerType(Class<T> c, String name, @Language("Regexp") String pattern) {
		nameToType.put(name, new Type<>(c, name, pattern));
	}
}