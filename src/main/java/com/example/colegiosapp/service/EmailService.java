package com.example.colegiosapp.service;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Value("${sendgrid.api.key}")
    private String apiKey;

    @Value("${mep.mail.from}")
    private String fromAddress;

    private void enviarEmail(String destinatario, String asunto, String html) {

        try {

            Email from = new Email(fromAddress);
            Email to = new Email(destinatario);

            Content content = new Content("text/html", html);
            Mail mail = new Mail(from, asunto, to, content);

            SendGrid sg = new SendGrid(apiKey);

            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);

            System.out.println(" Email enviado. Status: " + response.getStatusCode());

        } catch (Exception e) {

            System.err.println(" Error enviando correo: " + e.getMessage());

        }

    }

    public void enviarAprobacion(String destinatario, String nombreEstudiante, String grado) {

        String asunto = "✅ Estudiante aprobado para matrícula · MEP";

        String html = """
                <div style="font-family:'Segoe UI',sans-serif;max-width:580px;margin:0 auto;
                            border:1px solid #e2e8f0;border-radius:12px;overflow:hidden;">
                  <div style="background:linear-gradient(135deg,#0D3B8E,#1450b8);
                              padding:28px 32px;">
                    <h1 style="color:#fff;font-size:20px;margin:0;">
                      Matrículas y Educación Pública
                    </h1>
                    <p style="color:rgba(255,255,255,.7);font-size:13px;margin:4px 0 0;">
                      República de Colombia
                    </p>
                  </div>
                  <div style="padding:32px;">
                    <div style="background:#f0fdf4;border:1.5px solid #bbf7d0;
                                border-radius:10px;padding:16px 20px;margin-bottom:24px;">
                      <p style="margin:0;font-weight:700;color:#15803d;font-size:15px;">
                        ✅ Estudiante aprobado
                      </p>
                    </div>

                    <p style="color:#374151;font-size:14px;">
                      Nos complace informarle que el/la estudiante
                      <strong>%s</strong>
                      ha sido aprobado/a para el grado
                      <strong>%s</strong>.
                    </p>

                  </div>
                </div>
                """.formatted(nombreEstudiante, grado);

        enviarEmail(destinatario, asunto, html);

    }

    public void enviarRechazo(String destinatario, String nombreEstudiante, String grado) {

        String asunto = "❌ Solicitud de matrícula no aprobada · MEP";

        String html = """
                <div style="font-family:'Segoe UI',sans-serif;max-width:580px;margin:0 auto;
                            border:1px solid #e2e8f0;border-radius:12px;overflow:hidden;">
                  <div style="background:linear-gradient(135deg,#0D3B8E,#1450b8);
                              padding:28px 32px;">
                    <h1 style="color:#fff;font-size:20px;margin:0;">
                      Matrículas y Educación Pública
                    </h1>
                  </div>

                  <div style="padding:32px;">

                    <div style="background:#fef2f2;border:1.5px solid #fecaca;
                                border-radius:10px;padding:16px 20px;margin-bottom:24px;">

                      <p style="margin:0;font-weight:700;color:#dc2626;font-size:15px;">
                        ❌ Solicitud no aprobada
                      </p>

                    </div>

                    <p style="color:#374151;font-size:14px;">
                      El/la estudiante
                      <strong>%s</strong>
                      no fue aprobado/a para el grado
                      <strong>%s</strong>.
                    </p>

                  </div>
                </div>
                """.formatted(nombreEstudiante, grado);

        enviarEmail(destinatario, asunto, html);

    }
}