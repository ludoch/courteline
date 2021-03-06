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
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletException;

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
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return; // Ignore spammers
       }
        System.out.println(gRecaptchaResponse);
        if (!VerifyRecaptcha.verify(gRecaptchaResponse)) {
            System.out.println("RE CAPTCHA INVALID.so we stop.");
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return; // Ignore spammers
       }
        Map params = req.getParameterMap();
        Iterator i = params.keySet().iterator();
        while (i.hasNext()) {
            String key = (String) i.next();
            String value = ((String[]) params.get(key))[0];
            System.out.println(key + " = " + value);
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
        if (date.isEmpty() && !isNumeric(nuits)) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return; // Ignore spammers
        }
        String content = "Demande d'information via https://courteline.appspot.com";
        content += "\n\n" + "chambre_souhaitee: " + chambre_souhaitee;
        content += "\n" + "date: " + date;
        content += "\n" + "email: " + email;
        content += "\n" + "nom: " + nom;
        content += "\n" + "prenom: " + prenom;
        content += "\n" + "nombre_de_personnes: " + personnes;
        content += "\n" + "nombre_de_nuits: " + nuits;
        content += "\n" + "telephone: " + telephone;
        content += "\n" + "message: " + message;

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
            msg.setSubject("Demande r??servation pour Courteline");
            msg.setText(content);
            Transport.send(msg);
            resp.getWriter().println("Site Courteline, email re??u, merci: <br>"
                    + escapeHTML(content)
                    + "<br><button onclick=\"window.history.back()\">Retour page pr??c??dente.</button><br>");
            if (date.isEmpty()) {
                resp.getWriter().println("Erreur, la date est incorrecte. <br>");

            }

            //    RequestDispatcher dispatcher = getServletContext()
            //            .getRequestDispatcher("https://courteline-nantes.appspot.com/");
            //    dispatcher.forward(req, resp);
        } catch (IOException | MessagingException e) {
            resp.getWriter().println("Site Courteline, email erreur. \n\n");
            e.printStackTrace();
            resp.getWriter().println("Error=" + e.getMessage());
        }
    }
    private Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");

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
