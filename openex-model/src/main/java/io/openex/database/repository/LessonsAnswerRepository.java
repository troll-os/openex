package io.openex.database.repository;

import io.openex.database.model.LessonsAnswer;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import jakarta.validation.constraints.NotNull;
import java.util.Optional;

@Repository
public interface LessonsAnswerRepository extends CrudRepository<LessonsAnswer, String>, JpaSpecificationExecutor<LessonsAnswer> {

    @NotNull
    Optional<LessonsAnswer> findById(@NotNull String id);
}
