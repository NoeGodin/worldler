package com.utc.worlder.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "countries")
public class Country {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String name;
    
    @Column(name = "iso_code", nullable = false, unique = true, length = 3)
    private String isoCode;
    
    private String capital;
    
    private String continent;
    
    private Long population;
    
    private Double area;
    
    private String currency;
    
    @Column(name = "official_language")
    private String officialLanguage;

    public Country() {}

    public Country(String name, String isoCode, String capital, String continent, Long population, Double area, String currency, String officialLanguage) {
        this.name = name;
        this.isoCode = isoCode;
        this.capital = capital;
        this.continent = continent;
        this.population = population;
        this.area = area;
        this.currency = currency;
        this.officialLanguage = officialLanguage;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIsoCode() {
        return isoCode;
    }

    public void setIsoCode(String isoCode) {
        this.isoCode = isoCode;
    }

    public String getCapital() {
        return capital;
    }

    public void setCapital(String capital) {
        this.capital = capital;
    }

    public String getContinent() {
        return continent;
    }

    public void setContinent(String continent) {
        this.continent = continent;
    }

    public Long getPopulation() {
        return population;
    }

    public void setPopulation(Long population) {
        this.population = population;
    }

    public Double getArea() {
        return area;
    }

    public void setArea(Double area) {
        this.area = area;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getOfficialLanguage() {
        return officialLanguage;
    }

    public void setOfficialLanguage(String officialLanguage) {
        this.officialLanguage = officialLanguage;
    }
}