package io.openex.database.repository;

import io.openex.database.model.LessonsQuestion;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import jakarta.validation.constraints.NotNull;
import java.util.Optional;

@Repository
public interface LessonsQuestionRepository extends CrudRepository<LessonsQuestion, String>, JpaSpecificationExecutor<LessonsQuestion> {

    @NotNull
    Optional<LessonsQuestion> findById(@NotNull String id);
}
