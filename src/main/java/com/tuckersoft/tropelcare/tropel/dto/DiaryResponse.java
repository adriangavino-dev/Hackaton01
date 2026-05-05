package com.tuckersoft.tropelcare.tropel.dto;

import java.time.Instant;
import java.util.List;

public record DiaryResponse(Long tropelId, String tropelName, List<Note> notes) {

    public record Note(Long signalId, String personalityNote, Instant createdAt) {}
}
