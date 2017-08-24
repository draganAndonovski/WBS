package com.wbs.controller;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RequestMapping(value = "/")
@Controller
public class IndexController {

    @RequestMapping(value = "/")
    public String index() {
        return "index";
    }

    @RequestMapping(value = "/city", method = RequestMethod.POST)
    public String getCity(@RequestBody String city) {

        System.out.println(city);

        String endpoint = "http://dbpedia.org/sparql";

        String queryString = "select distinct ?Concept where {[] a ?Concept} LIMIT 100";;
        Query query = QueryFactory.create(queryString);
        QueryExecution exec = QueryExecutionFactory.sparqlService(endpoint, query);
        ResultSet resSet = exec.execSelect();
        while (resSet.hasNext()){
            QuerySolution solution = resSet.nextSolution();
            System.out.println(solution.get("Concept"));
        }

        return "index";
    }
}
