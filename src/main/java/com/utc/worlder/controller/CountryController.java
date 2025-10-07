package com.utc.worlder.controller;

import com.utc.worlder.entity.Country;
import com.utc.worlder.service.CountryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/countries")
@CrossOrigin(origins = "*")
public class CountryController {
    
    private final CountryService countryService;
    
    @Autowired
    public CountryController(CountryService countryService) {
        this.countryService = countryService;
    }
    
    @GetMapping
    public ResponseEntity<List<Country>> getAllCountries() {
        List<Country> countries = countryService.getAllCountries();
        return ResponseEntity.ok(countries);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Country> getCountryById(@PathVariable Long id) {
        Optional<Country> country = countryService.getCountryById(id);
        return country.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/name/{name}")
    public ResponseEntity<Country> getCountryByName(@PathVariable String name) {
        Optional<Country> country = countryService.getCountryByName(name);
        return country.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/code/{isoCode}")
    public ResponseEntity<Country> getCountryByIsoCode(@PathVariable String isoCode) {
        Optional<Country> country = countryService.getCountryByIsoCode(isoCode);
        return country.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/continent/{continent}")
    public ResponseEntity<List<Country>> getCountriesByContinent(@PathVariable String continent) {
        List<Country> countries = countryService.getCountriesByContinent(continent);
        return ResponseEntity.ok(countries);
    }
    
    @GetMapping("/population/min/{minPopulation}")
    public ResponseEntity<List<Country>> getCountriesWithMinPopulation(@PathVariable Long minPopulation) {
        List<Country> countries = countryService.getCountriesWithPopulationGreaterThan(minPopulation);
        return ResponseEntity.ok(countries);
    }
    
    @GetMapping("/area/min/{minArea}")
    public ResponseEntity<List<Country>> getCountriesWithMinArea(@PathVariable Double minArea) {
        List<Country> countries = countryService.getCountriesWithAreaGreaterThan(minArea);
        return ResponseEntity.ok(countries);
    }
    
    @GetMapping("/continents")
    public ResponseEntity<List<String>> getAllContinents() {
        List<String> continents = countryService.getAllContinents();
        return ResponseEntity.ok(continents);
    }
    
    @GetMapping("/count")
    public ResponseEntity<Long> getTotalCountriesCount() {
        long count = countryService.getTotalCountriesCount();
        return ResponseEntity.ok(count);
    }
    
    @PostMapping
    public ResponseEntity<Country> createCountry(@RequestBody Country country) {
        try {
            Country savedCountry = countryService.saveCountry(country);
            return new ResponseEntity<>(savedCountry, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Country> updateCountry(@PathVariable Long id, @RequestBody Country country) {
        if (!countryService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        
        country.setId(id);
        Country updatedCountry = countryService.saveCountry(country);
        return ResponseEntity.ok(updatedCountry);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCountry(@PathVariable Long id) {
        if (!countryService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        
        countryService.deleteCountry(id);
        return ResponseEntity.noContent().build();
    }
}