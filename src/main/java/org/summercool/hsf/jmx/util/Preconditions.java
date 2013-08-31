package org.summercool.hsf.jmx.util;

public class Preconditions {

	private Preconditions() {
	}

	/**
	 * An assertion method that makes null validation more fluent
	 * 
	 * @param <E>
	 *        The type of elements
	 * @param obj
	 *        an Object
	 * @return {@code obj}
	 * @throws NullPointerException
	 *         if {@code obj} is null
	 */
	public static <E> E notNull(E obj) {
		return notNull(obj, null);
	}

	/**
	 * An assertion method that makes null validation more fluent
	 * 
	 * @param <E>
	 *        The type of elements
	 * @param obj
	 *        an Object
	 * @param msg
	 *        a message that is reported in the exception
	 * @return {@code obj}
	 * @throws NullPointerException
	 *         if {@code obj} is null
	 */
	public static <E> E notNull(E obj, String msg) {
		if (obj == null) {
			throw (msg == null) ? new NullPointerException() : new NullPointerException(msg);
		}
		return obj;
	}
}
