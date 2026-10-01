package org.aether.common.config;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AetherPropertiesTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void acceptsSafeDefaults() {
        AetherProperties properties = new AetherProperties();

        assertThat(validator.validate(properties)).isEmpty();
    }

    @Test
    void rejectsPageSizeOutsideConfiguredBounds() {
        AetherProperties properties = new AetherProperties();
        properties.setMaxPageSize(0);

        assertThat(validator.validate(properties))
                .extracting(constraint -> constraint.getPropertyPath().toString())
                .contains("maxPageSize");
    }
}
