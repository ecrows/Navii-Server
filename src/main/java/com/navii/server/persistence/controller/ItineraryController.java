package com.navii.server.persistence.controller;

import com.navii.server.persistence.domain.Itinerary;
import com.navii.server.persistence.service.ItineraryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/itinerary")
public class ItineraryController {
    private static final Logger logger = LoggerFactory.getLogger(ItineraryController.class);

    @Autowired
    private ItineraryService itineraryService;

    /**
     * Gets a Itinerary by id
     * @param itineraryId   Identifier for Itinerary
     * @return          If Itinerary is found, return the Itinerary object and HTTP status 302; otherwise, 404
     */
    @RequestMapping(value = "/{itineraryId}", method = RequestMethod.GET)
    public ResponseEntity<Itinerary> getItinerary(@PathVariable String itineraryId) {
        Itinerary foundItinerary = itineraryService.findOne(itineraryId);

        if (foundItinerary != null) {
            return new ResponseEntity<>(foundItinerary, HttpStatus.FOUND);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Gets all Itinerarys in database
     * @return      If Geese exist, return list of Geese and HTTP status 302; otherwise, 404
     */
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<List<Itinerary>> getAllItinerarys() {
        List<Itinerary> geese = itineraryService.findAll();

        if (geese != null) {
            return new ResponseEntity<>(geese, HttpStatus.FOUND);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Creates a new Itinerary if the itinerary does not already exist
     * @param itinerary Itinerary to persist in server
     * @return      If Itinerary is successfully created, return HTTP status 201; otherwise, 400
     */
    @RequestMapping(value = "", method = RequestMethod.POST)
    public ResponseEntity<Itinerary> createItinerary(@RequestBody Itinerary itinerary) {
        int numCreatedItinerary = itineraryService.create(itinerary);

        if (numCreatedItinerary > 0) {
            return new ResponseEntity<>(HttpStatus.CREATED);
        } else {
            // TODO: choose better HTTP status
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Updates an existing Itinerary
     * @param itineraryId   Identifier for Itinerary
     * @param itinerary     Itinerary to persist in server
     * @return          If the Itinerary exists and is changed, return HTTP status 202; otherwise 404.
     */
    @RequestMapping(value = "/{itineraryId}", method = RequestMethod.PUT)
    public ResponseEntity<Itinerary> updateItinerary(@PathVariable String itineraryId, @RequestBody Itinerary itinerary) {
        int numUpdatedItinerary = itineraryService.update(itineraryId, itinerary);

        if (numUpdatedItinerary > 0) {
            return new ResponseEntity<>(HttpStatus.ACCEPTED);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Deletes an existing Itinerary
     * @param itineraryId   Identifier for the Itinerary
     * @return          If the Itinerary exists and is deleted, return HTTP status 202; otherwise 404.
     */
    @RequestMapping(value = "/{itineraryId}", method = RequestMethod.DELETE)
    public ResponseEntity<Itinerary> deleteItinerary(@PathVariable String itineraryId) {
        int numDeletedItinerary = itineraryService.delete(itineraryId);

        if (numDeletedItinerary > 0) {
            return new ResponseEntity<>(HttpStatus.ACCEPTED);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(value="/tags" , method= RequestMethod.GET)
    public List<Itinerary> getItinerariesFromTags(@RequestBody List<String> tagList) {
        /*List<Itinerary> itineraries = itineraryService.getItineraries(tagList);
        return itineraries;*/
        return null;
    }
}