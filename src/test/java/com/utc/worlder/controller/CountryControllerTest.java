package com.utc.worlder.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.utc.worlder.config.AbstractTestBase;
import com.utc.worlder.entity.Country;
import com.utc.worlder.service.CountryService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CountryController.class)
@DisplayName("Country Controller Web Layer Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CountryControllerTest extends AbstractTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CountryService countryService;

    private Country france;
    private Country germany;
    private List<Country> countriesList;

    @BeforeEach
    void setUp() {
        france = createTestCountry("France", "FRA");
        france.setId(1L);
        
        germany = createTestCountry("Germany", "DEU");
        germany.setId(2L);
        
        countriesList = Arrays.asList(france, germany);
    }

    @Nested
    @DisplayName("GET Operations")
    class GetOperations {

        @Test
        @Order(1)
        @DisplayName("Should return all countries with HTTP 200")
        @Tag("web")
        void shouldReturnAllCountries_WithHttp200() throws Exception {
            given(countryService.getAllCountries()).willReturn(countriesList);

            mockMvc.perform(get("/api/countries")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].name", is("France")))
                    .andExpect(jsonPath("$[0].isoCode", is("FRA")))
                    .andExpect(jsonPath("$[1].name", is("Germany")))
                    .andExpect(jsonPath("$[1].isoCode", is("DEU")));

            verify(countryService).getAllCountries();
        }

        @Test
        @Order(2)
        @DisplayName("Should return empty array when no countries exist")
        @Tag("web")
        void shouldReturnEmptyArray_WhenNoCountriesExist() throws Exception {
            given(countryService.getAllCountries()).willReturn(Arrays.asList());

            mockMvc.perform(get("/api/countries"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @Order(3)
        @DisplayName("Should return country by ID with HTTP 200")
        @Tag("web")
        void shouldReturnCountryById_WithHttp200() throws Exception {
            Long countryId = 1L;
            given(countryService.getCountryById(countryId)).willReturn(Optional.of(france));

            mockMvc.perform(get("/api/countries/{id}", countryId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.name", is("France")))
                    .andExpect(jsonPath("$.isoCode", is("FRA")))
                    .andExpect(jsonPath("$.capital", is("Capital of France")))
                    .andExpect(jsonPath("$.continent", is("Europe")));

            verify(countryService).getCountryById(countryId);
        }

        @Test
        @Order(4)
        @DisplayName("Should return HTTP 404 when country not found by ID")
        @Tag("web")
        void shouldReturnHttp404_WhenCountryNotFoundById() throws Exception {
            Long nonExistentId = 999L;
            given(countryService.getCountryById(nonExistentId)).willReturn(Optional.empty());

            mockMvc.perform(get("/api/countries/{id}", nonExistentId))
                    .andExpect(status().isNotFound());

            verify(countryService).getCountryById(nonExistentId);
        }

        @ParameterizedTest(name = "Should find country by name: ''{0}''")
        @ValueSource(strings = {"France", "Germany", "Spain"})
        @DisplayName("Should return country by name")
        @Tag("web")
        void shouldReturnCountryByName(String countryName) throws Exception {
            Country testCountry = createTestCountry(countryName, countryName.substring(0, 3).toUpperCase());
            testCountry.setId(1L);
            given(countryService.getCountryByName(countryName)).willReturn(Optional.of(testCountry));

            mockMvc.perform(get("/api/countries/name/{name}", countryName))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name", is(countryName)));

            verify(countryService).getCountryByName(countryName);
        }

        @Test
        @DisplayName("Should return HTTP 404 when country not found by name")
        @Tag("web")
        void shouldReturnHttp404_WhenCountryNotFoundByName() throws Exception {
            String nonExistentName = "WilliamLand";
            given(countryService.getCountryByName(nonExistentName)).willReturn(Optional.empty());

            mockMvc.perform(get("/api/countries/name/{name}", nonExistentName))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return countries by continent")
        @Tag("web")
        void shouldReturnCountriesByContinent() throws Exception {
            String continent = "Europe";
            given(countryService.getCountriesByContinent(continent)).willReturn(countriesList);

            mockMvc.perform(get("/api/countries/continent/{continent}", continent))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[*].continent", everyItem(is("Europe"))));

            verify(countryService).getCountriesByContinent(continent);
        }

        @Test
        @DisplayName("Should return all continents")
        @Tag("web")
        void shouldReturnAllContinents() throws Exception {
            List<String> continents = Arrays.asList("Europe", "Asia", "Africa", "North America");
            given(countryService.getAllContinents()).willReturn(continents);

            mockMvc.perform(get("/api/countries/continents"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(4)))
                    .andExpect(jsonPath("$", containsInAnyOrder("Europe", "Asia", "Africa", "North America")));
        }

        @Test
        @DisplayName("Should return total countries count")
        @Tag("web")
        void shouldReturnTotalCountriesCount() throws Exception {
            long totalCount = 195L;
            given(countryService.getTotalCountriesCount()).willReturn(totalCount);

            mockMvc.perform(get("/api/countries/count"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("195"));

            verify(countryService).getTotalCountriesCount();
        }
    }

    @Nested
    @DisplayName("Filtered Operations")
    class FilteredOperations {

        @Test
        @DisplayName("Should return countries with minimum population")
        @Tag("web")
        void shouldReturnCountriesWithMinimumPopulation() throws Exception {
            Long minPopulation = 50_000_000L;
            given(countryService.getCountriesWithPopulationGreaterThan(minPopulation))
                .willReturn(countriesList);

            mockMvc.perform(get("/api/countries/population/min/{minPopulation}", minPopulation))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)));

            verify(countryService).getCountriesWithPopulationGreaterThan(minPopulation);
        }

        @Test
        @DisplayName("Should return countries with minimum area")
        @Tag("web")
        void shouldReturnCountriesWithMinimumArea() throws Exception {
            Double minArea = 100_000.0;
            given(countryService.getCountriesWithAreaGreaterThan(minArea))
                .willReturn(Arrays.asList(france));

            mockMvc.perform(get("/api/countries/area/min/{minArea}", minArea))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].name", is("France")));

            verify(countryService).getCountriesWithAreaGreaterThan(minArea);
        }
    }

    @Nested
    @DisplayName("POST/PUT/DELETE Operations")
    class CrudOperations {

        @Test
        @DisplayName("Should create new country with HTTP 201")
        @Tag("web")
        void shouldCreateNewCountry_WithHttp201() throws Exception {
            Country newCountry = createTestCountry("New Country", "NEW");
            Country savedCountry = createTestCountry("New Country", "NEW");
            savedCountry.setId(3L);
            
            given(countryService.saveCountry(any(Country.class))).willReturn(savedCountry);

            ResultActions result = mockMvc.perform(post("/api/countries")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(newCountry)));

            result.andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id", is(3)))
                    .andExpect(jsonPath("$.name", is("New Country")))
                    .andExpect(jsonPath("$.isoCode", is("NEW")));

            verify(countryService).saveCountry(any(Country.class));
        }

        @Test
        @DisplayName("Should update existing country with HTTP 200")
        @Tag("web")
        void shouldUpdateExistingCountry_WithHttp200() throws Exception {
            Long countryId = 1L;
            Country updatedCountry = createTestCountry("Updated France", "FRA");
            updatedCountry.setId(countryId);
            
            given(countryService.existsById(countryId)).willReturn(true);
            given(countryService.saveCountry(any(Country.class))).willReturn(updatedCountry);

            mockMvc.perform(put("/api/countries/{id}", countryId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updatedCountry)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name", is("Updated France")))
                    .andExpect(jsonPath("$.id", is(1)));

            verify(countryService).existsById(countryId);
            verify(countryService).saveCountry(any(Country.class));
        }

        @Test
        @DisplayName("Should return HTTP 404 when updating non-existent country")
        @Tag("web")
        void shouldReturnHttp404_WhenUpdatingNonExistentCountry() throws Exception {
            Long nonExistentId = 999L;
            Country updatedCountry = createTestCountry("Updated Country", "UPD");
            
            given(countryService.existsById(nonExistentId)).willReturn(false);

            mockMvc.perform(put("/api/countries/{id}", nonExistentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updatedCountry)))
                    .andExpect(status().isNotFound());

            verify(countryService).existsById(nonExistentId);
            verify(countryService, never()).saveCountry(any(Country.class));
        }

        @Test
        @DisplayName("Should delete country with HTTP 204")
        @Tag("web")
        void shouldDeleteCountry_WithHttp204() throws Exception {
            Long countryId = 1L;
            given(countryService.existsById(countryId)).willReturn(true);
            willDoNothing().given(countryService).deleteCountry(countryId);

            mockMvc.perform(delete("/api/countries/{id}", countryId))
                    .andExpect(status().isNoContent());

            verify(countryService).existsById(countryId);
            verify(countryService).deleteCountry(countryId);
        }

        @Test
        @DisplayName("Should return HTTP 404 when deleting non-existent country")
        @Tag("web")
        void shouldReturnHttp404_WhenDeletingNonExistentCountry() throws Exception {
            Long nonExistentId = 999L;
            given(countryService.existsById(nonExistentId)).willReturn(false);

            mockMvc.perform(delete("/api/countries/{id}", nonExistentId))
                    .andExpect(status().isNotFound());

            verify(countryService).existsById(nonExistentId);
            verify(countryService, never()).deleteCountry(nonExistentId);
        }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {

        @Test
        @DisplayName("Should handle invalid JSON with HTTP 400")
        @Tag("web")
        void shouldHandleInvalidJson_WithHttp400() throws Exception {
            mockMvc.perform(post("/api/countries")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{ invalid json }"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should handle service exception gracefully")
        @Tag("web")
        void shouldHandleServiceExceptionGracefully() throws Exception {
            Country newCountry = createTestCountry("New Country", "NEW");
            given(countryService.saveCountry(any(Country.class)))
                .willThrow(new RuntimeException("Database error"));
            mockMvc.perform(post("/api/countries")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(newCountry)))
                    .andExpect(status().isBadRequest());
        }
    }

    @AfterEach
    void tearDown(TestInfo testInfo) {
        logTestDuration(testInfo);
        reset(countryService);
    }
}