/*
 * Copyright © 2023 Gleb Minaev
 * All rights reserved. Licensed under the Apache License, Version 2.0. See the license in file LICENSE
 */

package com.lounres.gradle.stal.extensions

import com.lounres.gradle.stal.dsl.*


internal class StalSettingsDslImpl(
    override val structure: StructureDsl,
    override val tag: TagDsl,
    override val action: ActionDsl,
): StalSettingsDsl

internal class StalRootProjectDslImpl(
    override val tag: TagDsl,
    override val action: ActionDsl,
    override val lookUp: LookUpDsl,
): StalRootProjectDsl