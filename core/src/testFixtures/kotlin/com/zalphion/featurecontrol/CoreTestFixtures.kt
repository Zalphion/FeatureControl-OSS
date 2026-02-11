package com.zalphion.featurecontrol

import com.zalphion.featurecontrol.configs.Property
import com.zalphion.featurecontrol.configs.PropertyKey
import com.zalphion.featurecontrol.configs.PropertyType
import com.zalphion.featurecontrol.features.EnvironmentName
import com.zalphion.featurecontrol.features.FeatureEnvironment
import com.zalphion.featurecontrol.features.FeatureKey
import com.zalphion.featurecontrol.features.FeatureUpdateData
import com.zalphion.featurecontrol.features.SubjectId
import com.zalphion.featurecontrol.features.Variant
import com.zalphion.featurecontrol.features.Weight
import com.zalphion.featurecontrol.lib.Colour
import com.zalphion.featurecontrol.lib.Update
import com.zalphion.featurecontrol.applications.Environment
import com.zalphion.featurecontrol.applications.AppName
import com.zalphion.featurecontrol.users.EmailAddress

val appName1 = AppName.of("app1")
val appName2 = AppName.of("app2")
val appName3 = AppName.of("app3")

val featureKey1 = FeatureKey.of("feature1")
val featureKey2 = FeatureKey.of("feature2")
val featureKey3 = FeatureKey.of("feature3")

val old = Variant.of("old")
val new = Variant.of("new")

val off = Variant.of("off")
val on = Variant.of("on")

val subject1 = SubjectId.of("user1")
val subject2 = SubjectId.of("user-second")
val testSubject = SubjectId.of("testuser")

val devName = EnvironmentName.of("dev")
val stagingName = EnvironmentName.of("staging")
val prodName = EnvironmentName.of("prod")

val dev = Environment(
    name = devName,
    description = "always broken",
    colour = Colour.parse("#000000"),
    extensions = emptyMap()
)

val staging = Environment(
    name = stagingName,
    description = "never used",
    colour = Colour.parse("#AAAAAA"),
    extensions = emptyMap()
)

val prod = Environment(
    name = prodName,
    description = "stack of cards",
    colour = Colour.parse("#FFFFFF"),
    extensions = emptyMap()
)

val mostlyOld = FeatureEnvironment(
    weights = mapOf(
        old to Weight.of(2),
        new to Weight.of(1)
    ),
    overrides = mapOf(
        testSubject to new
    ),
    extensions = emptyMap()
)

val mostlyNew = FeatureEnvironment(
    weights = mapOf(
        old to Weight.of(1),
        new to Weight.of(2)
    ),
    overrides = emptyMap(),
    extensions = emptyMap()
)

val mostlyOff = FeatureEnvironment(
    weights = mapOf(
        off to Weight.of(2),
        on to Weight.of(1)
    ),
    overrides = mapOf(
        testSubject to on
    ),
    extensions = emptyMap()
)

val alwaysOn = FeatureEnvironment(
    weights = mapOf(
        off to Weight.of(0),
        on to Weight.of(1)
    ),
    overrides = emptyMap(),
    extensions = emptyMap()
)

const val IDP1 = "idp1.com"
val idp1Email1 = EmailAddress.of("user1@$IDP1")
val idp1Email2 = EmailAddress.of("user2@$IDP1")
val idp1Email3 = EmailAddress.of("user3@$IDP1")
val idp1Email4 = EmailAddress.of("user4@$IDP1")

const val IDP2 = "idp2.com"
val idp2Email1 = EmailAddress.of("user1@$IDP2")

val oldNewData = FeatureUpdateData(
    defaultVariant = Update(old),
    variants = Update(mapOf(old to "old", new to "new")),
    environmentsToUpdate = Update(mapOf(
        devName to mostlyNew,
        prodName to mostlyOld
    )),
    description = Update("old or new"),
    extensions = null
)

val onOffData = FeatureUpdateData(
    defaultVariant = Update(off),
    variants = Update(mapOf(off to "off", on to "on")),
    environmentsToUpdate = Update(mapOf(
        devName to alwaysOn,
        prodName to mostlyOff
    )),
    description = Update("on or off"),
    extensions = null
)

val strProperty = PropertyKey.parse("str") to Property(
    description = "a string property",
    type = PropertyType.String
)

val numberProperty = PropertyKey.parse("num") to Property(
    description = "a number property",
    type = PropertyType.Number
)

val booleanProperty = PropertyKey.parse("bool") to Property(
    description = "a boolean property",
    type = PropertyType.Boolean
)

val secretProperty = PropertyKey.parse("secret") to Property(
    description = "a secret property",
    type = PropertyType.Secret
)