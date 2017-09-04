package com.wbs.model;

public class City {

    String name;

    int population;

    float totalArea;

    float elevation;

    float cityPrecipitation;

    float cityDecSunHours;

    float cityYearHighC;

    float cityYearLowC;

    public City(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPopulation() {
        return population;
    }

    public void setPopulation(int population) {
        this.population = population;
    }

    public float getTotalArea() {
        return totalArea;
    }

    public void setTotalArea(float totalArea) {
        this.totalArea = totalArea;
    }

    public float getElevation() {
        return elevation;
    }

    public void setElevation(float elevation) {
        this.elevation = elevation;
    }

    public float getCityPrecipitation() {
        return cityPrecipitation;
    }

    public void setCityPrecipitation(float cityPrecipitation) {
        this.cityPrecipitation = cityPrecipitation;
    }

    public float getCityDecSunHours() {
        return cityDecSunHours;
    }

    public void setCityDecSunHours(float cityDecSunHours) {
        this.cityDecSunHours = cityDecSunHours;
    }

    public float getCityYearHighC() {
        return cityYearHighC;
    }

    public void setCityYearHighC(float cityYearHighC) {
        this.cityYearHighC = cityYearHighC;
    }

    public float getCityYearLowC() {
        return cityYearLowC;
    }

    public void setCityYearLowC(float cityYearLowC) {
        this.cityYearLowC = cityYearLowC;
    }
}
