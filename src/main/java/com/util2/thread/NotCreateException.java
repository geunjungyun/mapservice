package com.util2.thread;

public class NotCreateException extends Exception {

	/**
	 * 생성자
	 */

	public NotCreateException() {

		super("Not Create Exception");

	} // end public NotCreateException()

	/**
	 * 생성자
	 *
	 * @param exception 예외 내용
	 */

	public NotCreateException(String exception) {

		super(exception);

	} // end public NotCreateException(String exception)

} // end class NotCreateException
