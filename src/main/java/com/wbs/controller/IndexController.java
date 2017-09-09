package com.wbs.controller;

import com.wbs.model.City;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

@RequestMapping(value = "/")
@Controller
public class IndexController {

    static String startingUrl = "http://dbpedia.org/sparql";

    static String resourceUrl = "http://dbpedia.org/resource/";

    static String dataUrl = "http://dbpedia.org/data/";

    static String dboPrefix = "http://dbpedia.org/ontology/";

    static String dbpPrefix = "http://dbpedia.org/property/";

    @RequestMapping(value = "/")
    public String index() {
        return "index";
    }

    @RequestMapping(value = "/city")
    public String result() {
        return "result";
    }

    private ResultSet getResults(Query query) {
        return QueryExecutionFactory.sparqlService(startingUrl, query).execSelect();
    }

    private ResultSet getOtherCitiesResults() {
        ParameterizedSparqlString qs = new ParameterizedSparqlString("" +
                "prefix dbo:    <http://dbpedia.org/ontology/>\n" +
                "prefix dbp:    <http://dbpedia.org/property/>\n" +
                "prefix foaf:    <http://xmlns.com/foaf/0.1/>\n" +
                "prefix dct:    <http://purl.org/dc/terms/>\n" +
                "prefix rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "prefix rdfs:    <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "\n" +
                "select ?capital where {\n" +
                "  ?country rdf:type dbo:Country .\n" + // get all countries
                "  ?country dct:subject ?unitedNations .\n" + // that are in the United Nations
                "  ?country dbo:capital ?capital .\n" + // with capital city
                "  ?capital dbo:populationTotal ?population .\n" + // that has populationTotal property
                "}"
        );

        Model model = ModelFactory.createDefaultModel();
        model.read(dataUrl + "Category:Member_states_of_the_United_Nations", "TURTLE");
        Resource unitedNationsResource = model.getResource(resourceUrl + "Category:Member_states_of_the_United_Nations");
        qs.setParam("unitedNations", unitedNationsResource); // set the united nations as parameter to the query


        return getResults(QueryFactory.create(qs.asQuery()));
    }

