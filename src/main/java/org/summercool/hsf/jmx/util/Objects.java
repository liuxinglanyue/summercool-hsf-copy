package org.summercool.hsf.jmx.util;

public class Objects {

	private Objects() {
	}

	/**
	 * @param <E>
	 *        The type of elements
	 * @param all
	 *        any number of elements
	 * @return the first element of {@code all} that is not null
	 * @throws NullPointerException
	 *         if no element is not-null
	 */
	public static <E> E firstNotNull(E... all) {
		for (E element : all) {
			if (element != null) {
				return element;
			}
		}
		throw new NullPointerException("All null arguments");
	}
}
