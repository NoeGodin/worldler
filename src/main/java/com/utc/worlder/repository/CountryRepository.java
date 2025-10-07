package com.utc.worlder.repository;

import com.utc.worlder.entity.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CountryRepository extends JpaRepository<Country, Long> {
    
    Optional<Country> findByName(String name);
    
    Optional<Country> findByIsoCode(String isoCode);
    
    List<Country> findByContinent(String continent);
    
    @Query("SELECT c FROM Country c WHERE c.population > :minPopulation")
    List<Country> findCountriesWithPopulationGreaterThan(Long minPopulation);
    
    @Query("SELECT c FROM Country c WHERE c.area > :minArea")
    List<Country> findCountriesWithAreaGreaterThan(Double minArea);
    
    @Query("SELECT DISTINCT c.continent FROM Country c")
    List<String> findAllContinents();
}