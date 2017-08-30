package com.wbs.controller;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

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
    public String getCity(@RequestBody String city) {
        //System.out.println(city);
        if (city != null && city != ""){
            city = city.substring(0, 1).toUpperCase() + city.substring(1);
            System.out.println(city);
        }

        // read the model
        city = city.replaceAll(" ", "_");
        Model cityModel = ModelFactory.createDefaultModel();
        cityModel.read(dataUrl + city, "TURTLE");
        Resource cityResource = cityModel.getResource(resourceUrl + city);

        // System.out.println(cityResource);

        // create property for population (dbo:populationTotal)
        Property populationProperty = ResourceFactory.createProperty(dboPrefix + "populationTotal");
        Property areaProperty = ResourceFactory.createProperty(dboPrefix + "PopulatedPlace/areaTotal");
        Property elevationProperty = ResourceFactory.createProperty(dboPrefix + "elevation");
        int cityPopulation = Integer.parseInt(cityResource.getProperty(populationProperty).getString());
        float cityArea = Float.parseFloat(cityResource.getProperty(areaProperty).getString());
        float cityElevation = Float.parseFloat(cityResource.getProperty(elevationProperty).getString());

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
            try {
                otherCityPopulation = Integer.parseInt(resource.getProperty(populationProperty).getString());
                otherCityArea = Float.parseFloat(resource.getProperty(areaProperty).getString());
                otherCityElevation = Float.parseFloat(resource.getProperty(elevationProperty).getString());
                String cityName = resource.getProperty(FOAF.name).getString();
                int score = 0;
                score += gradeCities(cityPopulation, otherCityPopulation);
                score += gradeCities(cityArea, otherCityArea);
                score += gradeCities(cityElevation, otherCityElevation);
                String[] cityAttributes = new String[5];
                cityAttributes[0] = cityName;
                cityAttributes[1] = Integer.toString(otherCityPopulation);
                cityAttributes[2] = Float.toString(otherCityArea);
                cityAttributes[3] = Float.toString(otherCityElevation);
                cityAttributes[4] = Integer.toString(score);
                citiesPopulations.add(cityAttributes);
                citiesPopulations.sort(CitySort);
            } catch (Exception e) {
                continue;
            }
            System.out.println(resource + " has " + otherCityPopulation + " inhabitants and Area: " + otherCityArea + " and elevation: " + otherCityElevation);
        }

        for (String[] cityString : citiesPopulations) {
            System.out.println(cityString[0] + " " + cityString[4]);
        }

        // Most similar city is in position 0
        String[] firstCity = citiesPopulations.get(0);
        System.out.println(Arrays.toString(firstCity));

        return "index";
    }

    private Comparator<String[]> CitySort = new Comparator<String[]>() {

        @Override
        public int compare(String[] o1, String[] o2) {
            int rollno1 = Integer.parseInt(o1[4]);
            int rollno2 = Integer.parseInt(o2[4]);

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
