package com.google.appengine.demos.courteline;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URL;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import javax.net.ssl.HttpsURLConnection;

public class VerifyRecaptchaNew {

    // Same API Key and Project ID as the first page
    private static final String API_KEY = "AIzaSyCCqsM94oAX_ZcdjDm5o1UgewlS2W7sgaU";
    private static final String PROJECT_ID = "courteline-nantes";

    // This MUST be the V2 Invisible Site Key
    private static final String V2_SITE_KEY = "6LciQIMsAAAAALrh8ieVfTMnf3OtLL-cuv8fo7PC";

    public static boolean verify(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }

        try {
            String apiUrl = String.format(
                    "https://recaptchaenterprise.googleapis.com/v1/projects/%s/assessments?key=%s",
                    PROJECT_ID, API_KEY);

            URL url = new URL(apiUrl);
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            con.setDoOutput(true);

            // Enterprise payload
            String jsonInputString = String.format(
                    "{\"event\": {\"token\": \"%s\", \"siteKey\": \"%s\"}}",
                    token, V2_SITE_KEY);

            try (OutputStream os = con.getOutputStream()) {
                os.write(jsonInputString.getBytes("utf-8"));
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line.trim());
            }

            String result = response.toString();
            System.out.println("v2 Enterprise Result: " + result);

            // For v2 Invisible Enterprise, "valid" indicates if the challenge was passed
            return result.contains("\"valid\": true");

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
