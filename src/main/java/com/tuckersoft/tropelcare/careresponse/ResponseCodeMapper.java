package com.tuckersoft.tropelcare.careresponse;

import java.util.Map;

public final class ResponseCodeMapper {

    private static final Map<String, String> MAPPING = Map.of(
            "HAMBRE", "DISPATCH_NUTRIENT_PACK",
            "ABANDONO", "SEND_COMPANIONSHIP_PROTOCOL",
            "MUTACION", "ISOLATE_AND_OBSERVE",
            "FUGA", "ACTIVATE_SECTOR_LOCK",
            "CONFLICTO", "DEPLOY_MEDIATION_FIELD",
            "REPRODUCCION_MASIVA", "ENABLE_POPULATION_CONTROL",
            "SENAL_CORRUPTA", "ARCHIVE_AND_IGNORE"
    );

    private ResponseCodeMapper() {}

    public static String forSignalType(String signalType) {
        return MAPPING.getOrDefault(signalType, "ARCHIVE_AND_IGNORE");
    }
}
