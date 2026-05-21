package com.yomu.core.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yomu.core.dto.GoogleAuthRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class GoogleTokenValidator {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String googleClientId;

    public GoogleTokenValidator(ObjectMapper objectMapper,
                                @Value("${GOOGLE_CLIENT_ID:}") String googleClientId) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = objectMapper;
        this.googleClientId = googleClientId;
    }

    public void validate(GoogleAuthRequest request) {
        if (!hasText(request.getEmail()) || !hasText(request.getGoogleId())) {
            throw new RuntimeException("Google email and subject are required");
        }
        if (!hasText(googleClientId)) {
            return;
        }
        if (!hasText(request.getIdToken())) {
            throw new RuntimeException("Missing Google ID token");
        }

        try {
            String encodedToken = URLEncoder.encode(request.getIdToken(), StandardCharsets.UTF_8);
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create("https://oauth2.googleapis.com/tokeninfo?id_token=" + encodedToken))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new RuntimeException("Invalid Google ID token");
            }

            Map<String, Object> tokenInfo = objectMapper.readValue(response.body(), new TypeReference<>() {});
            if (!googleClientId.equals(tokenInfo.get("aud"))) {
                throw new RuntimeException("Google token audience mismatch");
            }
            if (!request.getEmail().equals(tokenInfo.get("email"))) {
                throw new RuntimeException("Google token email mismatch");
            }
            if (hasText(request.getGoogleId()) && !request.getGoogleId().equals(tokenInfo.get("sub"))) {
                throw new RuntimeException("Google token subject mismatch");
            }
            if (!"true".equals(String.valueOf(tokenInfo.get("email_verified")))) {
                throw new RuntimeException("Google email is not verified");
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Unable to verify Google ID token");
        }
    }

    private boolean hasText(String str) {
        return str != null && !str.trim().isEmpty();
    }
}