package com.utc.worlder.service;

import com.utc.worlder.config.AbstractTestBase;
import com.utc.worlder.entity.Country;
import com.utc.worlder.repository.CountryRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Country Service Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CountryServiceTest extends AbstractTestBase {

    @Mock
    private CountryRepository countryRepository;

    @InjectMocks
    private CountryService countryService;

    @Nested
    @DisplayName("Basic CRUD Operations")
    class BasicCrudOperations {

        @Test
        @Order(1)
        @DisplayName("Should return all countries when repository has data")
        @Tag("unit")
        void shouldReturnAllCountries_WhenRepositoryHasData() {
            Country france = createTestCountry("France", "FRA");
            Country germany = createTestCountry("Germany", "DEU");
            List<Country> expectedCountries = Arrays.asList(france, germany);
            
            given(countryRepository.findAll()).willReturn(expectedCountries);

            List<Country> actualCountries = countryService.getAllCountries();

            assertThat(actualCountries)
                .hasSize(2)
                .containsExactlyElementsOf(expectedCountries);

            verify(countryRepository, times(1)).findAll();
            verifyNoMoreInteractions(countryRepository);
        }

        @Test
        @Order(2)
        @DisplayName("Should return empty list when no countries exist")
        @Tag("unit")
        void shouldReturnEmptyList_WhenNoCountriesExist() {
            given(countryRepository.findAll()).willReturn(Arrays.asList());

            List<Country> countries = countryService.getAllCountries();

            assertThat(countries).isEmpty();
            verify(countryRepository).findAll();
        }

        @Test
        @Order(3)
        @DisplayName("Should return country when found by valid ID")
        @Tag("unit")
        void shouldReturnCountry_WhenFoundByValidId() {
            Long countryId = 1L;
            Country expectedCountry = createTestCountry("France", "FRA");
            given(countryRepository.findById(countryId)).willReturn(Optional.of(expectedCountry));

            Optional<Country> actualCountry = countryService.getCountryById(countryId);

            assertThat(actualCountry)
                .isPresent()
                .contains(expectedCountry);

            verify(countryRepository).findById(countryId);
        }

        @Test
        @Order(4)
        @DisplayName("Should return empty when country not found by ID")
        @Tag("unit")
        void shouldReturnEmpty_WhenCountryNotFoundById() {
            Long nonExistentId = 999L;
            given(countryRepository.findById(nonExistentId)).willReturn(Optional.empty());

            Optional<Country> result = countryService.getCountryById(nonExistentId);

            assertThat(result).isEmpty();
            verify(countryRepository).findById(nonExistentId);
        }
    }

    @Nested
    @DisplayName("Search Operations")
    class SearchOperations {

        @ParameterizedTest(name = "Should find country with name: ''{0}''")
        @ValueSource(strings = {"France", "Germany", "United Kingdom"})
        @DisplayName("Should find countries by valid names")
        @Tag("parameterized")
        void shouldFindCountryByName(String countryName) {
            Country expectedCountry = createTestCountry(countryName, "TST");
            given(countryRepository.findByName(countryName)).willReturn(Optional.of(expectedCountry));

            Optional<Country> result = countryService.getCountryByName(countryName);

            assertThat(result)
                .isPresent()
                .get()
                .extracting(Country::getName)
                .isEqualTo(countryName);
        }

        @ParameterizedTest
        @CsvSource({
            "FRA, France",
            "DEU, Germany",
            "GBR, United Kingdom",
            "USA, United States"
        })
        @DisplayName("Should find countries by ISO codes")
        @Tag("parameterized")
        void shouldFindCountryByIsoCode(String isoCode, String expectedName) {
            Country expectedCountry = createTestCountry(expectedName, isoCode);
            given(countryRepository.findByIsoCode(isoCode)).willReturn(Optional.of(expectedCountry));

            Optional<Country> result = countryService.getCountryByIsoCode(isoCode);

            assertThat(result)
                .isPresent()
                .get()
                .satisfies(country -> {
                    assertThat(country.getIsoCode()).isEqualTo(isoCode);
                    assertThat(country.getName()).isEqualTo(expectedName);
                });
        }

        @ParameterizedTest
        @MethodSource("continentTestData")
        @DisplayName("Should find countries by continent")
        @Tag("parameterized")
        void shouldFindCountriesByContinent(String continent, List<String> expectedCountryNames) {
            List<Country> expectedCountries = expectedCountryNames.stream()
                .map(name -> createTestCountry(name, name.substring(0, 3).toUpperCase()))
                .toList();

            given(countryRepository.findByContinent(continent)).willReturn(expectedCountries);

            List<Country> result = countryService.getCountriesByContinent(continent);

            assertThat(result)
                .hasSize(expectedCountryNames.size())
                .extracting(Country::getName)
                .containsExactlyElementsOf(expectedCountryNames);
        }

        static Stream<Arguments> continentTestData() {
            return Stream.of(
                Arguments.of("Europe", Arrays.asList("France", "Germany", "Spain")),
                Arguments.of("Asia", Arrays.asList("China", "Japan", "India")),
                Arguments.of("North America", Arrays.asList("USA", "Canada", "Mexico"))
            );
        }
    }

    @Nested
    @DisplayName("Advanced Queries")
    class AdvancedQueries {

        @Test
        @DisplayName("Should find countries with population greater than threshold")
        @Tag("unit")
        void shouldFindCountriesWithHighPopulation() {
            Long populationThreshold = 50_000_000L;
            List<Country> expectedCountries = Arrays.asList(
                createTestCountry("China", "CHN"),
                createTestCountry("India", "IND")
            );

            given(countryRepository.findCountriesWithPopulationGreaterThan(populationThreshold))
                .willReturn(expectedCountries);

            List<Country> result = countryService.getCountriesWithPopulationGreaterThan(populationThreshold);

            assertThat(result)
                .hasSize(2)
                .extracting(Country::getName)
                .containsExactly("China", "India");

            verify(countryRepository).findCountriesWithPopulationGreaterThan(populationThreshold);
        }

        @Test
        @DisplayName("Should find countries with area greater than threshold")
        @Tag("unit")
        void shouldFindCountriesWithLargeArea() {
            Double areaThreshold = 1_000_000.0;
            List<Country> expectedCountries = Arrays.asList(
                createTestCountry("Russia", "RUS"),
                createTestCountry("Canada", "CAN")
            );

            given(countryRepository.findCountriesWithAreaGreaterThan(areaThreshold))
                .willReturn(expectedCountries);

            List<Country> result = countryService.getCountriesWithAreaGreaterThan(areaThreshold);

            assertThat(result)
                .isNotEmpty()
                .hasSize(2);
        }

        @Test
        @DisplayName("Should return all unique continents")
        @Tag("unit")
        void shouldReturnAllContinents() {
            List<String> expectedContinents = Arrays.asList(
                "Europe", "Asia", "North America", "South America", "Africa", "Oceania"
            );

            given(countryRepository.findAllContinents()).willReturn(expectedContinents);

            List<String> result = countryService.getAllContinents();

            assertThat(result)
                .hasSize(6)
                .containsExactlyElementsOf(expectedContinents);
        }
    }

    @Nested
    @DisplayName("Persistence Operations")
    class PersistenceOperations {

        @Test
        @DisplayName("Should save new country successfully")
        @Tag("unit")
        void shouldSaveNewCountry() {
            Country newCountry = createTestCountry("New Country", "NEW");
            Country savedCountry = createTestCountry("New Country", "NEW");
            savedCountry.setId(1L);

            given(countryRepository.save(newCountry)).willReturn(savedCountry);

            Country result = countryService.saveCountry(newCountry);

            assertThat(result)
                .isNotNull()
                .satisfies(country -> {
                    assertThat(country.getId()).isEqualTo(1L);
                    assertThat(country.getName()).isEqualTo("New Country");
                    assertThat(country.getIsoCode()).isEqualTo("NEW");
                });

            verify(countryRepository).save(newCountry);
        }

        @Test
        @DisplayName("Should delete country by ID")
        @Tag("unit")
        void shouldDeleteCountryById() {
            Long countryId = 1L;
            willDoNothing().given(countryRepository).deleteById(countryId);

            assertThatCode(() -> countryService.deleteCountry(countryId))
                .doesNotThrowAnyException();

            verify(countryRepository).deleteById(countryId);
        }

        @Test
        @DisplayName("Should check if country exists by ID")
        @Tag("unit")
        void shouldCheckIfCountryExists() {
            Long existingId = 1L;
            Long nonExistingId = 999L;

            given(countryRepository.existsById(existingId)).willReturn(true);
            given(countryRepository.existsById(nonExistingId)).willReturn(false);

            assertThat(countryService.existsById(existingId)).isTrue();
            assertThat(countryService.existsById(nonExistingId)).isFalse();

            verify(countryRepository).existsById(existingId);
            verify(countryRepository).existsById(nonExistingId);
        }

        @Test
        @DisplayName("Should return total count of countries")
        @Tag("unit")
        void shouldReturnTotalCountOfCountries() {
            long expectedCount = 195L;
            given(countryRepository.count()).willReturn(expectedCount);

            long actualCount = countryService.getTotalCountriesCount();

            assertThat(actualCount).isEqualTo(expectedCount);
            verify(countryRepository).count();
        }
    }

    @AfterEach
    void tearDown(TestInfo testInfo) {
        logTestDuration(testInfo);
        reset(countryRepository);
    }
}