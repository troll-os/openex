package io.openex.injects.email.service;


import io.openex.database.model.Communication;
import io.openex.database.model.Inject;
import io.openex.database.model.Setting;
import io.openex.database.model.User;
import io.openex.database.repository.CommunicationRepository;
import io.openex.database.repository.InjectRepository;
import io.openex.database.repository.SettingRepository;
import io.openex.database.repository.UserRepository;
import io.openex.service.FileService;
import jakarta.activation.DataSource;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.apache.commons.mail.util.MimeMessageParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.lang.Integer.parseInt;
import static java.time.Instant.now;

@Service
public class ImapService {

    private static final Logger LOGGER = Logger.getLogger(ImapService.class.getName());
    private final static Pattern INJECT_ID_PATTERN = Pattern.compile("\\[inject_id=(.*)\\]");
    private final static String PROVIDER = "imap";

    private Store imapStore;

    @Value("${openex.mail.imap.enabled}")
    private boolean enabled;

    @Value("${openex.mail.imap.host}")
    private String host;

    @Value("${openex.mail.imap.port}")
    private Integer port;

    @Value("${openex.mail.imap.username}")
    private String username;

    @Value("${openex.mail.imap.password}")
    private String password;

    @Value("${openex.mail.imap.inbox}")
    private List<String> inboxFolders;

    @Value("${openex.mail.imap.sent}")
    private String sentFolder;

    private UserRepository userRepository;
    private InjectRepository injectRepository;
    private CommunicationRepository communicationRepository;
    private SettingRepository settingRepository;
    private FileService fileService;

    public ImapService(Environment env) throws Exception {
        initStore(env);
    }

    @Autowired
    public void setFileService(FileService fileService) {
        this.fileService = fileService;
    }

    @Autowired
    public void setSettingRepository(SettingRepository settingRepository) {
        this.settingRepository = settingRepository;
    }

    @Autowired
    public void setInjectRepository(InjectRepository injectRepository) {
        this.injectRepository = injectRepository;
    }

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Autowired
    public void setCommunicationRepository(CommunicationRepository communicationRepository) {
        this.communicationRepository = communicationRepository;
    }

