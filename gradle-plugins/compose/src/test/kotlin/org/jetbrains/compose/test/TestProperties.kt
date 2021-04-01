/*
 * Copyright 2020-2021 JetBrains s.r.o. and respective authors and developers.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE.txt file.
 */

package org.jetbrains.compose.test

object TestProperties {
    val composeVersion: String
        get() = System.getProperty("compose.plugin.version")!!

    val gradleVersionForTests: String
        get() = System.getProperty("gradle.version.for.tests")!!
}
