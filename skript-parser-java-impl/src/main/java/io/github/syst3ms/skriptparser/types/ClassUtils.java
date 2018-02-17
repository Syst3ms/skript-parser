package io.github.syst3ms.skriptparser.types;

public class ClassUtils {
	public final static Class<?> getCommonSuperclass(final Class<?>... cs) {
		assert cs.length > 0;
		Class<?> r = cs[0];
		assert r != null;
		outer: for (final Class<?> c : cs) {
			assert c != null && !c.isArray() && !c.isPrimitive() : c;
			if (c.isAssignableFrom(r)) {
				r = c;
				continue;
			}
			if (!r.isAssignableFrom(c)) {
				Class<?> s = c;
				while ((s = s.getSuperclass()) != null) {
					if (s != Object.class && s.isAssignableFrom(r)) {
						r = s;
						continue outer;
					}
				}
				for (final Class<?> i : c.getInterfaces()) {
					s = getCommonSuperclass(i, r);
					if (s != Object.class) {
						r = s;
						continue outer;
					}
				}
				return Object.class;
			}
		}
		return r;
	}
}