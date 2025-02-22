package io.openex.service;

import io.openex.database.model.Asset;
import io.openex.database.model.AssetGroup;
import io.openex.database.repository.AssetGroupRepository;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static io.openex.helper.StreamHelper.fromIterable;
import static java.time.Instant.now;

@RequiredArgsConstructor
@Service
public class AssetGroupService {

  private final AssetGroupRepository assetGroupRepository;
  private final AssetService assetService;

  // -- ASSET GROUP --

  public AssetGroup createAssetGroup(@NotNull final AssetGroup assetGroup) {
    return this.assetGroupRepository.save(assetGroup);
  }

  public List<AssetGroup> assetGroups() {
    return fromIterable(this.assetGroupRepository.findAll());
  }

  public List<AssetGroup> assetGroups(@NotBlank final List<String> assetGroupIds) {
    return fromIterable(this.assetGroupRepository.findAllById(assetGroupIds));
  }

  public AssetGroup assetGroup(@NotBlank final String assetGroupId) {
    return this.assetGroupRepository.findById(assetGroupId).orElseThrow();
  }

  public AssetGroup updateAssetGroup(@NotNull final AssetGroup assetGroup) {
    assetGroup.setUpdatedAt(now());
    return this.assetGroupRepository.save(assetGroup);
  }

  public AssetGroup updateAssetsOnAssetGroup(
      @NotNull final AssetGroup assetGroup,
      @NotNull final List<String> assetIds) {
    Iterable<Asset> assets = this.assetService.assetFromIds(assetIds);
    assetGroup.setAssets(fromIterable(assets));
    assetGroup.setUpdatedAt(now());
    return this.assetGroupRepository.save(assetGroup);
  }

  public void deleteAssetGroup(@NotBlank final String assetGroupId) {
    this.assetGroupRepository.deleteById(assetGroupId);
  }

  // -- ASSET --

  @Transactional(readOnly=true)
  public List<Asset> assetsFromAssetGroup(@NotBlank final String assetGroupId) {
    return this.assetGroupRepository.assetsFromAssetGroup(assetGroupId);
  }

}
