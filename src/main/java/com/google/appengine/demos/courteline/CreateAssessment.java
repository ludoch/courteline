package com.google.appengine.demos.courteline;

import com.google.cloud.recaptchaenterprise.v1.RecaptchaEnterpriseServiceClient;
import com.google.recaptchaenterprise.v1.Assessment;
import com.google.recaptchaenterprise.v1.CreateAssessmentRequest;
import com.google.recaptchaenterprise.v1.Event;
import com.google.recaptchaenterprise.v1.ProjectName;
import com.google.recaptchaenterprise.v1.RiskAnalysis.ClassificationReason;
import java.io.IOException;

public class CreateAssessment {

    public static void main(String[] args) throws IOException {
        // TODO: Replace the token and reCAPTCHA action variables before running the sample.
        String projectID = "courteline-nantes";
        String recaptchaKey = "6LcYtoMsAAAAAL7dECqKcZLR4RUk4dpqDkxlgZRY";
        String token = "action-token";
        String recaptchaAction = "action-name";

        createAssessment(projectID, recaptchaKey, token, recaptchaAction);
    }

    /**
     * Create an assessment to analyze the risk of a UI action.
     *
     * @param projectID : Your Google Cloud Project ID.
     * @param recaptchaKey : The reCAPTCHA key associated with the site/app
     * @param token : The generated token obtained from the client.
     * @param recaptchaAction : Action name corresponding to the token.
     */
    public static void createAssessment(
            String projectID, String recaptchaKey, String token, String recaptchaAction)
            throws IOException {
        // Create the reCAPTCHA client.
        // TODO: Cache the client generation code (recommended) or call client.close() before exiting the method.
        try (RecaptchaEnterpriseServiceClient client = RecaptchaEnterpriseServiceClient.create()) {

            // Set the properties of the event to be tracked.
            Event event = Event.newBuilder().setSiteKey(recaptchaKey).setToken(token).build();

            // Build the assessment request.
            CreateAssessmentRequest createAssessmentRequest
                    = CreateAssessmentRequest.newBuilder()
                            .setParent(ProjectName.of(projectID).toString())
                            .setAssessment(Assessment.newBuilder().setEvent(event).build())
                            .build();

            Assessment response = client.createAssessment(createAssessmentRequest);

            // Check if the token is valid.
            if (!response.getTokenProperties().getValid()) {
                System.out.println(
                        "The CreateAssessment call failed because the token was: "
                        + response.getTokenProperties().getInvalidReason().name());
                return;
            }

            // Check if the expected action was executed.
            String actualAction = response.getTokenProperties().getAction();
            if (!actualAction.equals(recaptchaAction)) {
                System.out.println("reCAPTCHA Action Mismatch: Expected '" + recaptchaAction + "' but got '" + actualAction + "'");
                return;
            }

            // Get the risk score and the reason(s).
            // For more information on interpreting the assessment, see:
            // https://cloud.google.com/recaptcha/docs/interpret-assessment
            for (ClassificationReason reason : response.getRiskAnalysis().getReasonsList()) {
                System.out.println(reason);
            }

            float recaptchaScore = response.getRiskAnalysis().getScore();
            System.out.println("The reCAPTCHA score is: " + recaptchaScore);

            // Get the assessment name (id). Use this to annotate the assessment.
            String assessmentName = response.getName();
            System.out.println(
                    "Assessment name: " + assessmentName.substring(assessmentName.lastIndexOf("/") + 1));
        }
    }

    /**
     * Create an assessment to analyze the risk of a UI action.
     *
     * @param projectID Your Google Cloud Project ID
     * @param recaptchaKey The reCAPTCHA site key
     * @param token The token generated from the frontend
     * @param recaptchaAction The action name (e.g., "submit")
     */
    public static float calculateAssessment(
            String projectID, String recaptchaKey, String token, String recaptchaAction)
            throws IOException {

        try (RecaptchaEnterpriseServiceClient client = RecaptchaEnterpriseServiceClient.create()) {
            // Set the properties of the event to be tracked.
            Event event = Event.newBuilder()
                    .setSiteKey(recaptchaKey)
                    .setToken(token)
                    .build();

            // Build the assessment request
            CreateAssessmentRequest assessmentRequest = CreateAssessmentRequest.newBuilder()
                    .setParent(ProjectName.of(projectID).toString())
                    .setAssessment(Assessment.newBuilder().setEvent(event).build())
                    .build();

            Assessment response = client.createAssessment(assessmentRequest);

            // Check if the token is valid
            if (!response.getTokenProperties().getValid()) {
                System.out.println("The expected site key does not match the actual key: "
                        + response.getTokenProperties().getInvalidReason().name());
                return 0.0f;
            }

            // Check if the expected action was executed
            String actualAction = response.getTokenProperties().getAction();
            if (!actualAction.equals(recaptchaAction)) {
                System.out.println("reCAPTCHA Action Mismatch: Expected '" + recaptchaAction + "' but got '" + actualAction + "'");
                return 0.0f;
            }

            // Return the risk score (0.0 to 1.0)
            return response.getRiskAnalysis().getScore();
        }
    }
}
