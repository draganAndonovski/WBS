package com.wbs.controller;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

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
        int count = 0;

        //System.out.println(city);

        // read the model
        city = city.replaceAll(" ", "_");
        Model cityModel = ModelFactory.createDefaultModel();
        cityModel.read(dataUrl + city, "TURTLE");
        Resource cityResource = cityModel.getResource(resourceUrl + city);

        // System.out.println(cityResource);

        // create property for population (dbo:populationTotal)
        Property populationProperty = ResourceFactory.createProperty(dboPrefix + "populationTotal");
        Property areaProperty = ResourceFactory.createProperty(dboPrefix + "PopulatedPlace/areaTotal");
        int cityPopulation = Integer.parseInt(cityResource.getProperty(populationProperty).getString());
        float cityArea = Float.parseFloat(cityResource.getProperty(areaProperty).getString());

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
            try {
                otherCityPopulation = Integer.parseInt(resource.getProperty(populationProperty).getString());
                otherCityArea = Float.parseFloat(resource.getProperty(areaProperty).getString());
                System.out.println(otherCityArea);
                String cityName = resource.getProperty(FOAF.name).getString();
                String[] cityAttributes = new String[3];
                cityAttributes[0] = cityName;
                cityAttributes[1] = Integer.toString(otherCityPopulation);
                cityAttributes[2] = Float.toString(otherCityArea);
                citiesPopulations.add(cityAttributes);
            } catch (Exception e) {
                continue;
            }
            System.out.println(resource + " has " + otherCityPopulation + " inhabitants and Area: " + otherCityArea);
            ++count;
        }

        // filter cities with population and area bigger or smaller by 30% of the chosen city
        for (String[] cityAttr : citiesPopulations) {
            int diffPopulation = cityPopulation * 30 / 100;
            float diffArea = cityArea * 30 / 100;
            if (Integer.parseInt(cityAttr[1]) + diffPopulation < cityPopulation || Integer.parseInt(cityAttr[1]) - diffPopulation > cityPopulation) {
                continue;
            }
            if (Float.parseFloat(cityAttr[2]) + diffArea < cityArea || Float.parseFloat(cityAttr[2]) - diffArea > cityArea) {
                continue;
            }
            System.out.println(cityAttr[0] + " " + cityAttr[1] + " " + cityAttr[2]);
        }

        // print total number of cities processed (167)
        //System.out.println("VKUPNO " + count);

        return "index";
    }
}
