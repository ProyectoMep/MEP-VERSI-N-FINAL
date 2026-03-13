package com.example.colegiosapp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${mep.mail.from}")
    private String fromAddress;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarAprobacion(String destinatario, String nombreEstudiante, String grado) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromAddress);
            helper.setTo(destinatario);
            helper.setSubject("✅ Estudiante aprobado para matrícula · MEP");

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
                                border-radius:10px;padding:16px 20px;margin-bottom:24px;
                                display:flex;align-items:center;gap:12px;">
                      <span style="font-size:24px;">✅</span>
                      <div>
                        <p style="margin:0;font-weight:700;color:#15803d;font-size:15px;">
                          Estudiante aprobado
                        </p>
                        <p style="margin:0;color:#166534;font-size:13px;">
                          La solicitud de matrícula ha sido aceptada.
                        </p>
                      </div>
                    </div>
                    <p style="color:#374151;font-size:14px;line-height:1.6;">
                      Nos complace informarle que el/la estudiante
                      <strong style="color:#0D3B8E;">%s</strong>
                      ha sido <strong>aprobado/a</strong> para el grado
                      <strong style="color:#F7861C;">%s</strong>
                      en el proceso de matrícula del año lectivo 2025.
                    </p>
                    <p style="color:#374151;font-size:14px;line-height:1.6;">
                      Por favor acérquese a la institución con los documentos requeridos
                      para completar el proceso de matrícula.
                    </p>
                    <div style="background:#f8faff;border-radius:8px;padding:14px 18px;
                                border-left:4px solid #0D3B8E;margin-top:20px;">
                      <p style="margin:0;font-size:12px;color:#64748b;">
                        Este mensaje fue generado automáticamente por el sistema MEP.
                        Si tiene dudas, comuníquese directamente con la institución.
                      </p>
                    </div>
                  </div>
                </div>
                """.formatted(nombreEstudiante, grado);

            helper.setText(html, true);
            mailSender.send(message);

        } catch (Exception e) {
            System.err.println("❌ Error enviando correo de aprobación: " + e.getMessage());
        }
    }

    public void enviarRechazo(String destinatario, String nombreEstudiante, String grado) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromAddress);
            helper.setTo(destinatario);
            helper.setSubject("❌ Solicitud de matrícula no aprobada · MEP");

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
                    <div style="background:#fef2f2;border:1.5px solid #fecaca;
                                border-radius:10px;padding:16px 20px;margin-bottom:24px;">
                      <p style="margin:0;font-weight:700;color:#dc2626;font-size:15px;">
                        ❌ Solicitud no aprobada
                      </p>
                      <p style="margin:4px 0 0;color:#991b1b;font-size:13px;">
                        La solicitud de matrícula no ha sido aceptada en esta ocasión.
                      </p>
                    </div>
                    <p style="color:#374151;font-size:14px;line-height:1.6;">
                      Le informamos que el/la estudiante
                      <strong style="color:#0D3B8E;">%s</strong>
                      no ha sido aprobado/a para el grado
                      <strong>%s</strong>
                      en el proceso de matrícula del año lectivo 2025.
                    </p>
                    <p style="color:#374151;font-size:14px;line-height:1.6;">
                      Si considera que hubo un error o desea más información,
                      por favor comuníquese directamente con la institución educativa.
                    </p>
                    <div style="background:#f8faff;border-radius:8px;padding:14px 18px;
                                border-left:4px solid #dc2626;margin-top:20px;">
                      <p style="margin:0;font-size:12px;color:#64748b;">
                        Este mensaje fue generado automáticamente por el sistema MEP.
                      </p>
                    </div>
                  </div>
                </div>
                """.formatted(nombreEstudiante, grado);

            helper.setText(html, true);
            mailSender.send(message);

        } catch (Exception e) {
            System.err.println("❌ Error enviando correo de rechazo: " + e.getMessage());
        }
    }
}