    @RequestMapping(value = "/city", method = RequestMethod.POST)
    public String getCity(@RequestParam("city") String city,
                            org.springframework.ui.Model model) {
        //System.out.println(city);
        if (city != null && city != "") {
            city = city.substring(0, 1).toUpperCase() + city.substring(1);
            System.out.println(city);
        }

        City firstCity = new City(city);

        // read the model
        city = city.replaceAll(" ", "_");
        Model cityModel = ModelFactory.createDefaultModel();
        cityModel.read(dataUrl + city, "TURTLE");
        Resource cityResource = cityModel.getResource(resourceUrl + city);
        model.addAttribute("prv",firstCity);

        // System.out.println(cityResource);

        // create property for population (dbo:populationTotal)

        Property populationProperty = ResourceFactory.createProperty(dboPrefix + "populationTotal");
        Property areaProperty = ResourceFactory.createProperty(dboPrefix + "PopulatedPlace/areaTotal");
        Property elevationProperty = ResourceFactory.createProperty(dboPrefix + "elevation");
        Property precipitationProperty = ResourceFactory.createProperty(dbpPrefix + "aprPrecipitationMm");
        Property decSunHoursProperty = ResourceFactory.createProperty(dbpPrefix + "decSun");
        Property yearHighCProperty = ResourceFactory.createProperty(dbpPrefix + "yearRecordHighC");
        Property yearLowCProperty = ResourceFactory.createProperty(dbpPrefix + "yearRecordLowC");

        int cityPopulation = 0;
        float cityArea = 0;
        float cityElevation = 0;
        float cityPrecipitation = 0;
        float cityDecSunHours = 0;
        float cityYearHighC = 0;
        float cityYearLowC = 0;
        try {
            cityPopulation = Integer.parseInt(cityResource.getProperty(populationProperty).getString());
            cityArea = Float.parseFloat(cityResource.getProperty(areaProperty).getString());
            cityElevation = Float.parseFloat(cityResource.getProperty(elevationProperty).getString());
            cityPrecipitation = Float.parseFloat(cityResource.getProperty(precipitationProperty).getString());
            cityDecSunHours = Float.parseFloat(cityResource.getProperty(decSunHoursProperty).getString());
            cityYearHighC = Float.parseFloat(cityResource.getProperty(yearHighCProperty).getString());
            cityYearLowC = Float.parseFloat(cityResource.getProperty(yearLowCProperty).getString());

            firstCity.setPopulation(cityPopulation);
            firstCity.setTotalArea(cityArea);
            firstCity.setElevation(cityElevation);
            firstCity.setCityPrecipitation(cityPrecipitation);
            firstCity.setCityDecSunHours(cityDecSunHours);
            firstCity.setCityYearHighC(cityYearHighC);
            firstCity.setCityYearLowC(cityYearLowC);
        } catch (Exception e) {
        }

//        System.out.println(cityArea);
        //System.out.println(cityPopulation);

        // get info about all the country capitals
        ResultSet resSet = getOtherCitiesResults();

        ArrayList<String[]> citiesPopulations = new ArrayList<>();

        // iterate the set and print the number of inhabitants in each capital
        while (resSet.hasNext()) {
            Resource resource = resSet.next().get("capital").asResource();
            String otherCityUrl = resource.toString().replaceAll("resource", "data");
            Model otherCityModel = ModelFactory.createDefaultModel();
            otherCityModel.read(otherCityUrl, "TURTLE");
            resource = otherCityModel.getResource(resource.toString());
            int otherCityPopulation;
            float otherCityArea;
            float otherCityElevation;
            float otherCityPrecipitation;
            float otherCityDecSunHours;
            float otherCityYearHighC;
            float otherCityYearLowC;
            try {
                otherCityPopulation = Integer.parseInt(resource.getProperty(populationProperty).getString());
                otherCityArea = Float.parseFloat(resource.getProperty(areaProperty).getString());
                otherCityElevation = Float.parseFloat(resource.getProperty(elevationProperty).getString());
                otherCityPrecipitation = Float.parseFloat(resource.getProperty(precipitationProperty).getString());
                otherCityDecSunHours = Float.parseFloat(resource.getProperty(decSunHoursProperty).getString());
                otherCityYearHighC = Float.parseFloat(resource.getProperty(yearHighCProperty).getString());
                otherCityYearLowC = Float.parseFloat(resource.getProperty(yearLowCProperty).getString());
                String cityName = resource.getProperty(FOAF.name).getString();
                int score = 0;
                if (cityPopulation != 0) {
                    score += gradeCities(cityPopulation, otherCityPopulation);
                }
                if (cityArea != 0) {
                    score += gradeCities(cityArea, otherCityArea);
                }
                if (cityElevation != 0) {
                    score += gradeCities(cityElevation, otherCityElevation);
                }
                if (cityPrecipitation != 0) {
                    score += gradeCities(cityPrecipitation, otherCityPrecipitation);
                }
                if (cityDecSunHours != 0) {
                    score += gradeCities(cityDecSunHours, otherCityDecSunHours);
                }
                if (cityYearHighC != 0){
                    score += gradeCities(cityYearHighC, otherCityYearHighC);
                }
                if (cityYearLowC != 0){
                    score += gradeCities(cityYearLowC, otherCityYearLowC);
                }
                String[] cityAttributes = new String[9];
                cityAttributes[0] = cityName;
                cityAttributes[1] = Integer.toString(otherCityPopulation);
                cityAttributes[2] = Float.toString(otherCityArea);
                cityAttributes[3] = Float.toString(otherCityElevation);
                cityAttributes[4] = Float.toString(otherCityPrecipitation);
                cityAttributes[5] = Integer.toString(score);
                cityAttributes[6] = Float.toString(otherCityDecSunHours);
                cityAttributes[7] = Float.toString(otherCityYearHighC);
                cityAttributes[8] = Float.toString(otherCityYearLowC);
                citiesPopulations.add(cityAttributes);
                citiesPopulations.sort(CitySort);
            } catch (Exception e) {
                continue;
            }
            System.out.println(resource + " has " + otherCityPopulation + " inhabitants and Area: "
                    + otherCityArea + " and elevation: " + otherCityElevation + ", precipitation: "
                    + otherCityPrecipitation + ", december sun hours: " + otherCityDecSunHours);
        }

        for (String[] cityString : citiesPopulations) {
            System.out.println(cityString[0] + " " + cityString[5]);
        }

        // Most similar city is in position 0
        String[] firstCityStrings = citiesPopulations.get(0);
        City otherCity = new City(firstCityStrings[0]);
        otherCity.setPopulation(Integer.parseInt(firstCityStrings[1]));
        otherCity.setTotalArea(Float.parseFloat(firstCityStrings[2]));
        otherCity.setElevation(Float.parseFloat(firstCityStrings[3]));
        otherCity.setCityPrecipitation(Float.parseFloat(firstCityStrings[4]));
        otherCity.setCityDecSunHours(Float.parseFloat(firstCityStrings[6]));
        otherCity.setCityYearHighC(Float.parseFloat(firstCityStrings[7]));
        otherCity.setCityYearLowC(Float.parseFloat(firstCityStrings[8]));
        System.out.println(Arrays.toString(firstCityStrings));
        model.addAttribute("vtor",otherCity);

        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpSession session = attr.getRequest().getSession();
        session.setAttribute("prv", firstCity);
        session.setAttribute("vtor", otherCity);

        return "result";
    }

    private Comparator<String[]> CitySort = new Comparator<String[]>() {

        @Override
        public int compare(String[] o1, String[] o2) {
            int rollno1 = Integer.parseInt(o1[5]);
            int rollno2 = Integer.parseInt(o2[5]);

	   /*For ascending order*/
            return rollno2 - rollno1;
        }
    };

    private int gradeCities(float cityArg, float otherCitArg) {
        if (otherCitArg + cityArg * 20 / 100 > cityArg && otherCitArg < cityArg) {
            return 5;
        } else if (otherCitArg - cityArg * 20 / 100 < cityArg && otherCitArg > cityArg) {
            return 5;
        } else if (otherCitArg - cityArg * 40 / 100 < cityArg && otherCitArg > cityArg) {
            return 4;
        } else if (otherCitArg - cityArg * 40 / 100 < cityArg && otherCitArg > cityArg) {
            return 4;
        } else if (otherCitArg - cityArg * 60 / 100 < cityArg && otherCitArg > cityArg) {
            return 3;
        } else if (otherCitArg - cityArg * 60 / 100 < cityArg && otherCitArg > cityArg) {
            return 3;
        } else if (otherCitArg - cityArg * 80 / 100 < cityArg && otherCitArg > cityArg) {
            return 2;
        } else if (otherCitArg - cityArg * 80 / 100 < cityArg && otherCitArg > cityArg) {
            return 2;
        } else {
            return 1;
        }
    }
}
