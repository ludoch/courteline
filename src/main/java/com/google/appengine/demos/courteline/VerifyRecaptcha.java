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

    public static boolean verify(String token) {
        if (token == null || token.isEmpty()) return false;

        try {
            // Enterprise URL format: projects/{project_id}/assessments
            String apiUrl = String.format(
                "https://recaptchaenterprise.googleapis.com/v1/projects/%s/assessments?key=%s",
                PROJECT_ID, API_KEY);

            URL url = new URL(apiUrl);
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            con.setDoOutput(true);

            // JSON body required for Enterprise Assessments
            String jsonInputString = String.format(
                "{\"event\": {\"token\": \"%s\", \"siteKey\": \"%s\"}}",
                token, SITE_KEY);

            try (OutputStream os = con.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = in.readLine()) != null) {
                response.append(responseLine.trim());
            }

            String result = response.toString();
            System.out.println("Enterprise Verification Result: " + result);

            // For v3 Enterprise, success means "tokenProperties.valid": true
            // and you can also check "riskAnalysis.score" (0.0 to 1.0)
            return result.contains("\"valid\": true") && result.contains("\"score\":");

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
