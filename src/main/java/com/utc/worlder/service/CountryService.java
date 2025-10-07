package com.utc.worlder.service;

import com.utc.worlder.entity.Country;
import com.utc.worlder.repository.CountryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CountryService {
    
    private final CountryRepository countryRepository;
    
    @Autowired
    public CountryService(CountryRepository countryRepository) {
        this.countryRepository = countryRepository;
    }
    
    public List<Country> getAllCountries() {
        return countryRepository.findAll();
    }
    
    public Optional<Country> getCountryById(Long id) {
        return countryRepository.findById(id);
    }
    
    public Optional<Country> getCountryByName(String name) {
        return countryRepository.findByName(name);
    }
    
    public Optional<Country> getCountryByIsoCode(String isoCode) {
        return countryRepository.findByIsoCode(isoCode);
    }
    
    public List<Country> getCountriesByContinent(String continent) {
        return countryRepository.findByContinent(continent);
    }
    
    public List<Country> getCountriesWithPopulationGreaterThan(Long minPopulation) {
        return countryRepository.findCountriesWithPopulationGreaterThan(minPopulation);
    }
    
    public List<Country> getCountriesWithAreaGreaterThan(Double minArea) {
        return countryRepository.findCountriesWithAreaGreaterThan(minArea);
    }
    
    public List<String> getAllContinents() {
        return countryRepository.findAllContinents();
    }
    
    public Country saveCountry(Country country) {
        return countryRepository.save(country);
    }
    
    public void deleteCountry(Long id) {
        countryRepository.deleteById(id);
    }
    
    public boolean existsById(Long id) {
        return countryRepository.existsById(id);
    }
    
    public long getTotalCountriesCount() {
        return countryRepository.count();
    }
}