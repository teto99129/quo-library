
buildscript {
	repositories {
		mavenCentral()
	}
	dependencies {
		classpath("org.postgresql:postgresql:42.7.3")
		classpath("org.flywaydb:flyway-database-postgresql:12.11.0")
	}
}

plugins {
	kotlin("jvm") version "2.3.21"
	kotlin("plugin.spring") version "2.3.21"
	id("org.springframework.boot") version "4.1.0"
	id("io.spring.dependency-management") version "1.1.7"
	id("org.flywaydb.flyway") version "12.11.0"
	id("org.jooq.jooq-codegen-gradle") version "3.21.6"
	id("org.jlleitschuh.gradle.ktlint") version "14.2.0"
	id("io.gitlab.arturbosch.detekt") version "1.23.8"
}

group = "com.github.teto99129"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-jooq")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.flywaydb:flyway-core")
	implementation("tools.jackson.module:jackson-module-kotlin:3.2.0")
	runtimeOnly("org.postgresql:postgresql")
	runtimeOnly("org.flywaydb:flyway-database-postgresql")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	testImplementation("org.springframework.boot:spring-boot-starter-jooq-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testImplementation("io.kotest:kotest-runner-junit5:5.9.1")
	testImplementation("io.kotest:kotest-assertions-core:5.9.1")
	testImplementation("io.kotest.extensions:kotest-extensions-spring:1.3.0")
	testImplementation("io.mockk:mockk:1.13.12")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	jooqCodegen("org.postgresql:postgresql:42.7.13")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
	}
	sourceSets {
		main {
			kotlin.srcDir("build/generated-sources/jooq")
		}
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.named("jooqCodegen") {
	dependsOn("flywayMigrate")
}

tasks.named("compileKotlin") {
	dependsOn("jooqCodegen")
}

val dbUrl = System.getenv("SPRING_DATASOURCE_URL") ?: "jdbc:postgresql://host.docker.internal:5432/mydb"
val dbUser = System.getenv("SPRING_DATASOURCE_USERNAME") ?: "user"
val dbPassword = System.getenv("SPRING_DATASOURCE_PASSWORD") ?: "postgres"

flyway {
	url = dbUrl
	user = dbUser
	password = dbPassword
}

jooq {
	configuration {
		jdbc {
			driver = "org.postgresql.Driver"
			url = dbUrl
			user = dbUser
			password = dbPassword
		}
		generator {
			name = "org.jooq.codegen.KotlinGenerator"
			database {
				name = "org.jooq.meta.postgres.PostgresDatabase"
				includes = ".*"
				inputSchema = "public"
			}
			generate {}
			target {
				packageName = "com.github.teto99129.library.jooq"
				directory = "build/generated-sources/jooq"
			}
		}
	}
}

ktlint {
	filter {
		exclude { it.file.path.contains("build/generated-sources") }
	}
}

detekt {
	toolVersion = "1.23.8"
	buildUponDefaultConfig = true
	source.setFrom(files("src/main/kotlin", "src/test/kotlin"))
}

configurations.matching { it.name.startsWith("detekt") }.configureEach {
	resolutionStrategy.eachDependency {
		if (requested.group == "org.jetbrains.kotlin") {
			useVersion("2.0.21")
		}
	}
}

tasks.named("runKtlintCheckOverMainSourceSet") {
	dependsOn("jooqCodegen")
}

tasks.named("runKtlintFormatOverMainSourceSet") {
	dependsOn("jooqCodegen")
}
