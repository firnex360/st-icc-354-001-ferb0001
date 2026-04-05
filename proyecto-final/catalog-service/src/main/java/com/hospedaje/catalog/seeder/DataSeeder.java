package com.hospedaje.catalog.seeder;

import com.hospedaje.catalog.model.Property;
import com.hospedaje.catalog.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Startup component that seeds the MongoDB {@code properties} collection
 * with realistic mock data using the Datafaker library.
 * <p>
 * Only runs when the collection is empty (first startup / clean DB).
 * Generates 20 diverse property listings.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final PropertyRepository propertyRepository;

    /** Number of mock properties to generate. */
    private static final int SEED_COUNT = 20;

    /** Available room types in the platform. */
    private static final String[] ROOM_TYPES = {
            "SINGLE", "DOUBLE", "SUITE", "PENTHOUSE"
    };

    /** Pool of amenities to randomly assign. */
    private static final String[] AMENITY_POOL = {
            "WiFi", "Pool", "Gym", "Spa", "Restaurant", "Bar",
            "Room Service", "Parking", "Airport Shuttle", "Beach Access",
            "Air Conditioning", "Mini Bar", "Balcony", "Ocean View",
            "Pet Friendly", "Business Center", "Laundry Service"
    };

    /** Placeholder image URLs for mock data. */
    private static final String[] IMAGE_URLS = {
            "https://images.unsplash.com/photo-1566073771259-6a8506099945",
            "https://images.unsplash.com/photo-1582719508461-905c673771fd",
            "https://images.unsplash.com/photo-1551882547-ff40c63fe5fa",
            "https://images.unsplash.com/photo-1520250497591-112f2f40a3f4",
            "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb"
    };

    @Override
    public void run(String... args) {
        // Only seed if the collection is empty
        if (propertyRepository.count() > 0) {
            log.info("Properties collection already has data — skipping seed.");
            return;
        }

        log.info("Seeding {} mock properties into MongoDB...", SEED_COUNT);
        Faker faker = new Faker();
        List<Property> properties = new ArrayList<>();

        for (int i = 0; i < SEED_COUNT; i++) {
            properties.add(generateProperty(faker));
        }

        propertyRepository.saveAll(properties);
        log.info("Successfully seeded {} properties.", SEED_COUNT);
    }

    /**
     * Generates a single randomized property document.
     */
    private Property generateProperty(Faker faker) {
        // Random availability window: starts 1-30 days from now, lasts 30-180 days
        LocalDate from = LocalDate.now()
                .plusDays(ThreadLocalRandom.current().nextInt(1, 31));
        LocalDate to = from
                .plusDays(ThreadLocalRandom.current().nextInt(30, 181));

        return Property.builder()
                .name(faker.company().name() + " " + faker.address().cityName() + " Hotel")
                .description(faker.lorem().paragraph(3))
                .location(faker.address().city() + ", " + faker.address().country())
                .roomType(ROOM_TYPES[ThreadLocalRandom.current().nextInt(ROOM_TYPES.length)])
                .amenities(pickRandomAmenities())
                .imageUrls(pickRandomImages())
                .pricePerNight(roundToTwo(50.0 + ThreadLocalRandom.current().nextDouble(450.0)))
                .availableFrom(from)
                .availableTo(to)
                .maxGuests(ThreadLocalRandom.current().nextInt(1, 7))
                .active(true)
                .build();
    }

    /**
     * Randomly selects 3 to 7 amenities from the pool.
     */
    private List<String> pickRandomAmenities() {
        int count = ThreadLocalRandom.current().nextInt(3, 8);
        List<String> shuffled = new ArrayList<>(List.of(AMENITY_POOL));
        java.util.Collections.shuffle(shuffled);
        return shuffled.subList(0, count);
    }

    /**
     * Randomly selects 1 to 3 placeholder images.
     */
    private List<String> pickRandomImages() {
        int count = ThreadLocalRandom.current().nextInt(1, 4);
        List<String> shuffled = new ArrayList<>(List.of(IMAGE_URLS));
        java.util.Collections.shuffle(shuffled);
        return shuffled.subList(0, count);
    }

    /**
     * Rounds a double value to two decimal places.
     */
    private double roundToTwo(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
