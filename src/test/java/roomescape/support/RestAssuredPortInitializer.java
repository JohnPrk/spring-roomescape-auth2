package roomescape.support;

import io.restassured.RestAssured;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class RestAssuredPortInitializer implements BeforeAllCallback {

    private static final int TEST_PORT = 8888;

    @Override
    public void beforeAll(ExtensionContext context) {
        RestAssured.port = TEST_PORT;
    }
}
