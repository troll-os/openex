package io.openex.rest.inject;

import io.openex.contract.Contract;
import io.openex.database.model.*;
import io.openex.database.repository.*;
import io.openex.database.specification.InjectSpecification;
import io.openex.execution.ExecutableInject;
import io.openex.execution.ExecutionContext;
import io.openex.execution.Injector;
import io.openex.rest.helper.RestBehavior;
import io.openex.rest.inject.form.*;
import io.openex.service.AssetGroupService;
import io.openex.service.AssetService;
import io.openex.service.ContractService;
import io.openex.service.ExecutionContextService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static io.openex.config.SessionHelper.currentUser;
import static io.openex.database.model.ExecutionTrace.traceSuccess;
import static io.openex.database.specification.CommunicationSpecification.fromInject;
import static io.openex.helper.DatabaseHelper.resolveOptionalRelation;
import static io.openex.helper.DatabaseHelper.updateRelation;
import static io.openex.helper.StreamHelper.fromIterable;
import static java.time.Instant.now;

@RestController
public class InjectApi extends RestBehavior {

  private static final int MAX_NEXT_INJECTS = 6;

  private CommunicationRepository communicationRepository;
  private ExerciseRepository exerciseRepository;
  private UserRepository userRepository;
  private InjectRepository injectRepository;
  private InjectDocumentRepository injectDocumentRepository;
  private TeamRepository teamRepository;
  private AssetService assetService;
  private AssetGroupService assetGroupService;
  private TagRepository tagRepository;
  private DocumentRepository documentRepository;
  private ApplicationContext context;
  private ContractService contractService;
  private ExecutionContextService executionContextService;

