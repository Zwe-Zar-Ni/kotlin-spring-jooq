import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// Reads the .env file from the project root so build-time tasks (like jOOQ codegen)
// can reuse the exact same database credentials/config as the running application.
fun loadDotEnv(): Map<String, String> {
	val file = rootProject.file(".env")
	if (!file.exists()) return emptyMap()
	return file.readLines()
		.map { it.trim() }
		.filter { it.isNotEmpty() && !it.startsWith("#") }
		.associate { line ->
			val (key, value) = line.split("=", limit = 2)
			key to value
		}
}

val dotEnv = loadDotEnv()

// Resolves a value from the real environment first, falling back to .env.
fun envVar(name: String): String =
	System.getenv(name) ?: dotEnv[name] ?: error("Missing environment variable: $name")

plugins {
	kotlin("jvm") version "2.3.21"                              // Provides the Kotlin compiler & Gradle tasks for Kotlin sources
	kotlin("plugin.spring") version "2.3.21"                    // Makes Spring annotations open by default (Kotlin classes are final otherwise)
	id("org.springframework.boot") version "4.1.1"              // Adds bootRun, bootJar, and Spring Boot dependency management / packaging
	id("io.spring.dependency-management") version "1.1.7"       // Lets us use Spring-managed BOMs so versions stay aligned with Spring Boot
	id("org.jooq.jooq-codegen-gradle") version "3.21.7"         // Adds the jooqCodegen task that generates code from the database schema
}

group = "com.vaddshah"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)             // Compile and run with a Java 21 toolchain
	}
}

repositories {
	mavenCentral()                                               // Pull all dependencies from Maven Central
}

dependencies {
	// ? Web layer: builds REST endpoints and the HTTP server
	implementation("org.springframework.boot:spring-boot-starter-webmvc")

	// ? Dev only: auto-restart & live reload; NOT packaged into the production jar
	developmentOnly("org.springframework.boot:spring-boot-devtools")

	// ? Web test support: MockMvc, @WebMvcTest, etc.; available only to the test classpath
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")

	// ? Kotlin assertion library that plugs into the JUnit 5 test runner
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")

	// ? JUnit Platform runner; required at test runtime to actually execute JUnit 5 tests
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

	// ? Kotlin reflection (KClass, KFunction); needed by Spring/Kotlin integration
	implementation("org.jetbrains.kotlin:kotlin-reflect")

	// ? Lets Jackson (de)serialize Kotlin data classes without no-arg constructors
	implementation("tools.jackson.module:jackson-module-kotlin")

	// ? jOOQ autoconfiguration: exposes a DSLContext bean wired into Spring transactions
	implementation("org.springframework.boot:spring-boot-starter-jooq")

	// ? Database migrations: runs Flyway scripts at application startup
	implementation("org.springframework.boot:spring-boot-starter-flyway")

	// ? Kotlin-specific jOOQ extensions (nested DSL, coroutines, etc.)
	implementation("org.jooq:jooq-kotlin")

	// ? MySQL dialect support for Flyway; runtime only
	runtimeOnly("org.flywaydb:flyway-mysql")

	// ? MySQL JDBC driver; runtime only
	runtimeOnly("com.mysql:mysql-connector-j")

	// ? Same JDBC driver, but attached to the jOOQ codegen task so it can introspect the schema
	jooqCodegen("com.mysql:mysql-connector-j")

	// ? Bean Validation (jakarta.validation): validates @RequestBody DTOs with annotations
	implementation("org.springframework.boot:spring-boot-starter-validation")

	// ? Loads .env into Spring's Environment at startup; dev only, excluded from the packaged jar
	developmentOnly("me.paulschwarz:springboot4-dotenv:5.1.0")

	// ? Spring Security
	implementation("org.springframework.boot:spring-boot-starter-security")

	// ? JWT (Java Web Token) for stateless authentication
	implementation("io.jsonwebtoken:jjwt-api:0.12.6")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")
}

jooq {
	configuration {
		jdbc {
			// DB connection used by codegen to read the schema metadata
			driver = envVar("SPRING_DATASOURCE_DRIVER_CLASS_NAME")
			url = envVar("SPRING_DATASOURCE_URL")
			user = envVar("SPRING_DATASOURCE_USERNAME")
			password = envVar("SPRING_DATASOURCE_PASSWORD")
		}
		generator {
			name = "org.jooq.codegen.KotlinGenerator"            // Generate Kotlin sources instead of Java
			database {
				name = "org.jooq.meta.mysql.MySQLDatabase"        // Read metadata from a MySQL database
				inputSchema = "ktjooq"                            // Only generate code for this schema
				excludes = "flyway_schema_history"                // Skip Flyway's internal bookkeeping table
			}
			generate {
				javaTimeTypes = true                              // Use java.time types (LocalDateTime) instead of java.sql types
				records = true                                    // Generate DAO-style record classes (one per table)
				pojos = true                                      // Generate plain POJO/immutable DTO classes
			}
			target {
				packageName = "com.vaddshah.ktjooq.generated"     // Base package for all generated classes
				directory = "build/generated-src/jooq"            // Output folder for the generated sources
			}
		}
	}
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
	}
}

sourceSets {
	main {
		kotlin.srcDir("build/generated-src/jooq")                // Compile the jOOQ-generated sources as part of main sources
	}
}

tasks.withType<KotlinCompile> {
	dependsOn(tasks.named("jooqCodegen"))                        // Generate jOOQ sources before compiling Kotlin
}

tasks.withType<Test> {
	useJUnitPlatform()                                           // Use JUnit 5 (JUnit Platform) for running tests
}