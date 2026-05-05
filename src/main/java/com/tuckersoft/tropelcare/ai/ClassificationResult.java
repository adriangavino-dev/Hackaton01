package com.tuckersoft.tropelcare.ai;

public record ClassificationResult(
        String signalType,
        String severity,
        String assignedUnit,
        String recommendedAction,
        String personalityNote,
        boolean fallback
) {
    public static ClassificationResult ofFallback() {
        return new ClassificationResult(
                "SENAL_CORRUPTA",
                "LEVE",
                "Archivo de Senales",
                "Archivar la senal y revisar manualmente si se repite.",
                null,
                true
        );
    }
}
