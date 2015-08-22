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

import org.grouplens.grapht.annotation.DefaultImplementation;
import org.grouplens.grapht.annotation.DefaultProvider;
import org.grouplens.grapht.solver.UnresolvableDependencyException;
import org.junit.Test;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class SkipIfUnusableTest {
    /**
     * A skippable default with satisfied dependencies should be injected.
     */
    @Test
    public void testSatisfyImplementation() throws InjectionException {
        InjectorBuilder bld = InjectorBuilder.create();
        bld.bind(Inner.class).to(InnerObj.class);
        Injector inj = bld.build();
        IfaceWithSkippableDefault obj = inj.getInstance(IfaceWithSkippableDefault.class);
        assertThat(obj, instanceOf(DftImpl.class));
    }

    /**
     * A skippable default provider with satisfied dependencies should be injected.
     */
    @Test
    public void testSatisfyProvider() throws InjectionException {
        InjectorBuilder bld = InjectorBuilder.create();
        bld.bind(Inner.class).to(InnerObj.class);
        Injector inj = bld.build();
        IfaceWithSkippableDefaultProvider obj = inj.getInstance(IfaceWithSkippableDefaultProvider.class);
        assertThat(obj, instanceOf(ProvidedImpl.class));
    }

    /**
     * A skippable default without satisfied dependencies should not be injected.
     */
    @Test
    public void testSkipImplementation() {
        InjectorBuilder bld = InjectorBuilder.create();
        Injector inj = bld.build();
        try {
            IfaceWithSkippableDefault obj = inj.tryGetInstance(null, IfaceWithSkippableDefault.class);
            assertThat(obj, nullValue());
        } catch (InjectionException ex) {
            fail("injection of skipped object should succeed with null object");
        }
    }

    /**
     * A skippable default provider without satisfied dependencies should not be injected.
     */
    @Test
    public void testSkipProvider() {
        InjectorBuilder bld = InjectorBuilder.create();
        Injector inj = bld.build();
        try {
            IfaceWithSkippableDefaultProvider obj = inj.tryGetInstance(null, IfaceWithSkippableDefaultProvider.class);
            assertThat(obj, nullValue());
        } catch (InjectionException e) {
            fail("injection of skipped provider should succeed with null object");
        }
    }

    /**
     * A skippable default with a satisfied dependency that itself has an unsatisfied dependency should fail.
     */
    @Test
    public void testFailWithUnsatisfiedTransitiveDep() {
        InjectorBuilder bld = InjectorBuilder.create();
        bld.bind(Inner.class).to(InnerWithDep.class);
        Injector inj = bld.build();
        try {
            inj.tryGetInstance(null, IfaceWithSkippableDefault.class);
            fail("injecting a skippable default with a satisfied but instantiable dep should fail");
        } catch (InjectionException ex) {
            assertThat(ex, instanceOf(UnresolvableDependencyException.class));
        }
    }

    /**
     * Injection of component that depends on a skipped default should fail.
     */
    @Test
    public void testFailWithUnusableDefaultForDep() {
        InjectorBuilder bld = InjectorBuilder.create();
        Injector inj = bld.build();
        try {
            inj.getInstance(null, DefaultRequirer.class);
            fail("injection of dep on skipped default should fail");
        } catch (InjectionException ex) {
            assertThat(ex, instanceOf(UnresolvableDependencyException.class));
        }
    }

    /**
     * Injection of component that optionally uses a skipped default should succeed.
     */
    @Test
    public void testAcceptSkippedOptionalDep() {
        InjectorBuilder bld = InjectorBuilder.create();
        Injector inj = bld.build();
        try {
            OptionalDefaultRequirer obj = inj.getInstance(OptionalDefaultRequirer.class);
            assertThat(obj, notNullValue());
        } catch (InjectionException ex) {
            fail("injection of component with optional dep on skipped default should succeed");
        }
    }

    /**
     * Injection of component that optionally uses a non-skipped but transitively non-instantiable default should fail.
     */
    @Test
    public void testFailOptionalDepHasUnmetDep() {
        InjectorBuilder bld = InjectorBuilder.create();
        bld.bind(Inner.class).to(InnerWithDep.class);
        Injector inj = bld.build();
        try {
            OptionalDefaultRequirer obj = inj.getInstance(OptionalDefaultRequirer.class);
            fail("injection of component with optional dep on non-skipped but uninstantiable default should fail");
        } catch (InjectionException ex) {
            assertThat(ex, instanceOf(UnresolvableDependencyException.class));
        }
    }

    /**
     * Skippable defaults dependend on by skippable defaults should cleanly be skipped.
     */
    @Test
    public void testNestedSkipping() throws InjectionException {
        InjectorBuilder bld = InjectorBuilder.create();
        Injector inj = bld.build();
        Outer obj = inj.tryGetInstance(null, Outer.class);
        assertThat(obj, nullValue());
    }

    /**
     * Interface for dependencies.
     */
    interface Inner {
    }

    /**
     * Implementation of dependency interface.
     */
    static class InnerObj implements Inner {
        @Inject
        public InnerObj() {}
    }

    /**
     * Implementation of dependency with a dependency.
     */
    static class InnerWithDep implements Inner {
        private final String message;

        @Inject
        public InnerWithDep(String msg) {
            message = msg;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * Interface with a default that is skippable.
     */
    @DefaultImplementation(value=DftImpl.class, skipIfUnusable = true)
    interface IfaceWithSkippableDefault {
        Inner getInner();
    }

    /**
     * Default implementation of the skippable interface.
     */
    static class DftImpl implements IfaceWithSkippableDefault {
        private final Inner inner;

        @Inject
        public DftImpl(Inner in) {
            inner = in;
        }

        @Override
        public Inner getInner() {
            return inner;
        }
    }

    /**
     * Interface with a default provider that is skippable.
     */
    @DefaultProvider(value=DftProvider.class, skipIfUnusable = true)
    interface IfaceWithSkippableDefaultProvider {
        Inner getInner();
    }

    /**
     * Default provider for the skippable interface.
     */
    static class DftProvider implements Provider<IfaceWithSkippableDefaultProvider> {
        private final Inner inner;

        @Inject
        public DftProvider(Inner in) {
            inner = in;
        }

        @Override
        public IfaceWithSkippableDefaultProvider get() {
            return new ProvidedImpl(inner);
        }
    }

    static class ProvidedImpl implements IfaceWithSkippableDefaultProvider {
        private final Inner inner;

        public ProvidedImpl(Inner in) {
            inner = in;
        }

        @Override
        public Inner getInner() {
            return inner;
        }
    }

    @DefaultImplementation(value = DefaultRequirer.class, skipIfUnusable = true)
    interface Outer {}

    /**
     * A component that requires an object with one of our skippable defaults.
     */
    static class DefaultRequirer implements Outer {
        private final IfaceWithSkippableDefault dependency;

        @Inject
        public DefaultRequirer(IfaceWithSkippableDefault dep) {
            dependency = dep;
        }

        public IfaceWithSkippableDefault getDependency() {
            return dependency;
        }
    }

    /**
     * A component that optionally uses an object with one of our skippable defaults.
     */
    static class OptionalDefaultRequirer {
        private final IfaceWithSkippableDefault dependency;

        @Inject
        public OptionalDefaultRequirer(@Nullable IfaceWithSkippableDefault dep) {
            dependency = dep;
        }

        public IfaceWithSkippableDefault getDependency() {
            return dependency;
        }
    }
}
