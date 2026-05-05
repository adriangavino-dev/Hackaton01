package com.tuckersoft.tropelcare.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class GithubModelsClient implements SignalClassifier {

    private static final Logger log = LoggerFactory.getLogger(GithubModelsClient.class);

    private static final Set<String> VALID_TYPES = Set.of(
            "HAMBRE", "ABANDONO", "MUTACION", "FUGA",
            "CONFLICTO", "REPRODUCCION_MASIVA", "SENAL_CORRUPTA");
    private static final Set<String> VALID_SEVERITY = Set.of("LEVE", "MODERADO", "GRAVE", "CRITICO");
    private static final Set<String> VALID_UNITS = Set.of(
            "Laboratorio de Nutricion", "Unidad de Bienestar", "Division Genetica",
            "Equipo de Contencion", "Consejo de Mediacion", "Control Demografico", "Archivo de Senales");

    private static final String SYSTEM_PROMPT = """
            Eres el sistema de clasificacion de senales del TropelCare Signal Engine, desarrollado por Tuckersoft.
            Recibes senales emitidas por criaturas digitales llamadas Tropeles y debes clasificarlas.
            Responde UNICAMENTE con este JSON en una sola linea, sin texto adicional, sin markdown, sin bloques de codigo:
            {"signalType":"<TIPO>","severity":"<GRAVEDAD>","assignedUnit":"<UNIDAD>","recommendedAction":"<accion breve y concreta en espanol>","personalityNote":"<maximo 2 oraciones divertidas en espanol sobre el estado emocional del Tropel>"}

            Tipos validos: HAMBRE, ABANDONO, MUTACION, FUGA, CONFLICTO, REPRODUCCION_MASIVA, SENAL_CORRUPTA
            Gravedades validas: LEVE, MODERADO, GRAVE, CRITICO
            Unidades validas: Laboratorio de Nutricion, Unidad de Bienestar, Division Genetica, Equipo de Contencion, Consejo de Mediacion, Control Demografico, Archivo de Senales

            Reglas:
            - HAMBRE -> Laboratorio de Nutricion: escasez de nutrientes, intento de morder objetos digitales.
            - ABANDONO -> Unidad de Bienestar: angustia por falta de interaccion, silencio prolongado.
            - MUTACION -> Division Genetica: cambios fisicos, brillo anomalo, glitch corporal, duplicacion.
            - FUGA -> Equipo de Contencion: intento de abandonar el sector, zonas prohibidas.
            - CONFLICTO -> Consejo de Mediacion: pelea entre Tropeles, invasion de territorio.
            - REPRODUCCION_MASIVA -> Control Demografico: reproduccion no planificada, clonacion accidental.
            - SENAL_CORRUPTA -> Archivo de Senales: senal ininteligible, estatica, datos corruptos.
            """;

    private final RestClient http;
    private final ObjectMapper mapper = new ObjectMapper();
    private final String url;
    private final String token;
    private final String modelId;

    public GithubModelsClient(@Value("${github.models.url}") String url,
                              @Value("${github.token}") String token,
                              @Value("${github.models.model-id}") String modelId) {
        this.url = url;
        this.token = token;
        this.modelId = modelId;
        this.http = RestClient.builder().baseUrl(url).build();
    }

    @Override
    public ClassificationResult classify(String rawContent) {
        try {
            Map<String, Object> body = Map.of(
                    "model", modelId,
                    "messages", List.of(
                            Map.of("role", "system", "content", SYSTEM_PROMPT),
                            Map.of("role", "user", "content", rawContent)
                    ),
                    "temperature", 0.2
            );

            String response = http.post()
                    .uri("/chat/completions")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            JsonNode root = mapper.readTree(response);
            String content = root.path("choices").path(0).path("message").path("content").asText(null);
            if (content == null || content.isBlank()) return ClassificationResult.ofFallback();

            String json = extractJson(content);
            if (json == null) return ClassificationResult.ofFallback();

            JsonNode parsed = mapper.readTree(json);
            String signalType = parsed.path("signalType").asText(null);
            String severity = parsed.path("severity").asText(null);
            String assignedUnit = parsed.path("assignedUnit").asText(null);
            String recommendedAction = parsed.path("recommendedAction").asText(null);
            String personalityNote = parsed.path("personalityNote").asText(null);

            if (!VALID_TYPES.contains(signalType)
                    || !VALID_SEVERITY.contains(severity)
                    || !VALID_UNITS.contains(assignedUnit)
                    || recommendedAction == null || recommendedAction.isBlank()) {
                return ClassificationResult.ofFallback();
            }

            return new ClassificationResult(signalType, severity, assignedUnit, recommendedAction,
                    personalityNote == null || personalityNote.isBlank() ? null : personalityNote, false);

        } catch (Exception ex) {
            log.warn("Fallo en clasificacion IA, aplicando fallback: {}", ex.getMessage());
            return ClassificationResult.ofFallback();
        }
    }

    public static String extractJson(String text) {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start < 0 || end <= start) return null;
        return text.substring(start, end + 1);
    }
}
