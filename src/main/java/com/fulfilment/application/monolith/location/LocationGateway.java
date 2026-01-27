package com.fulfilment.application.monolith.location;

import com.fulfilment.application.monolith.common.AppConstants;
import com.fulfilment.application.monolith.common.exceptions.NotFoundException;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class LocationGateway implements LocationResolver {

  private static final String LOCATION_ZWOLLE_001 = "ZWOLLE-001";
  private static final String LOCATION_ZWOLLE_002 = "ZWOLLE-002";
  private static final String LOCATION_AMSTERDAM_001 = "AMSTERDAM-001";
  private static final String LOCATION_AMSTERDAM_002 = "AMSTERDAM-002";
  private static final String LOCATION_TILBURG_001 = "TILBURG-001";
  private static final String LOCATION_HELMOND_001 = "HELMOND-001";
  private static final String LOCATION_EINDHOVEN_001 = "EINDHOVEN-001";
  private static final String LOCATION_VETSBY_001 = "VETSBY-001";

  private static final List<Location> locations = new ArrayList<>();

  static {
    locations.add(new Location(LOCATION_ZWOLLE_001, 1, 40));
    locations.add(new Location(LOCATION_ZWOLLE_002, 2, 50));
    locations.add(new Location(LOCATION_AMSTERDAM_001, 5, 100));
    locations.add(new Location(LOCATION_AMSTERDAM_002, 3, 75));
    locations.add(new Location(LOCATION_TILBURG_001, 1, 40));
    locations.add(new Location(LOCATION_HELMOND_001, 1, 45));
    locations.add(new Location(LOCATION_EINDHOVEN_001, 2, 70));
    locations.add(new Location(LOCATION_VETSBY_001, 1, 90));
  }

  @Override
  public Location resolveByIdentifier(String identifier) {
	  return locations.stream()
			  .filter(location -> location.identification.equals(identifier))
              .findFirst()
              .orElseThrow(() ->
            new NotFoundException(
              String.format(AppConstants.ERR_LOCATION_NOT_FOUND, identifier)
            )
              );
  }

}
