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
 */
package com.google.appengine.demos.courteline;

import java.io.IOException;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SendMailServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        resp.setContentType("text/plain");
        resp.getWriter().println("Hello. \n\n");

    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        resp.setContentType("text/html");
        // get reCAPTCHA request param
        String gRecaptchaResponse = req
                .getParameter("g-recaptcha-response");
        if (gRecaptchaResponse == null) {
            System.out.println("RE CAPTCHA IS NULL...");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return; // Ignore spammers
       }
        System.out.println(gRecaptchaResponse);
        if (!VerifyRecaptcha.verify(gRecaptchaResponse)) {
            System.out.println("RE CAPTCHA INVALID.so we stop.");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return; // Ignore spammers
       }

        String date = req.getParameter("date");
        String telephone = req.getParameter("telephone");
        String nuits = req.getParameter("nuits");
        String chambre_souhaitee = req.getParameter("chambre_souhaitee");
        String message = req.getParameter("message");
        String email = req.getParameter("email");
        String nom = req.getParameter("nom");
        String prenom = req.getParameter("prenom");
        String personnes = req.getParameter("personnes");
        if (prenom.startsWith("Henrytug")) {
            return; // Ignore spammers
        }
        StringBuilder contentBuilder = new StringBuilder();
        contentBuilder.append("Demande d'information via https://courteline.appspot.com\n\n");
        contentBuilder.append("chambre_souhaitee: ").append(chambre_souhaitee).append("\n");
        contentBuilder.append("date: ").append(date).append("\n");
        if (date.isEmpty()) {
            contentBuilder.append("Erreur, la date est incorrecte.").append("\n");
        }
        contentBuilder.append("email: ").append(email).append("\n");
        if (email.isEmpty()) {
            contentBuilder.append("Erreur, le mail est incorrect.").append("\n");
        }
        contentBuilder.append("nom: ").append(nom).append("\n");
        contentBuilder.append("prenom: ").append(prenom).append("\n");
        contentBuilder.append("nombre_de_personnes: ").append(personnes).append("\n");
        contentBuilder.append("nombre_de_nuits: ").append(nuits).append("\n");
        if (!isNumeric(nuits)) {
            contentBuilder.append("Erreur, le nombre de nuits est incorrect.").append("\n");
        }
        contentBuilder.append("telephone: ").append(telephone).append("\n");
        contentBuilder.append("message: ").append(message).append("\n");
        String content = contentBuilder.toString();
        System.out.println(content);

        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress("chambres.hotes.courteline@gmail.com", "Site Courteline"));
            if (!date.isEmpty()) {
                msg.addRecipient(Message.RecipientType.CC,
                        new InternetAddress("chambres.hotes.courteline@gmail.com", "Courteline"));
            }
            if (email.chars().filter(ch -> ch == '@').count() == 1) {
                msg.addRecipient(Message.RecipientType.TO,
                        new InternetAddress(email, prenom + " " + nom));
            }
            msg.addRecipient(Message.RecipientType.BCC,
                    new InternetAddress("ludovic.champenois@gmail.com", "Courteline"));
            msg.setSubject("Demande réservation pour Courteline");
            msg.setText(content);
            Transport.send(msg);
            resp.getWriter().println("Site Courteline, email reçu, merci: <br>"
                    + escapeHTML(content)
                    + "<br><button onclick=\"window.history.back()\">Retour page précédente.</button><br>");

            //    RequestDispatcher dispatcher = getServletContext()
            //            .getRequestDispatcher("https://courteline-nantes.appspot.com/");
            //    dispatcher.forward(req, resp);
        } catch (IOException | MessagingException e) {
            resp.getWriter().println("Site Courteline, email erreur. \n\n");
            e.printStackTrace();
            resp.getWriter().println("Error=" + e.getMessage());
        }
    }
    private final Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");

    public boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        return pattern.matcher(strNum).matches();
    }

    public static String escapeHTML(String s) {
        StringBuilder out = new StringBuilder(Math.max(16, s.length()));
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c > 127 || c == '"' || c == '\'' || c == '<' || c == '>' || c == '&') {
                out.append("&#");
                out.append((int) c);
                out.append(';');
            } else if (c == '\n') {
                out.append("<br>");
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }
}