  @Autowired
  public void setUserRepository(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Autowired
  public void setCommunicationRepository(CommunicationRepository communicationRepository) {
    this.communicationRepository = communicationRepository;
  }

  @Autowired
  public void setInjectDocumentRepository(InjectDocumentRepository injectDocumentRepository) {
    this.injectDocumentRepository = injectDocumentRepository;
  }

  @Autowired
  public void setDocumentRepository(DocumentRepository documentRepository) {
    this.documentRepository = documentRepository;
  }

  @Autowired
  public void setTagRepository(TagRepository tagRepository) {
    this.tagRepository = tagRepository;
  }

  @Autowired
  public void setExerciseRepository(ExerciseRepository exerciseRepository) {
    this.exerciseRepository = exerciseRepository;
  }

  @Autowired
  public void setTeamRepository(TeamRepository teamRepository) {
    this.teamRepository = teamRepository;
  }

  @Autowired
  public void setAssetService(@NotNull final AssetService assetService) {
    this.assetService = assetService;
  }


  @Autowired
  public void setAssetGroupService(@NotNull final AssetGroupService assetGroupService) {
    this.assetGroupService = assetGroupService;
  }

  @Autowired
  public void setContractService(ContractService contractService) {
    this.contractService = contractService;
  }

  @Autowired
  public void setInjectRepository(InjectRepository injectRepository) {
    this.injectRepository = injectRepository;
  }

  @Autowired
  public void setContext(ApplicationContext context) {
    this.context = context;
  }
  @Autowired
  public void setExecutionContextService(@NotNull final ExecutionContextService executionContextService) {
    this.executionContextService = executionContextService;
  }

  @GetMapping("/api/inject_types")
  public Collection<Contract> injectTypes() {
    return contractService.getContracts().values();
  }

  @GetMapping("/api/injects/try/{injectId}")
  public InjectStatus tryInject(@PathVariable String injectId) {
    Inject inject = injectRepository.findById(injectId).orElseThrow();
    User user = this.userRepository.findById(currentUser().getId()).orElseThrow();
    List<ExecutionContext> userInjectContexts = List.of(
        this.executionContextService.executionContext(user, inject, "Direct test")
    );
    Contract contract = contractService.resolveContract(inject);
    if (contract == null) {
      throw new UnsupportedOperationException("Unknown inject contract " + inject.getContract());
    }
    ExecutableInject injection = new ExecutableInject(false, true, inject, contract, List.of(), inject.getAssets(), inject.getAssetGroups(), userInjectContexts);
    Injector executor = context.getBean(contract.getConfig().getType(), Injector.class);
    Execution execution = executor.executeInjection(injection);
    return InjectStatus.fromExecution(execution, inject);
  }

  @Transactional(rollbackOn = Exception.class)
  @PutMapping("/api/injects/{exerciseId}/{injectId}")
  @PreAuthorize("isExercisePlanner(#exerciseId)")
  public Inject updateInject(
      @PathVariable String exerciseId,
      @PathVariable String injectId,
      @Valid @RequestBody InjectInput input) {
    Exercise exercise = exerciseRepository.findById(exerciseId).orElseThrow();
    Inject inject = injectRepository.findById(injectId).orElseThrow();
    inject.setUpdateAttributes(input);
    // Set dependencies
    inject.setDependsOn(updateRelation(input.getDependsOn(), inject.getDependsOn(), injectRepository));
    inject.setTeams(fromIterable(teamRepository.findAllById(input.getTeams())));
    inject.setAssets(fromIterable(this.assetService.assets(input.getAssets())));
    inject.setAssetGroups(fromIterable(this.assetGroupService.assetGroups(input.getAssetGroups())));

    inject.setTags(fromIterable(tagRepository.findAllById(input.getTagIds())));
    List<InjectDocumentInput> documents = input.getDocuments();
    List<String> askedDocumentIds = documents.stream().map(InjectDocumentInput::getDocumentId).toList();
    List<String> currentDocumentIds = inject.getDocuments().stream().map(document -> document.getDocument().getId())
        .toList();
    // region Set documents
    List<InjectDocument> injectDocuments = inject.getDocuments();
    // To delete
    List<InjectDocument> toRemoveDocuments = inject.getDocuments().stream()
        .filter(injectDoc -> !askedDocumentIds.contains(injectDoc.getDocument().getId()))
        .toList();
    injectDocuments.removeAll(toRemoveDocuments);
    // To add
    documents.stream().filter(doc -> !currentDocumentIds.contains(doc.getDocumentId())).forEach(in -> {
      Optional<Document> doc = documentRepository.findById(in.getDocumentId());
      if (doc.isPresent()) {
        InjectDocument injectDocument = new InjectDocument();
        injectDocument.setInject(inject);
        Document document = doc.get();
        injectDocument.setDocument(document);
        injectDocument.setAttached(in.isAttached());
        InjectDocument savedInjectDoc = injectDocumentRepository.save(injectDocument);
        injectDocuments.add(savedInjectDoc);
        // If Document not yet linked directly to the exercise, attached it
        if (!document.getExercises().contains(exercise)) {
          exercise.getDocuments().add(document);
          exerciseRepository.save(exercise);
        }
      }
    });
    // Remap the attached boolean
    injectDocuments.forEach(injectDoc -> {
      Optional<InjectDocumentInput> inputInjectDoc = input.getDocuments().stream()
          .filter(id -> id.getDocumentId().equals(injectDoc.getDocument().getId())).findFirst();
      Boolean attached = inputInjectDoc.map(InjectDocumentInput::isAttached).orElse(false);
      injectDoc.setAttached(attached);
    });
    inject.setDocuments(injectDocuments);
    // endregion
    return injectRepository.save(inject);
  }

  @GetMapping("/api/exercises/{exerciseId}/injects")
  @PreAuthorize("isExerciseObserver(#exerciseId)")
  public Iterable<Inject> exerciseInjects(@PathVariable String exerciseId) {
    return injectRepository.findAll(InjectSpecification.fromExercise(exerciseId)).stream()
        .sorted(Inject.executionComparator).toList();
  }

  @GetMapping("/api/exercises/{exerciseId}/injects/{injectId}")
  @PreAuthorize("isExerciseObserver(#exerciseId)")
  public Inject exerciseInject(@PathVariable String exerciseId, @PathVariable String injectId) {
    return injectRepository.findById(injectId).orElseThrow();
  }

  @GetMapping("/api/exercises/{exerciseId}/injects/{injectId}/teams")
  @PreAuthorize("isExerciseObserver(#exerciseId)")
  public Iterable<Team> exerciseInjectTeams(@PathVariable String exerciseId, @PathVariable String injectId) {
    return injectRepository.findById(injectId).orElseThrow().getTeams();
  }

  @GetMapping("/api/exercises/{exerciseId}/injects/{injectId}/communications")
  @PreAuthorize("isExerciseObserver(#exerciseId)")
  public Iterable<Communication> exerciseInjectCommunications(@PathVariable String exerciseId,
      @PathVariable String injectId) {
    List<Communication> coms = communicationRepository.findAll(fromInject(injectId),
        Sort.by(Sort.Direction.DESC, "receivedAt"));
    List<Communication> ackComs = coms.stream().peek(com -> com.setAck(true)).toList();
    return communicationRepository.saveAll(ackComs);
  }

  @PostMapping("/api/exercises/{exerciseId}/injects")
  @PreAuthorize("isExercisePlanner(#exerciseId)")
  public Inject createInject(@PathVariable String exerciseId, @Valid @RequestBody InjectInput input) {
    Exercise exercise = exerciseRepository.findById(exerciseId).orElseThrow();
    // Get common attributes
    Inject inject = input.toInject();
    inject.setType(contractService.getContractType(input.getContract()));
    inject.setUser(userRepository.findById(currentUser().getId()).orElseThrow());
    inject.setExercise(exercise);
    // Set dependencies
    inject.setDependsOn(resolveOptionalRelation(input.getDependsOn(), injectRepository));
    inject.setTeams(fromIterable(teamRepository.findAllById(input.getTeams())));
    inject.setTags(fromIterable(tagRepository.findAllById(input.getTagIds())));
    List<InjectDocument> injectDocuments = input.getDocuments().stream()
        .map(i -> {
          InjectDocument injectDocument = new InjectDocument();
          injectDocument.setInject(inject);
          injectDocument.setDocument(documentRepository.findById(i.getDocumentId()).orElseThrow());
          injectDocument.setAttached(i.isAttached());
          return injectDocument;
        }).toList();
    inject.setDocuments(injectDocuments);
    return injectRepository.save(inject);
  }

  @PostMapping("/api/exercises/{exerciseId}/inject")
  @PreAuthorize("isExercisePlanner(#exerciseId)")
  public InjectStatus executeInject(@PathVariable String exerciseId,
      @Valid @RequestPart("input") DirectInjectInput input,
      @RequestPart("file") Optional<MultipartFile> file) {
    Inject inject = input.toInject();
    Contract contract = contractService.resolveContract(inject);
    if (contract == null) {
      throw new UnsupportedOperationException("Unknown inject contract " + inject.getContract());
    }
    inject.setType(contract.getConfig().getType());
    inject.setUser(userRepository.findById(currentUser().getId()).orElseThrow());
    inject.setExercise(exerciseRepository.findById(exerciseId).orElseThrow());
    Iterable<User> users = userRepository.findAllById(input.getUserIds());
    List<ExecutionContext> userInjectContexts = fromIterable(users).stream()
        .map(user -> this.executionContextService.executionContext(user, inject, "Direct execution")).toList();
    ExecutableInject injection = new ExecutableInject(true, true, inject, contract, List.of(), inject.getAssets(), inject.getAssetGroups(), userInjectContexts);
    file.ifPresent(injection::addDirectAttachment);
    Injector executor = context.getBean(contract.getConfig().getType(), Injector.class);
    Execution execution = executor.executeInjection(injection);
    return InjectStatus.fromExecution(execution, inject);
  }

  @Transactional(rollbackOn = Exception.class)
  @DeleteMapping("/api/exercises/{exerciseId}/injects/{injectId}")
  @PreAuthorize("isExercisePlanner(#exerciseId)")
  public void deleteInject(@PathVariable String exerciseId, @PathVariable String injectId) {
    injectDocumentRepository.deleteDocumentsFromInject(injectId);
    injectRepository.deleteById(injectId);
  }

  @PutMapping("/api/exercises/{exerciseId}/injects/{injectId}/activation")
  @PreAuthorize("isExercisePlanner(#exerciseId)")
  public Inject updateInjectActivation(@PathVariable String exerciseId, @PathVariable String injectId,
      @Valid @RequestBody InjectUpdateActivationInput input) {
    Inject inject = injectRepository.findById(injectId).orElseThrow();
    inject.setEnabled(input.isEnabled());
    inject.setUpdatedAt(now());
    return injectRepository.save(inject);
  }

  @PutMapping("/api/exercises/{exerciseId}/injects/{injectId}/trigger")
  @PreAuthorize("isExercisePlanner(#exerciseId)")
  public Inject updateInjectActivation(@PathVariable String exerciseId, @PathVariable String injectId,
      @Valid @RequestBody InjectUpdateTriggerInput input) {
    Inject inject = injectRepository.findById(injectId).orElseThrow();
    inject.setDependsDuration(input.getDependsDuration());
    inject.setUpdatedAt(now());
    return injectRepository.save(inject);
  }

  @Transactional(rollbackOn = Exception.class)
  @PostMapping("/api/exercises/{exerciseId}/injects/{injectId}/status")
  @PreAuthorize("isExercisePlanner(#exerciseId)")
  public Inject setInjectStatus(@PathVariable String exerciseId, @PathVariable String injectId,
      @Valid @RequestBody InjectUpdateStatusInput input) {
    Inject inject = injectRepository.findById(injectId).orElseThrow();
    // build status
    InjectStatus injectStatus = new InjectStatus();
    injectStatus.setInject(inject);
    injectStatus.setDate(now());
    injectStatus.setName(input.getStatus());
    injectStatus.setExecutionTime(0);
    Execution execution = new Execution(false);
    execution.addTrace(traceSuccess(currentUser().getId(), input.getMessage()));
    execution.stop();
    injectStatus.setReporting(execution);
    // Save status for inject
    inject.setStatus(injectStatus);
    return injectRepository.save(inject);
  }

  @PutMapping("/api/exercises/{exerciseId}/injects/{injectId}/teams")
  @PreAuthorize("isExercisePlanner(#exerciseId)")
  public Inject updateInjectTeams(@PathVariable String exerciseId, @PathVariable String injectId,
      @Valid @RequestBody InjectTeamsInput input) {
    Inject inject = injectRepository.findById(injectId).orElseThrow();
    Iterable<Team> injectTeams = teamRepository.findAllById(input.getTeamIds());
    inject.setTeams(fromIterable(injectTeams));
    return injectRepository.save(inject);
  }

  @GetMapping("/api/injects/next")
  public List<Inject> nextInjectsToExecute(@RequestParam Optional<Integer> size) {
    return injectRepository.findAll(InjectSpecification.next()).stream()
        // Keep only injects visible by the user
        .filter(inject -> inject.getDate().isPresent())
        .filter(inject -> inject.getExercise()
            .isUserHasAccess(userRepository.findById(currentUser().getId()).orElseThrow()))
        // Order by near execution
        .sorted(Inject.executionComparator)
        // Keep only the expected size
        .limit(size.orElse(MAX_NEXT_INJECTS))
        // Collect the result
        .toList();
  }
}
