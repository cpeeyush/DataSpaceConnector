/*
 *  Copyright (c) 2022 Amadeus
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Amadeus - initial API and implementation
 *
 */

plugins {
    `java-library`
    id("io.swagger.core.v3.swagger-gradle-plugin")
}

val rsApi: String by project
val nimbusVersion: String by project
val bouncycastleVersion: String by project
val jerseyVersion: String by project

dependencies {
    api(project(":spi:common:core-spi"))
    api(project(":spi:control-plane:contract-spi"))
    api(project(":spi:control-plane:transfer-spi"))
    api(project(":spi:common:web-spi"))

    api(project(":spi:control-plane:data-plane-transfer-spi"))
    api(project(":spi:data-plane:data-plane-spi"))
    api(project(":spi:data-plane-selector:selector-spi"))

    implementation(project(":common:token-validation-lib"))
    implementation(project(":common:token-generation-lib"))

    api("jakarta.ws.rs:jakarta.ws.rs-api:${rsApi}")
    api("com.nimbusds:nimbus-jose-jwt:${nimbusVersion}")
    // Note: nimbus requires bouncycastle as mentioned in documentation:
    // https://www.javadoc.io/doc/com.nimbusds/nimbus-jose-jwt/7.2.1/com/nimbusds/jose/jwk/JWK.html#parseFromPEMEncodedObjects-java.lang.String-
    api("org.bouncycastle:bcpkix-jdk15on:${bouncycastleVersion}")

    testImplementation("org.glassfish.jersey.media:jersey-media-multipart:${jerseyVersion}")
}


publishing {
    publications {
        create<MavenPublication>("data-plane-transfer-sync") {
            artifactId = "data-plane-transfer-sync"
            from(components["java"])
        }
    }
}