    private void initStore(Environment env) throws Exception {
        Session session = Session.getDefaultInstance(buildProperties(env), null);
        imapStore = session.getStore(PROVIDER);
        String host = env.getProperty("openex.mail.imap.host");
        int port = env.getProperty("openex.mail.imap.port", Integer.class, 995);
        String username = env.getProperty("openex.mail.imap.username");
        String password = env.getProperty("openex.mail.imap.password");
        String sentFolder = env.getProperty("openex.mail.imap.sent");
        boolean isEnabled = env.getProperty("openex.mail.imap.enabled", Boolean.class, false);
        if (isEnabled) {
            LOGGER.log(Level.INFO, "IMAP sync started");
            imapStore.connect(host, port, username, password);
            try {
                Folder defaultFolder = imapStore.getDefaultFolder();
                Folder sentBox = defaultFolder.getFolder(sentFolder);
                if (!sentBox.exists()) {
                    sentBox.create(Folder.READ_WRITE);
                    sentBox.setSubscribed(true);
                }
            }  catch (Exception e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
        } else {
            LOGGER.log(Level.INFO, "IMAP sync disabled");
        }
    }

    private String getTextFromMimeMultipart(MimeMultipart mimeMultipart) throws MessagingException, IOException {
        StringBuilder result = new StringBuilder();
        int count = mimeMultipart.getCount();
        for (int i = 0; i < count; i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                result.append(bodyPart.getContent());
                break;
            } else if (bodyPart.getContent() instanceof MimeMultipart) {
                result.append(getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent()));
            }
        }
        return result.toString();
    }

    private String getHtmlFromMimeMultipart(MimeMultipart mimeMultipart) throws MessagingException, IOException {
        StringBuilder result = new StringBuilder();
        int count = mimeMultipart.getCount();
        for (int i = 0; i < count; i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/html")) {
                result.append((String) bodyPart.getContent());
                break;
            } else if (bodyPart.getContent() instanceof MimeMultipart) {
                result.append(getHtmlFromMimeMultipart((MimeMultipart) bodyPart.getContent()));
            }
        }
        return result.toString();
    }

    private String getTextFromMessage(Message message) throws MessagingException, IOException {
        String result = "";
        if (message.isMimeType("multipart/*")) {
            MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
            result = getTextFromMimeMultipart(mimeMultipart);
        } else if (message.isMimeType("text/plain")) {
            result = message.getContent().toString();
        }
        return result;
    }

    private String getHtmlFromMessage(Message message) throws MessagingException, IOException {
        String result = "";
        if (message.isMimeType("multipart/*")) {
            MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
            result = getHtmlFromMimeMultipart(mimeMultipart);
        } else if (message.isMimeType("text/html")) {
            result = message.getContent().toString();
        }
        return result;
    }

    private Properties buildProperties(Environment env) {
        String sslEnable = env.getProperty("openex.mail.imap.ssl.enable");
        String sslTrust = env.getProperty("openex.mail.imap.ssl.trust");
        String sslAuth = env.getProperty("openex.mail.imap.auth");
        String sslStartTLS = env.getProperty("openex.mail.imap.starttls.enable");
        Properties props = new Properties();
        props.setProperty("mail.imap.ssl.enable", sslEnable);
        props.setProperty("mail.imap.ssl.trust", sslTrust);
        props.setProperty("mail.imap.auth", sslAuth);
        props.setProperty("mail.imap.starttls.enable", sslStartTLS);
        return props;
    }

    private List<String> computeParticipants(Message message) throws Exception {
        List<String> from = Arrays.stream(message.getFrom()).map(addr -> (((InternetAddress) addr).getAddress())).toList();
        List<String> recipients = Arrays.stream(message.getAllRecipients()).map(addr -> (((InternetAddress) addr).getAddress())).toList();
        return Stream.concat(from.stream(), recipients.stream()).map(String::toLowerCase).filter(recipient -> !recipient.equals(username)).distinct().toList();
    }

    private Inject injectResolver(String content, String contentHtml) {
        Matcher matcher = content.length() > 10
                ? INJECT_ID_PATTERN.matcher(content) : INJECT_ID_PATTERN.matcher(contentHtml);
        if (matcher.find()) {
            String injectId = matcher.group(1);
            return injectRepository.findById(injectId).orElse(null);
        }
        return null;
    }

    private void parseMessages(Message[] messages, Boolean isSent) throws Exception {
        for (Message message : messages) {
            MimeMessage mimeMessage = (MimeMessage) message;
            String messageID = mimeMessage.getMessageID();
            boolean messageAlreadyAvailable = communicationRepository.existsByIdentifier(messageID);
            if (!messageAlreadyAvailable) {
                String content = getTextFromMessage(message);
                String contentHtml = getHtmlFromMessage(message);
                Inject inject = injectResolver(content, contentHtml);
                List<String> participants = computeParticipants(message);
                List<User> users = userRepository.findAllByEmailInIgnoreCase(participants);
                if (inject != null && !users.isEmpty()) {
                    String subject = message.getSubject();
                    String from = String.valueOf(Arrays.stream(message.getFrom()).toList().get(0));
                    String to = String.valueOf(Arrays.stream(message.getAllRecipients()).toList());
                    Date receivedDate = message.getReceivedDate();
                    Date sentDate = message.getSentDate();
                    // Save messaging
                    Communication communication = new Communication();
                    communication.setReceivedAt(receivedDate.toInstant());
                    communication.setSentAt(sentDate.toInstant());
                    communication.setSubject(subject);
                    communication.setContent(content);
                    communication.setContentHtml(contentHtml);
                    communication.setIdentifier(messageID);
                    communication.setUsers(users);
                    communication.setInject(inject);
                    communication.setAnimation(isSent);
                    communication.setFrom(from);
                    communication.setTo(to);
                    try {
                        // Save the communication
                        Communication comm = communicationRepository.save(communication);
                        // Update inject for real time
                        inject.setUpdatedAt(now());
                        injectRepository.save(inject);
                        // Upload attachments in communication
                        final MimeMessageParser mimeParser = new MimeMessageParser(mimeMessage).parse();
                        final List<DataSource> attachmentList = mimeParser.getAttachmentList();
                        final List<String> uploads = new ArrayList<>();
                        String exerciseId = inject.getExercise().getId();
                        for (DataSource dataSource : attachmentList) {
                            final String fileName = dataSource.getName();
                            String path = "/" + exerciseId + "/communications/" + comm.getId();
                            String uploadName = fileService.uploadStream(path, fileName, dataSource.getInputStream());
                            uploads.add(uploadName);
                        }
                        // Add attachment in the communication
                        comm.setAttachments(uploads.toArray(String[]::new));
                        communicationRepository.save(comm);
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    }
                }
            }
        }
    }

    private void synchronizeBox(Folder inbox, Boolean isSent) throws Exception {
        String inboxKey = username + "-imap-" + inbox.getName();
        Optional<Setting> state = settingRepository.findByKey(inboxKey);
        Setting currentState = state.orElse(null);
        if (currentState == null) {
            currentState = settingRepository.save(new Setting(inboxKey, "0"));
        }
        int startMessageNumber = parseInt(currentState.getValue());
        int messageCount = inbox.getMessageCount();
        if (startMessageNumber < messageCount) {
            LOGGER.log(Level.INFO, "synchronizeInbox " + inbox.getName() + " from " + startMessageNumber + " to " + messageCount);
            int start = startMessageNumber + 1;
            Message[] messages = inbox.getMessages(start, messageCount);
            if (messages.length > 0) {
                parseMessages(messages, isSent);
            }
        }
        currentState.setValue(String.valueOf(messageCount));
        settingRepository.save(currentState);
    }

    private void syncFolders() throws Exception {
        // Sync sent
        Folder sentBox = imapStore.getFolder(sentFolder);
        sentBox.open(Folder.READ_ONLY);
        synchronizeBox(sentBox, true);
        // Sync received
        for (String listeningFolder : inboxFolders) {
            Folder inbox = imapStore.getFolder(listeningFolder);
            inbox.open(Folder.READ_ONLY);
            synchronizeBox(inbox, false);
        }
    }

    // Sync folders every 10 sec
    @Scheduled(fixedDelay = 10000, initialDelay = 10000)
    public void connectionListener() throws Exception {
        if (enabled) {
            if (!imapStore.isConnected()) {
                imapStore.connect(host, port, username, password);
            }
            syncFolders();
        }
    }

    public void storeSentMessage(MimeMessage message) throws Exception {
        if (enabled) {
            Folder folder = imapStore.getFolder(sentFolder);
            folder.open(Folder.READ_WRITE);
            message.setFlag(Flags.Flag.SEEN, true);
            folder.appendMessages(new Message[]{message});
            folder.close();
        }
    }
}
