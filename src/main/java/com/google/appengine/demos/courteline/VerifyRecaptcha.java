package com.google.appengine.demos.courteline;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;

public class VerifyRecaptcha {

    // 1. Get this from Google Cloud Console > APIs & Services > Credentials
    private static final String API_KEY = "AIzaSyCCqsM94oAX_ZcdjDm5o1UgewlS2W7sgaU";

    // 2. Your Project ID (e.g., courteline-nantes)
    private static final String PROJECT_ID = "courteline-nantes";

    // 3. The v3 Site Key from your Enterprise Dashboard
    private static final String SITE_KEY = "6LfWLIEgAAAAADoCmpeyrxRq_81uPpyhGGqQxXTX";

    /**
     * Verifies the reCAPTCHA token using the Google Cloud Enterprise API.
     *
     * @param token The g-recaptcha-response token from the form.
     * @return true if the token is valid and verified as a human.
     */
    public static boolean verify(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }

        try {
            // Enterprise Assessment Endpoint
            String apiUrl = String.format(
                    "https://recaptchaenterprise.googleapis.com/v1/projects/%s/assessments?key=%s",
                    PROJECT_ID, API_KEY);

            URL url = new URL(apiUrl);
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            con.setDoOutput(true);

            // Constructing the JSON payload with the 'expectedAction'
            // This MUST match the action name used in your index.html JS
            String jsonInputString = String.format(
                    "{\"event\": {\"token\": \"%s\", \"siteKey\": \"%s\", \"expectedAction\": \"homepage\"}}",
                    token, SITE_KEY);

            try (OutputStream os = con.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Read the JSON response from Google
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = in.readLine()) != null) {
                response.append(responseLine.trim());
            }

            String result = response.toString();
            // Optional: Log this to see the score (0.0 to 1.0) in App Engine logs
            System.out.println("reCAPTCHA Enterprise Assessment: " + result);

            // Logic: Token must be valid.
            // For v3, you can also add score checking (e.g., result.contains("\"score\":0.9"))
            return result.contains("\"valid\": true") && result.contains("\"score\":");

        } catch (Exception e) {
            System.err.println("reCAPTCHA Verification Error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
