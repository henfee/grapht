/*
 * Grapht, an open source dependency injector.
 * Copyright 2014-2015 various contributors (see CONTRIBUTORS.txt)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.grouplens.grapht;

import javax.inject.Qualifier;

/**
 * Thrown when a binding configuration is invalid, which often occurs when an
 * implementation type is bound to a type that it is not a subclass of, or when
 * an annotation is intended to be used as a qualifier but has not been
 * annotated with {@link Qualifier}.
 * 
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 */
public class InvalidBindingException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final Class<?> type;
    
    public InvalidBindingException(Class<?> type) {
        this(type, "");
    }
    
    public InvalidBindingException(Class<?> type, String message) {
        this(type, message, null);
    }
    
    public InvalidBindingException(Class<?> type, Throwable t) {
        this(type, "", t);
    }
    
    public InvalidBindingException(Class<?> type, String message, Throwable t) {
        super(message, t);
        this.type = type;
    }
    
    /**
     * @return The type that is configured incorrectly, or is the cause of
     *         configuration errors
     */
    public Class<?> getType() {
        return type;
    }
    
    @Override
    public String getMessage() {
        return String.format("Error configuring %s: %s", type, super.getMessage());
    }
}
