/*
 * Grapht, an open source dependency injector.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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

import org.grouplens.grapht.reflect.InjectionPoint;

import javax.annotation.Nullable;
import java.lang.reflect.Member;

/**
 * Thrown when there is an error injecting a component.  This is can be the result of an error
 * instantiating the object, one of its dependencies, or a run-time incompatibility (e.g. a null
 * dependency for a non-nullable injection point; see {@link NullComponentException}).
 *
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 */
public class InjectionException extends Exception {
    private static final long serialVersionUID = 1L;

    private final Class<?> type;
    private final Member target;
    private final InjectionPoint injectionPoint;

    public InjectionException(InjectionPoint ip, String message) {
        this(ip, message, null);
    }

    public InjectionException(InjectionPoint ip, Throwable cause) {
        this(ip, defaultMessage(ip, null, null), cause);
    }

    public InjectionException(InjectionPoint ip, String message, Throwable cause) {
        super(message, cause);
        target = ip.getMember();
        type = target.getDeclaringClass();
        injectionPoint = ip;
    }


    private static String defaultMessage(InjectionPoint ip, Class<?> type, @Nullable Member target) {
        if (ip != null) {
            return String.format("Error injecting into %s", ip);
        } else if (target != null) {
            return String.format("Error injecting into %s for %s", target, type);
        } else {
            return String.format("Error injecting %s", type);
        }
    }

    public InjectionException(String msg, Throwable cause) {
        super(msg, cause);
        type = null;
        target = null;
        injectionPoint = null;
    }

    public InjectionException(Class<?> type, @Nullable Member target) {
        this(type, target, defaultMessage(null, type, target));
    }

    public InjectionException(Class<?> type, @Nullable Member target, String message) {
        this(type, target, message, null);
    }

    public InjectionException(Class<?> type, @Nullable Member target, Throwable cause) {
        this(type, target, defaultMessage(null, type, target), cause);
    }

    public InjectionException(Class<?> type, @Nullable Member target, String message, Throwable cause) {
        super(message, cause);
        this.type = type;
        this.target = target;
        injectionPoint = null;
    }

    public InjectionException(Member target, String message, Throwable cause) {
        this(target.getDeclaringClass(), target, message, cause);
    }

    /**
     * @return The Class type that could not be instantiated, or configured by
     *         injection
     */
    public Class<?> getType() {
        return type;
    }
    
    /**
     * @return The Member that is the target of injection, or null if the
     *         failure had no injection point associated with it
     */
    @Nullable
    public Member getTarget() {
        return target;
    }

    @Nullable
    public InjectionPoint getInjectionPoint() {
        return injectionPoint;
    }
}
