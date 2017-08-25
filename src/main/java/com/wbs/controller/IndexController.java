package com.wbs.controller;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

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
                "  ?capital dbo:populationTotal ?nesto .\n" + // that has populationTotal property
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
        Model cityModel = ModelFactory.createDefaultModel();
        cityModel.read(dataUrl + city, "TURTLE");
        Resource cityResource = cityModel.getResource(resourceUrl + city);

        // System.out.println(cityResource);

        // create property for population (dbo:populationTotal)
        Property populationProperty = ResourceFactory.createProperty(dboPrefix + "populationTotal");
        int cityPopulation = Integer.parseInt(cityResource.getProperty(populationProperty).getString());

        //System.out.println(cityPopulation);

        // get info about all the country capitals
        ResultSet resSet = getOtherCitiesResults();

        // iterate the set and print the number of inhabitants in each capital
        while(resSet.hasNext()) {
            Resource resource = resSet.next().get("capital").asResource();
            String otherCityUrl = resource.toString().replaceAll("resource", "data");
            Model otherCityModel = ModelFactory.createDefaultModel();
            otherCityModel.read(otherCityUrl,"TURTLE");
            resource = otherCityModel.getResource(resource.toString());
            int otherCityPopulation = Integer.parseInt(resource.getProperty(populationProperty).getString());
            System.out.println(resource + " has " + otherCityPopulation + " inhabitants");
            ++count;
        }

        // print total number of cities processed (167)
        //System.out.println("VKUPNO " + count);

        return "index";
    }
}
