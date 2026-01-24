package com.fulfilment.application.monolith.stores;

import jakarta.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.jboss.logging.Logger;

@ApplicationScoped
public class LegacyStoreManagerGateway {

  private static final Logger LOGGER = Logger.getLogger(LegacyStoreManagerGateway.class);

  public void createStoreOnLegacySystem(Store store) {
    // emulate a legacy side-effect; failures are logged but don't bubble up
    try {
      writeToFile(store);
    } catch (IOException e) {
      LOGGER.errorf(e, "Failed to create legacy record for store=%s", store == null ? "<null>" : store.name);
    }
  }

  public void updateStoreOnLegacySystem(Store store) {
    try {
      writeToFile(store);
    } catch (IOException e) {
      LOGGER.errorf(e, "Failed to update legacy record for store=%s", store == null ? "<null>" : store.name);
    }
  }

  private void writeToFile(Store store) throws IOException {
    if (store == null) {
      LOGGER.warn("writeToFile called with null store");
      return;
    }

    // make a safe prefix for the temporary file name (must be at least 3 chars)
    String safeName = store.name == null ? "store" : store.name.replaceAll("[^a-zA-Z0-9_-]", "_");
    String prefix = "store-" + (safeName.length() >= 3 ? safeName.substring(0, Math.min(10, safeName.length())) : "tmp");

    Path tempFile = Files.createTempFile(prefix, ".txt");
    LOGGER.debugf("Temporary file created at: %s", tempFile.toString());

    String content = String.format("Store created. [ name = %s ] [ items on stock = %d ]", store.name, store.quantityProductsInStock);

    Files.writeString(tempFile, content, StandardCharsets.UTF_8);
    LOGGER.debug("Data written to temporary file.");

    String readContent = Files.readString(tempFile, StandardCharsets.UTF_8);
    LOGGER.debugf("Data read from temporary file: %s", readContent);

    Files.deleteIfExists(tempFile);
    LOGGER.debugf("Temporary file deleted: %s", tempFile.toString());
  }
}
