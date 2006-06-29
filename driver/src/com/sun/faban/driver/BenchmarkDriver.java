/* The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://www.sun.com/cddl/cddl.html or
 * install_dir/legal/LICENSE
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at faban/src/legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * $Id: BenchmarkDriver.java,v 1.1 2006/06/29 18:51:32 akara Exp $
 *
 * Copyright 2005 Sun Microsystems Inc. All Rights Reserved
 */
package com.sun.faban.driver;

import java.lang.annotation.*;

/**
 * This annotation interface describes the parameters
 * required when defining a benchmark driver.
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface BenchmarkDriver {

    /** The driver's name, can be ignored for a single-driver benchmark. */
    String name()           default "";

    /** The default metric is "ops/sec". Otherwise specify. */
    String metric()         default "ops/sec";

    /** Unit of operation, in plural. */
    String opsUnit()        default "operations";

    /**
     * The number of threads this driver should launch for each
     * benchmark scale.
     */
    int threadPerScale()    default 1;

    /**
     * The maximum run time of the benchmark, in hours. Inherits/overrides
     * the values defined in the @BenchmarkDefinition annotation.
     * Defaults to 6 hrs. This is only used if runControl is CYCLES.
     * Otherwise the benchmark configuration is a better indicator.
     */
    int maxRunTime()        default -1;

    /**
     * Frequency of throughput statistics collection, in seconds.
     * Inherits/overrides the values defined in the @BenchmarkDefition
     * annotation. Defaults to 30 seconds.
     */
    int thruputFrequency()  default -1;
}
