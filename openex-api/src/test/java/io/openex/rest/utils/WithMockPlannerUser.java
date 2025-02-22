package io.openex.rest.utils;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static io.openex.rest.utils.WithMockPlannerUserSecurityContextFactory.MOCK_USER_PLANNER_EMAIL;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockPlannerUserSecurityContextFactory.class)
public @interface WithMockPlannerUser {
    String email() default MOCK_USER_PLANNER_EMAIL;
}
