package com.tuckersoft.tropelcare.notification;

import com.tuckersoft.tropelcare.signal.TropelSignal;
import com.tuckersoft.tropelcare.signal.TropelSignalCreatedEvent;
import com.tuckersoft.tropelcare.signal.TropelSignalRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;

@Component
public class TropelSignalNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(TropelSignalNotificationListener.class);

    private final TropelSignalRepository signals;
    private final NotificationLogRepository notifications;
    private final JavaMailSender mailSender;

    public TropelSignalNotificationListener(TropelSignalRepository signals,
                                            NotificationLogRepository notifications,
                                            JavaMailSender mailSender) {
        this.signals = signals;
        this.notifications = notifications;
        this.mailSender = mailSender;
    }

    @Async("tropelExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSignalCreated(TropelSignalCreatedEvent event) {
        TropelSignal signal = signals.findById(event.signalId()).orElse(null);
        if (signal == null) {
            log.error("Signal {} not found in listener", event.signalId());
            return;
        }

        signal.setStatus("PROCESANDO");
        signal.setUpdatedAt(Instant.now());
        signals.save(signal);

        String recipient = signal.getGuardian().getNotificationEmail();
        String subject = String.format("[TROPELCARE] %s detectada en %s | Severidad %s",
                signal.getSignalType(), signal.getTropel().getName(), signal.getSeverity());
        String body = buildBody(signal);

        NotificationLog logRecord = new NotificationLog();
        logRecord.setSignal(signal);
        logRecord.setRecipientEmail(recipient);
        logRecord.setSubject(subject);
        logRecord.setCreatedAt(Instant.now());

        String finalStatus;
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(recipient);
            msg.setSubject(subject);
            msg.setText(body);
            mailSender.send(msg);

            signal.setStatus("ATENDIDA");
            logRecord.setNotifStatus("SENT");
            logRecord.setSentAt(Instant.now());
            finalStatus = "ATENDIDA";
        } catch (Exception ex) {
            log.error("Fallo el envio de correo para signal {}: {}", signal.getId(), ex.getMessage(), ex);
            signal.setStatus("ERROR");
            logRecord.setNotifStatus("FAILED");
            logRecord.setErrorMessage(ex.getMessage());
            finalStatus = "ERROR";
        }

        signal.setUpdatedAt(Instant.now());
        signals.save(signal);
        notifications.save(logRecord);

        System.out.printf(
                "[TROPEL-LOG] Signal ID: %d | Tropel: %s | Type: %s | Severity: %s | Unit: %s | Thread: %s | Status: %s%n",
                signal.getId(),
                signal.getTropel().getName(),
                signal.getSignalType(),
                signal.getSeverity(),
                signal.getAssignedUnit(),
                Thread.currentThread().getName(),
                finalStatus
        );
    }

    private String buildBody(TropelSignal s) {
        return "Hola " + s.getGuardian().getDisplayName() + ",\n\n" +
                "Tu Tropel ha emitido una senal que requiere atencion.\n\n" +
                "----------------------------------------\n" +
                "Senal ID         : #" + s.getId() + "\n" +
                "Tropel           : " + s.getTropel().getName() + " (" + s.getTropel().getSpecies() + ")\n" +
                "Tipo de senal    : " + s.getSignalType() + "\n" +
                "Severidad        : " + s.getSeverity() + "\n" +
                "Unidad asignada  : " + s.getAssignedUnit() + "\n" +
                "Accion sugerida  : " + s.getRecommendedAction() + "\n" +
                "Estado vital     : " + s.getTropel().getVitalState() + "\n" +
                "Nivel de energia : " + s.getTropel().getEnergyLevel() + "/100\n" +
                "Indice de caos   : " + s.getTropel().getChaosIndex() + "/100\n" +
                "Etapa mutacion   : " + s.getTropel().getMutationStage() + "/5\n" +
                "Registrada       : " + s.getCreatedAt() + "\n" +
                "----------------------------------------\n\n" +
                "Senal original:\n" +
                "\"" + s.getRawContent() + "\"\n\n" +
                "-- TropelCare Signal Engine, Tuckersoft\n";
    }
}
