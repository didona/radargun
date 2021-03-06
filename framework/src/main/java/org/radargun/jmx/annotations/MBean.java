/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009 Red Hat Inc. and/or its affiliates and other
 * contributors as indicated by the @author tags. All rights reserved.
 * See the copyright.txt in the distribution for a full listing of
 * individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.radargun.jmx.annotations;

import java.lang.annotation.*;

/**
 * Classes annotated with this will be exposed as MBeans. If you are looking for more fined grained way of exposing JMX
 * attributes/operations, take a look at {@link org.radargun.jmx.annotations.ManagedAttribute} and {@link
 * org.radargun.jmx.annotations.ManagedOperation}
 * <p/>
 * Note: copy from Infinispan
 *
 * @author Mircea.Markus@jboss.com
 * @author Pedro Ruivo
 * @since 1.1
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
public @interface MBean {

   String objectName() default "";

   String description() default "";
}
