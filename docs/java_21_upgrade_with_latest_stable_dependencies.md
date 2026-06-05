# Java 21 Upgrade & Dependency Update Playbook

## Overview

This playbook documents the upgrade of the Spring PetClinic Microservices project from **Java 17 to Java 21** and the update of all project dependencies to their latest stable versions.

---

## 1. Java Version Upgrade

### Change

| File | Property | Old Value | New Value |
|------|----------|-----------|-----------|
| `pom.xml` (parent) | `java.version` | `17` | `21` |
| `pom.xml` (parent) | `maven-enforcer-plugin` message | Java 17 | Java 21 |
| `docker/Dockerfile` | `eclipse-temurin` base image | `eclipse-temurin:17` | `eclipse-temurin:21` |
| `README.md` | Docker base image reference | Java 17 | Java 21 |

### Steps Taken

1. Updated `<java.version>` property in the parent `pom.xml` from `17` to `21`.
2. Updated the `maven-enforcer-plugin` message to reference Java 21.
3. Updated the Docker base image from `eclipse-temurin:17` to `eclipse-temurin:21` in both builder and runtime stages.
4. Updated the `README.md` documentation to reflect the new Java version.

### Breaking Changes Encountered

**None.** The codebase is fully compatible with Java 21. All modules compiled and all tests passed without any source code modifications.

---

## 2. Dependency Version Upgrades

### Parent POM (`pom.xml`)

| Dependency | Old Version | New Version | Notes |
|------------|-------------|-------------|-------|
| `chaos-monkey-spring-boot` | 3.1.0 | 3.1.2 | Bug fixes and minor improvements |
| `jolokia-core` | 1.7.1 | 1.7.2 | Bug fixes |
| `exec-maven-plugin` | 3.1.1 | 3.5.0 | Performance and compatibility improvements |

### API Gateway (`spring-petclinic-api-gateway/pom.xml`)

| Dependency | Old Version | New Version | Notes |
|------------|-------------|-------------|-------|
| `webjars-bootstrap` | 5.3.3 | 5.3.7 | Latest Bootstrap 5.3.x stable release |
| `webjars-marked` | 14.1.2 | 15.0.12 | Major version bump; no breaking API changes for webjar usage |
| `squareup-okhttp3` | 5.0.0-alpha.14 | 5.0.0-alpha.16 | Latest available alpha (test dependency only) |
| `libsass-maven-plugin` | 0.2.29 | 0.3.3 | Latest stable release |

### Dependencies Kept at Current Version

| Dependency | Current Version | Reason |
|------------|----------------|--------|
| `spring-boot-starter-parent` | 4.0.1 | Already at latest available version |
| `spring-cloud-dependencies` | 2025.1.0 | Already at latest available version |
| `spring-ai-bom` | 2.0.0-M1 | Latest milestone for Spring AI; GA 1.0.0 is an older release train |
| `spring-boot-admin` | 3.4.1 | Already at latest GA version |
| `datasource-micrometer-spring-boot` | 2.0.1 | Already at latest available version |
| `webjars-font-awesome` | 4.7.0 | Already at latest version (Font Awesome 4.x EOL) |
| `webjars-angularjs` | 1.8.3 | Already at latest stable 1.x (AngularJS is EOL) |
| `webjars-angular-ui-router` | 1.0.30 | Already at latest version |

---

## 3. Verification

### Build Verification

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH
./mvnw clean install
```

### Results

All 8 microservices build successfully with Java 21:

| Module | Status |
|--------|--------|
| `spring-petclinic-microservices` (parent) | SUCCESS |
| `spring-petclinic-admin-server` | SUCCESS |
| `spring-petclinic-customers-service` | SUCCESS |
| `spring-petclinic-vets-service` | SUCCESS |
| `spring-petclinic-visits-service` | SUCCESS |
| `spring-petclinic-genai-service` | SUCCESS |
| `spring-petclinic-config-server` | SUCCESS |
| `spring-petclinic-discovery-server` | SUCCESS |
| `spring-petclinic-api-gateway` | SUCCESS |

### Test Results

All existing tests pass without modification.

---

## 4. Files Modified

| File | Changes |
|------|---------|
| `pom.xml` | Java 21, dependency version bumps, enforcer message |
| `spring-petclinic-api-gateway/pom.xml` | Bootstrap, Marked, OkHttp3, libsass plugin version bumps |
| `docker/Dockerfile` | Base image updated to `eclipse-temurin:21` |
| `README.md` | Documentation updated for Java 21 |

---

## 5. Rollback Procedure

To revert to Java 17:

1. Set `<java.version>17</java.version>` in the parent `pom.xml`.
2. Revert `docker/Dockerfile` to use `eclipse-temurin:17`.
3. Revert dependency versions in all `pom.xml` files.
4. Run `./mvnw clean install` to verify the rollback.
