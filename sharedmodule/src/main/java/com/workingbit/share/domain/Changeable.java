package com.workingbit.share.domain;


import com.workingbit.share.domain.impl.BoardBox;

public interface Changeable {
	/**
	 * Undoes an action
	 */
	BoardBox undo();

	/**
	 * Redoes an action
	 */
	BoardBox redo();
}