package com.example.colegiosapp.report;

/**
 * Estrategia para generar reportes de citas de una institución.
 */
public interface ReportStrategy {
    /**
     * Genera el reporte para la institución indicada.
     * @param institucionId ID de la institución (puede ser null para incluir todas).
     * @return Reporte en formato binario (PDF/Excel/CSV, etc.).
     * @throws Exception si ocurre un error durante la generación.
     */
    byte[] generateReport(Long institucionId) throws Exception;
}
