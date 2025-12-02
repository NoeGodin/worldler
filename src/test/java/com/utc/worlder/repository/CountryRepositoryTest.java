package com.utc.worlder.repository;

import com.utc.worlder.config.AbstractTestBase;
import com.utc.worlder.entity.Country;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayName("Country Repository Integration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CountryRepositoryTest extends AbstractTestBase {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CountryRepository countryRepository;

    private Country savedFrance;

    @BeforeEach
    void setUp() {
        savedFrance = entityManager.persistAndFlush(createTestCountry("France", "FRA"));
        
        Country spain = createTestCountry("Spain", "ESP");
        spain.setContinent("Europe");
        spain.setPopulation(47_000_000L);
        spain.setArea(505_992.0);
        entityManager.persistAndFlush(spain);
        
        Country germany = createTestCountry("Germany", "DEU");
        germany.setContinent("Europe");
        germany.setPopulation(83_000_000L);
        germany.setArea(357_592.0);
        entityManager.persistAndFlush(germany);
        
        entityManager.clear();
    }

    @Nested
    @DisplayName("Find Operations")
    class FindOperations {

        @Test
        @Order(1)
        @DisplayName("Should find country by exact name")
        @Tag("integration")
        void shouldFindCountryByExactName() {
            Optional<Country> result = countryRepository.findByName("France");

            assertThat(result)
                .isPresent()
                .get()
                .satisfies(country -> {
                    assertThat(country.getName()).isEqualTo("France");
                    assertThat(country.getIsoCode()).isEqualTo("FRA");
                    assertThat(country.getId()).isEqualTo(savedFrance.getId());
                });
        }

        @Test
        @Order(2)
        @DisplayName("Should return empty when country name not found")
        @Tag("integration")
        void shouldReturnEmpty_WhenCountryNameNotFound() {
            Optional<Country> result = countryRepository.findByName("NonExistentCountry");

            assertThat(result).isEmpty();
        }

        @Test
        @Order(3)
        @DisplayName("Should find country by ISO code")
        @Tag("integration")
        void shouldFindCountryByIsoCode() {
            Optional<Country> result = countryRepository.findByIsoCode("DEU");

            assertThat(result)
                .isPresent()
                .get()
                .satisfies(country -> {
                    assertThat(country.getName()).isEqualTo("Germany");
                    assertThat(country.getIsoCode()).isEqualTo("DEU");
                });
        }

        @ParameterizedTest
        @MethodSource("continentTestData")
        @DisplayName("Should find countries by continent")
        @Tag("integration")
        void shouldFindCountriesByContinent(String continent, int expectedCount) {
            List<Country> result = countryRepository.findByContinent(continent);

            assertThat(result)
                .hasSize(expectedCount)
                .allSatisfy(country -> 
                    assertThat(country.getContinent()).isEqualTo(continent));
        }

        static Stream<Arguments> continentTestData() {
            return Stream.of(
                Arguments.of("Europe", 3),
                Arguments.of("Asia", 0),
                Arguments.of("NonExistent", 0)
            );
        }
    }

    @Nested
    @DisplayName("Custom Query Operations")
    class CustomQueryOperations {

        @Test
        @DisplayName("Should find countries with population greater than threshold")
        @Tag("integration")
        void shouldFindCountriesWithHighPopulation() {
            Long populationThreshold = 10_000_000L;

            List<Country> result = countryRepository.findCountriesWithPopulationGreaterThan(populationThreshold);

            assertThat(result)
                .isNotEmpty()
                .allSatisfy(country -> 
                    assertThat(country.getPopulation()).isGreaterThan(populationThreshold))
                .extracting(Country::getName)
                .contains("Spain");
        }

        @Test
        @DisplayName("Should find countries with area greater than threshold")
        @Tag("integration")
        void shouldFindCountriesWithLargeArea() {
            Double areaThreshold = 400_000.0;

            List<Country> result = countryRepository.findCountriesWithAreaGreaterThan(areaThreshold);

            assertThat(result)
                .isNotEmpty()
                .allSatisfy(country -> 
                    assertThat(country.getArea()).isGreaterThan(areaThreshold))
                .extracting(Country::getName)
                .contains("Spain");
        }

        @Test
        @DisplayName("Should find all unique continents")
        @Tag("integration")
        void shouldFindAllUniqueContinents() {
            List<String> result = countryRepository.findAllContinents();

            assertThat(result)
                .isNotEmpty()
                .contains("Europe")
                .doesNotHaveDuplicates();
        }
    }

    @Nested
    @DisplayName("Persistence Constraints")
    class PersistenceConstraints {

        @Test
        @DisplayName("Should enforce unique country name constraint")
        @Tag("integration")
        void shouldEnforceUniqueNameConstraint() {
            Country duplicateCountry = createTestCountry("France", "FRX");

            assertThatThrownBy(() -> {
                entityManager.persist(duplicateCountry);
                entityManager.flush();
            }).isInstanceOfAny(DataIntegrityViolationException.class, 
                              jakarta.persistence.PersistenceException.class,
                              org.springframework.dao.DataAccessException.class);
        }

        @Test
        @DisplayName("Should enforce unique ISO code constraint")
        @Tag("integration")
        void shouldEnforceUniqueIsoCodeConstraint() {
            Country duplicateIsoCountry = createTestCountry("New France", "FRA");

            assertThatThrownBy(() -> {
                entityManager.persist(duplicateIsoCountry);
                entityManager.flush();
            }).isInstanceOfAny(DataIntegrityViolationException.class, 
                              jakarta.persistence.PersistenceException.class,
                              org.springframework.dao.DataAccessException.class);
        }

        @Test
        @DisplayName("Should save and retrieve country with all fields")
        @Tag("integration")
        void shouldSaveAndRetrieveCountryWithAllFields() {
            Country newCountry = new Country(
                "Test Country",
                "TSC",
                "Test Capital",
                "Test Continent",
                12_345_678L,
                987_654.32,
                "Test Currency",
                "Test Language"
            );

            Country saved = countryRepository.save(newCountry);
            entityManager.flush();
            entityManager.clear();
            
            Optional<Country> retrieved = countryRepository.findById(saved.getId());

            assertThat(retrieved)
                .isPresent()
                .get()
                .satisfies(country -> {
                    assertThat(country.getName()).isEqualTo("Test Country");
                    assertThat(country.getIsoCode()).isEqualTo("TSC");
                    assertThat(country.getCapital()).isEqualTo("Test Capital");
                    assertThat(country.getContinent()).isEqualTo("Test Continent");
                    assertThat(country.getPopulation()).isEqualTo(12_345_678L);
                    assertThat(country.getArea()).isEqualTo(987_654.32);
                    assertThat(country.getCurrency()).isEqualTo("Test Currency");
                    assertThat(country.getOfficialLanguage()).isEqualTo("Test Language");
                });
        }
    }

    @Nested
    @DisplayName("Statistics Operations")
    class StatisticsOperations {

        @Test
        @DisplayName("Should count total countries correctly")
        @Tag("integration")
        void shouldCountTotalCountries() {
            long count = countryRepository.count();

            assertThat(count).isEqualTo(3L);
        }

        @Test
        @DisplayName("Should check if country exists by ID")
        @Tag("integration")
        void shouldCheckIfCountryExistsById() {
            assertThat(countryRepository.existsById(savedFrance.getId())).isTrue();
            assertThat(countryRepository.existsById(999L)).isFalse();
        }

        @Test
        @DisplayName("Should delete country and verify removal")
        @Tag("integration")
        void shouldDeleteCountryAndVerifyRemoval() {
            Long countryIdToDelete = savedFrance.getId();
            long initialCount = countryRepository.count();

            countryRepository.deleteById(countryIdToDelete);
            entityManager.flush();

            assertThat(countryRepository.existsById(countryIdToDelete)).isFalse();
            assertThat(countryRepository.count()).isEqualTo(initialCount - 1);
        }
    }

    @AfterEach
    void tearDown(TestInfo testInfo) {
        logTestDuration(testInfo);
    }
}