package io.openex.execution;

import io.openex.contract.Contract;
import io.openex.database.model.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ExecutableInject {

  private final Inject inject;
  private final Contract contract;
  private final Injection source;
  private final List<ExecutionContext> contextUser;
  private final List<Team> teams;
  private final List<Asset> assets;
  private final List<AssetGroup> assetGroups;
  private final boolean runtime;
  private final boolean direct;
  private final int teamSize;
  private final int documentSize;
  @Setter
  private List<MultipartFile> directAttachments = new ArrayList<>();

  public ExecutableInject(boolean runtime, boolean direct,
      Injection source, Inject inject, Contract contract,
      List<Team> teams, List<Asset> assets, List<AssetGroup> assetGroups, List<ExecutionContext> contextUser) {
    this.runtime = runtime;
    this.direct = direct;
    this.source = source;
    this.inject = inject;
    this.contract = contract;
    this.contextUser = contextUser;
    this.teams = teams;
    this.assets = assets;
    this.assetGroups = assetGroups;
    this.teamSize = teams.size();
    this.documentSize = inject.getDocuments().size();
  }

  public ExecutableInject(boolean runtime, boolean direct,
      Inject inject, Contract contract,
      List<Team> teams, List<Asset> assets, List<AssetGroup> assetGroups, List<ExecutionContext> contextUser) {
    this(runtime, direct, inject, inject, contract, teams, assets, assetGroups, contextUser);
  }

  public ExecutableInject(boolean runtime, boolean direct,
      Inject inject, Contract contract, List<ExecutionContext> contextUser) {
    this(runtime, direct, inject, inject, contract, List.of(), List.of(), List.of(), contextUser);
  }

  public void addDirectAttachment(MultipartFile file) {
    this.directAttachments.add(file);
  }

}
