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


    public static String getVerificationJson(String gRecaptchaResponse) {
        try {
            URL url = new URL("https://www.google.com/recaptcha/api/siteverify");
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);

            // USE YOUR NEW V2 SECRET KEY HERE
            String postParams = "secret=6LciQIMsAAAAALrh8ieVfTMnf3OtLL-cuv8fo7PC&response=" + gRecaptchaResponse;

            OutputStream os = con.getOutputStream();
            os.write(postParams.getBytes());
            os.flush();
            os.close();

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString(); // Returns the full JSON for logging
        } catch (Exception e) {
            return "{\"success\": false, \"error-codes\": [\"exception-caught\"]}";
        }
    }

}
