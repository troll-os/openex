package io.openex.service;

import io.openex.database.model.Asset;
import io.openex.database.model.Endpoint;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.TransactionSystemException;

import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;

import static io.openex.database.model.Endpoint.PLATFORM_TYPE.Linux;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AssetEndpointServiceTest {

  @Autowired
  private AssetEndpointService assetEndpointService;
  @Autowired
  private AssetService assetService;

  static String ENDPOINT_ID;

  @DisplayName("Create endpoint failed")
  @Test
  @Order(1)
  void createEndpointFailedTest() {
    // -- PREPARE --
    Endpoint endpoint = new Endpoint();
    String name = "Personal PC";
    endpoint.setName(name);
    endpoint.setIps(new String[]{"wrong ip"});
    endpoint.setHostname("hostname");
    endpoint.setPlatform(Linux);
    endpoint.setHostname("hostname");
    endpoint.setPlatform(Linux);

    // -- EXECUTE --
    assertThrows(TransactionSystemException.class, () -> this.assetEndpointService.createEndpoint(endpoint));
  }

  @DisplayName("Create endpoint succeed")
  @Test
  @Order(2)
  void createEndpointSucceedTest() {
    // -- PREPARE --
    Endpoint endpoint = new Endpoint();
    String name = "Personal PC";
    endpoint.setName(name);
    endpoint.setIps(new String[]{"127.0.0.1"});
    endpoint.setHostname("hostname");
    endpoint.setPlatform(Linux);
    endpoint.setHostname("hostname");
    endpoint.setPlatform(Linux);
    endpoint.setSources(new HashMap<>(){{
      put("Manual", "manual");
      put("Caldera", "caldera-id");
    }});

    // -- EXECUTE --
    Endpoint endpointCreated = this.assetEndpointService.createEndpoint(endpoint);
    ENDPOINT_ID = endpointCreated.getId();
    assertNotNull(endpointCreated);
    assertNotNull(endpointCreated.getId());
    assertNotNull(endpointCreated.getCreatedAt());
    assertNotNull(endpointCreated.getUpdatedAt());
    assertEquals(name, endpointCreated.getName());
  }

  @DisplayName("Retrieve endpoint")
  @Test
  @Order(3)
  void retrieveEndpointTest() {
    List<Asset> assets = this.assetService.assetsFromTypes(List.of("Endpoint"));
    assertTrue(assets.stream().map(Asset::getId).toList().contains(ENDPOINT_ID));

    Endpoint endpoint = this.assetEndpointService.endpoint(ENDPOINT_ID);
    assertNotNull(endpoint);

    List<Endpoint> endpoints = this.assetEndpointService.endpoints();
    assertNotNull(endpoints);
    assertTrue(endpoints.stream().map(Endpoint::getId).toList().contains(ENDPOINT_ID));
  }

  @DisplayName("Update endpoint")
  @Test
  @Order(4)
  void updateEndpointTest() {
    // -- PREPARE --
    Endpoint endpoint = this.assetEndpointService.endpoint(ENDPOINT_ID);
    String value = "Professional PC";
    endpoint.setName(value);

    // -- EXECUTE --
    Endpoint endpointUpdated = this.assetEndpointService.updateEndpoint(endpoint);
    assertNotNull(endpoint);
    assertEquals(value, endpointUpdated.getName());
  }

  @DisplayName("Delete endpoint")
  @Test
  @Order(5)
  void deleteEndpointTest() {
    this.assetEndpointService.deleteEndpoint(ENDPOINT_ID);
    assertThrows(NoSuchElementException.class, () -> this.assetEndpointService.endpoint(ENDPOINT_ID));
  }

}
