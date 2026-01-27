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
  private static final String LOG_CREATE_FAIL = "Failed to create legacy record for store=%s";
  private static final String LOG_UPDATE_FAIL = "Failed to update legacy record for store=%s";
  private static final String LOG_WRITE_NULL = "writeToFile called with null store";
  private static final String LOG_TEMP_CREATED = "Temporary file created at: %s";
  private static final String LOG_DATA_WRITTEN = "Data written to temporary file.";
  private static final String LOG_DATA_READ = "Data read from temporary file: %s";
  private static final String LOG_TEMP_DELETED = "Temporary file deleted: %s";
  private static final String STORE_NULL_NAME = "<null>";
  private static final String DEFAULT_STORE_NAME = "store";
  private static final String SAFE_NAME_REGEX = "[^a-zA-Z0-9_-]";
  private static final String SAFE_NAME_REPLACEMENT = "_";
  private static final String PREFIX_STORE = "store-";
  private static final String PREFIX_TMP = "tmp";
  private static final String TEMP_FILE_SUFFIX = ".txt";
  private static final String LEGACY_CONTENT_TEMPLATE =
      "Store created. [ name = %s ] [ items on stock = %d ]";

  public void createStoreOnLegacySystem(Store store) {
    // emulate a legacy side-effect; failures are logged but don't bubble up
    try {
      writeToFile(store);
    } catch (IOException e) {
      LOGGER.errorf(e, LOG_CREATE_FAIL, store == null ? STORE_NULL_NAME : store.name);
    }
  }

  public void updateStoreOnLegacySystem(Store store) {
    try {
      writeToFile(store);
    } catch (IOException e) {
      LOGGER.errorf(e, LOG_UPDATE_FAIL, store == null ? STORE_NULL_NAME : store.name);
    }
  }

  private void writeToFile(Store store) throws IOException {
    if (store == null) {
      LOGGER.warn(LOG_WRITE_NULL);
      return;
    }

    // make a safe prefix for the temporary file name (must be at least 3 chars)
    String safeName = store.name == null
      ? DEFAULT_STORE_NAME
      : store.name.replaceAll(SAFE_NAME_REGEX, SAFE_NAME_REPLACEMENT);
    String prefix = PREFIX_STORE + (safeName.length() >= 3
      ? safeName.substring(0, Math.min(10, safeName.length()))
      : PREFIX_TMP);

    Path tempFile = Files.createTempFile(prefix, TEMP_FILE_SUFFIX);
    LOGGER.debugf(LOG_TEMP_CREATED, tempFile.toString());

    String content = String.format(
      LEGACY_CONTENT_TEMPLATE,
      store.name,
      store.quantityProductsInStock
    );

    Files.writeString(tempFile, content, StandardCharsets.UTF_8);
    LOGGER.debug(LOG_DATA_WRITTEN);

    String readContent = Files.readString(tempFile, StandardCharsets.UTF_8);
    LOGGER.debugf(LOG_DATA_READ, readContent);

    Files.deleteIfExists(tempFile);
    LOGGER.debugf(LOG_TEMP_DELETED, tempFile.toString());
  }
}
