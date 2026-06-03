package com.ecommerce.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class JwtAuthFilterTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void protectedRoute_withoutToken_returns401() {
        webTestClient.get()
                .uri("/api/orders/my")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void protectedRoute_withInvalidToken_returns401() {
        webTestClient.get()
                .uri("/api/orders/my")
                .header("Authorization", "Bearer invalid.token.here")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void publicRoute_withoutToken_isNotBlocked() {
        // Auth register is public — gateway should forward (downstream may be unavailable in test,
        // but we should NOT get 401 from the gateway itself)
        webTestClient.post()
                .uri("/api/auth/register")
                .exchange()
                .expectStatus().value(status -> {
                    // Any status except 401 means gateway passed it through
                    assert status != 401 : "Expected gateway to pass through public route, got 401";
                });
    }
}
