package org.summercool.hsf.jmx.util;

import java.util.Arrays;
import java.util.EnumSet;

public class Collections {
	private Collections() {
		// non instantiable
	}

	static <E extends Enum<E>> EnumSet<E> setOf(Class<E> clazz, E... elements) {
		if (elements == null || elements.length == 0) {
			return EnumSet.noneOf(clazz);
		} else {
			return EnumSet.copyOf(Arrays.asList(elements));
		}
	}
}
