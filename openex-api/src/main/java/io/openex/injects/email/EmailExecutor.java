package io.openex.injects.email;

import io.openex.config.OpenExConfig;
import io.openex.contract.Contract;
import io.openex.database.model.*;
import io.openex.execution.ExecutableInject;
import io.openex.execution.ExecutionContext;
import io.openex.execution.Injector;
import io.openex.injects.email.model.EmailContent;
import io.openex.injects.email.service.EmailService;
import io.openex.model.Expectation;
import io.openex.model.expectation.ManualExpectation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Stream;

import static io.openex.database.model.ExecutionTrace.traceError;
import static io.openex.injects.email.EmailContract.EMAIL_GLOBAL;

@Component(EmailContract.TYPE)
public class EmailExecutor extends Injector {

  @Resource
  private OpenExConfig openExConfig;

  private EmailService emailService;

  @Value("${openex.mail.imap.enabled}")
  private boolean imapEnabled;

  @Autowired
  public void setEmailService(EmailService emailService) {
    this.emailService = emailService;
  }

  private void sendMulti(Execution execution, List<ExecutionContext> users, String replyTo, String inReplyTo,
      String subject, String message, List<DataAttachment> attachments) {
    try {
      emailService.sendEmail(execution, users, replyTo, inReplyTo, subject, message, attachments);
    } catch (Exception e) {
      execution.addTrace(traceError("email", e.getMessage(), e));
    }
  }

  private void sendSingle(Execution execution, List<ExecutionContext> users, String replyTo, String inReplyTo,
      boolean mustBeEncrypted, String subject, String message, List<DataAttachment> attachments) {
    users.stream().parallel().forEach(user -> {
      try {
        emailService.sendEmail(execution, user, replyTo, inReplyTo, mustBeEncrypted, subject, message, attachments);
      } catch (Exception e) {
        execution.addTrace(traceError("email", e.getMessage(), e));
      }
    });
  }

  @Override
  public List<Expectation> process(
      @NotNull final Execution execution,
      @NotNull final ExecutableInject injection,
      @NotNull final Contract contract)
      throws Exception {
    Inject inject = injection.getInject();
    EmailContent content = contentConvert(injection, EmailContent.class);
    List<Document> documents = inject.getDocuments().stream().filter(InjectDocument::isAttached)
        .map(InjectDocument::getDocument).toList();
    List<DataAttachment> attachments = resolveAttachments(execution, injection, documents);
    String inReplyTo = content.getInReplyTo();
    String subject = content.getSubject();
    String message = content.buildMessage(injection, this.imapEnabled);
    boolean mustBeEncrypted = content.isEncrypted();
    // Resolve the attachments only once
    List<ExecutionContext> users = injection.getContextUser();
    if (users.isEmpty()) {
      throw new UnsupportedOperationException("Email needs at least one user");
    }
    Exercise exercise = injection.getSource().getExercise();
    String replyTo = exercise != null ? exercise.getReplyTo() : this.openExConfig.getDefaultMailer();
    //noinspection SwitchStatementWithTooFewBranches
    switch (contract.getId()) {
      case EMAIL_GLOBAL -> sendMulti(execution, users, replyTo, inReplyTo, subject, message, attachments);
      default -> sendSingle(execution, users, replyTo, inReplyTo, mustBeEncrypted, subject, message, attachments);
    }
    return content.getExpectations()
        .stream()
        .flatMap((entry) -> switch (entry.getType()) {
          case MANUAL -> Stream.of((Expectation) new ManualExpectation(entry.getScore(), entry.getName(), entry.getDescription()));
          default -> Stream.of();
        })
        .toList();
  }
}
