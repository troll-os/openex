package io.openex.database.repository;

import io.openex.database.model.User;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<User, String>, JpaSpecificationExecutor<User>,
    StatisticRepository {

  @NotNull
  Optional<User> findById(@NotNull String id);

  Optional<User> findByEmailIgnoreCase(String email);

  List<User> findAllByEmailInIgnoreCase(List<String> emails);

  @Query("select user from User user where user.organization is null or user.organization.id in :organizationIds")
  List<User> usersAccessibleFromOrganizations(@Param("organizationIds") List<String> organizationIds);

  @Override
  @Query("select count(distinct u) from User u " +
      "join u.teams as team " +
      "join team.exercises as e " +
      "join e.grants as grant " +
      "join grant.group.users as user " +
      "where user.id = :userId and u.createdAt < :creationDate")
  long userCount(String userId, Instant creationDate);

  @Override
  @Query("select count(distinct u) from User u where u.createdAt < :creationDate")
  long globalCount(Instant creationDate);

  // -- ADMIN --

  // Custom query to bypass ID generator on User property
  @Modifying
  @Query(value = "insert into users(user_id, user_firstname, user_lastname, user_email, user_password, user_admin, user_status) "
      + "values (:id, :firstname, :lastName, :email, :password, true, 1)", nativeQuery = true)
  void createAdmin(
      @Param("id") String userId,
      @Param("firstname") String userFirstName,
      @Param("lastName") String userLastName,
      @Param("email") String userEmail,
      @Param("password") String userPassword
  );
}
