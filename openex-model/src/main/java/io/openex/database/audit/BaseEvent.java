package io.openex.database.audit;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.openex.database.model.Base;
import lombok.Getter;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

@Getter
public class BaseEvent implements Cloneable {

    @JsonIgnore
    private final String sessionId;
    @JsonIgnore
    private final Base instance;
    @JsonProperty("event_type")
    private String type;
    @JsonProperty("attribute_id")
    private String attributeId;
    @JsonProperty("attribute_schema")
    private String schema;
    @JsonProperty("instance")
    private JsonNode instanceData;

    public BaseEvent(String type, Base data, ObjectMapper mapper) {
        this.type = type;
        this.instance = data;
        this.instanceData = mapper.valueToTree(instance);
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        this.sessionId = requestAttributes != null ? requestAttributes.getSessionId() : null;
        Class<? extends Base> currentClass = data.getClass();
        boolean isTargetClass = currentClass.getSuperclass().equals(Object.class);
        Class<?> baseClass = isTargetClass ? currentClass : currentClass.getSuperclass();
        String className = baseClass.getSimpleName().toLowerCase();
        this.attributeId = className + "_id";
        this.schema = className + (className.endsWith("s") ? "es" : "s");
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setAttributeId(String attributeId) {
        this.attributeId = attributeId;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public void setInstanceData(JsonNode instanceData) {
        this.instanceData = instanceData;
    }

    @JsonIgnore
    public boolean isUserObserver(final boolean isAdmin) {
        return this.instance.isUserHasAccess(isAdmin);
    }

    @Override
    public BaseEvent clone() {
        try {
            return (BaseEvent) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
