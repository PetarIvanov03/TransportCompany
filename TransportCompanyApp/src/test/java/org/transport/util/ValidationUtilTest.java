package org.transport.util;

import org.junit.jupiter.api.Test;
import org.transport.entity.Client;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ValidationUtilTest {

    @Test
    void validate_blankName_throwsException() {
        Client client = new Client();
        client.setName("");

        assertThrows(IllegalArgumentException.class,
                () -> ValidationUtil.validate(client));
    }
}