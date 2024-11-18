/*
 * Copyright © 2023 Gleb Minaev
 * All rights reserved. Licensed under the Apache License, Version 2.0. See the license in file LICENSE
 */

import dev.lounres.gradle.stal.dsl.StalSettingsDsl
import org.gradle.api.initialization.Settings
import org.gradle.kotlin.dsl.getByName


public val Settings.stal: StalSettingsDsl get() = extensions.getByName<StalSettingsDsl>("stal")
public fun Settings.stal(block: StalSettingsDsl.() -> Unit): Unit = extensions.configure("stal", block)
