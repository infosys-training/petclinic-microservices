package org.springframework.samples.petclinic.admin;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class SpringBootAdminApplicationTests {

    @Test
    void applicationClassShouldExist() {
        assertNotNull(SpringBootAdminApplication.class);
    }

    @Test
    void mainMethodShouldExist() throws NoSuchMethodException {
        assertNotNull(
            SpringBootAdminApplication.class.getMethod("main", String[].class)
        );
    }
}
