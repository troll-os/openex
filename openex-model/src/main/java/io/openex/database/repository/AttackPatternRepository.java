package io.openex.database.repository;

import io.openex.database.model.AttackPattern;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import jakarta.validation.constraints.NotNull;

import javax.swing.text.html.Option;
import java.util.Optional;

@Repository
public interface AttackPatternRepository extends CrudRepository<AttackPattern, String>, JpaSpecificationExecutor<AttackPattern> {

    @NotNull
    Optional<AttackPattern> findById(@NotNull String id);

    Optional<AttackPattern> findByExternalId(@NotNull String externalId);

    Optional<AttackPattern> findByStixId(@NotNull String stixId);
}
