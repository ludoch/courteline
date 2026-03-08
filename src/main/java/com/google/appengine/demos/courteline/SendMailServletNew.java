/**
 * Copyright 2022 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */package com.google.appengine.demos.courteline;

import java.io.IOException;
import java.util.Properties;
import java.util.regex.Pattern;
import javax.mail.*;
import javax.mail.internet.*;
import javax.servlet.http.*;

public class SendMailServletNew extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/plain");
        resp.getWriter().println("Servlet is active. Use POST to send mail.");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html;charset=UTF-8");
//
//        // 1. Validate reCAPTCHA
//        String gRecaptchaResponse = req.getParameter("g-recaptcha-response");
//        if (gRecaptchaResponse == null || gRecaptchaResponse.isEmpty()) {
//             System.out.println("RE CAPTCHA IS NULL...");
//           resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Veuillez valider le reCAPTCHA.");
//            return;
//        }
//        
//        // Verify with Google and get the full JSON response
//        boolean ok = VerifyRecaptchaNew.verify(gRecaptchaResponse);
//        
//        if (!ok) {
//            System.err.println("Google reCAPTCHA Error: " );
//             System.out.println("RE CAPTCHA INVALID.so we stop.");
//           resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "reCAPTCHA invalide.");
//            return;
//        }

    

        // 2. Extract and sanitize parameters
        String prenom = getString(req.getParameter("prenom"));
        String nom = getString(req.getParameter("nom"));
        String email = getString(req.getParameter("email"));
        String telephone = getString(req.getParameter("telephone"));
        String date = getString(req.getParameter("date"));
        String nuits = getString(req.getParameter("nuits"));
        String personnes = getString(req.getParameter("personnes"));
        String chambre = getString(req.getParameter("chambre_souhaitee"));
        String message = getString(req.getParameter("message"));

        // Anti-spam check
        if (prenom.toLowerCase().startsWith("henrytug")) return;

        // 3. Construct Email Body
        StringBuilder sb = new StringBuilder();
        sb.append("Demande de réservation - Courteline\n\n");
        sb.append("Client: ").append(prenom).append(" ").append(nom).append("\n");
        sb.append("Email: ").append(email).append("\n");
        sb.append("Tel: ").append(telephone).append("\n");
        sb.append("Arrivée: ").append(date).append("\n");
        sb.append("Séjour: ").append(nuits).append(" nuit(s), ").append(personnes).append(" pers.\n");
        sb.append("Chambre: ").append(chambre).append("\n");
        sb.append("Message: \n").append(message).append("\n");

        String content = sb.toString();

        // 4. Send Email
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress("chambres.hotes.courteline@gmail.com", "Site Courteline"));
            
            // CC to owner
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress("chambres.hotes.courteline@gmail.com", "Courteline"));
            
            // BCC to developer
            msg.addRecipient(Message.RecipientType.BCC, new InternetAddress("ludovic.champenois@gmail.com", "Dev Support"));

            msg.setSubject("Demande réservation pour Courteline: " + prenom + " " + nom);
            msg.setText(content);
            Transport.send(msg);

            resp.getWriter().println("<h3>Merci " + prenom + ", votre demande a bien été envoyée.</h3>");
            resp.getWriter().println("<p>" + escapeHTML(content).replace("\n", "<br>") + "</p>");
            resp.getWriter().println("<button onclick=\"window.history.back()\">Retour</button>");

        } catch (MessagingException e) {
            resp.getWriter().println("Erreur d'envoi : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String getString(String val) {
        return (val == null) ? "" : val.trim();
    }

    public static String escapeHTML(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}