package com.navii.server.persistence.yelpAPI;

import com.navii.server.persistence.domain.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Code sample for accessing the Yelp API V2.
 * <p>
 * This program demonstrates the capability of the Yelp API version 2.0 by using the Search API to
 * query for businesses by a search term and location, and the Business API to query additional
 * information about the top result nfrom the search query.
 * <p>
 * <p>
 * See <a href="http://www.yelp.com/developers/documentation">Yelp Documentation</a> for more info.
 */

public class YelpThread extends Thread {

    private static final String API_HOST = "api.yelp.com";
    private static final String DEFAULT_LOCATION = "Toronto, ON";
    private int radius = 3000;
    //TODO: CHANGE TO ACTUAL LIMIT
    private static final int SEARCH_LIMIT = 20;

    private static final String SEARCH_PATH = "/v2/search";

    private static final String V3_APPID = "XH4jpCC5i6JC1bATfWvNxA";
    private static final String V3_APPSECRET = "CgefNNDRCXQ1C2sApqpDxG1T0KJkNQy7iJ1UFTb7K3zgvAOXlhOuV2ytHyoX40Im";
    private static final String V3_API_HOST = "https://api.yelp.com/v3";

    private static final String CONSUMER_KEY = "iTmWPOyJ1wneoYALeoXuJA";
    private static final String CONSUMER_SECRET = "X_rMRFPKqZhDhZvWeow7hS3BYcY";
    private static final String TOKEN = "CXw2pupEi9Ige8xD0_RCiv0wlidxILrX";
    private static final String TOKEN_SECRET = "MsoAW60bef21LrDTWJFm___lmGk";

    private static final String REQUEST_TERM = "term";
    private static final String REQUEST_LOCATION = "location";
    private static final String REQUEST_CLL = "cll";
    private static final String REQUEST_LIMIT = "limit";
    private static final String REQUEST_RADIUS_FILTER = "radius_filter";
    private static final String REQUEST_SORT = "sort";
    private static final String REQUEST_CATEGORY_FILTER = "category_filter";

    private final static String JSON_BUSINESSES = "businesses";
    private final static String JSON_LOCATION = "location";
    private final static String JSON_NAME = "name";
    private final static String JSON_CATEGORIES = "categories";
    private static final String JSON_IMAGE_URL = "image_url";
    private static final String JSON_SNIPPET_TEXT = "snippet_text";
    private static final String JSON_RATING = "rating";
    private static final String JSON_PHONE_NUMBER = "display_phone";
    private static final String JSON_COORDINATES = "coordinate";
    private static final String JSON_LATITUDE = "latitude";
    private static final String JSON_LONGITUDE = "longitude";

    private static final String DEFAULT_NA = "N/A";

    private Itinerary itinerary;
    private Set<String> uniqueCheckHashSet;
    private List<Venture> potentialAttractionStack;
    private List<Attraction> attractionsPrefetch;
    private List<Attraction> restaurantPrefetch;
    private Set<String> filterCategories = new HashSet<>();

    List<String> terms = new ArrayList<>();
    private int sort;
    private int days;
    private String TAG;

    private static Map<Integer, String> nameMap = new HashMap<>();

    static {
        nameMap.put(0, "Best Matched");
        nameMap.put(1, "By Distance");
        nameMap.put(2, "Highest Rated");
    }

    public static String getYelpName(int id) {
        return nameMap.get(id);
    }

    OAuthService service;
    Token accessToken;

    public YelpThread(List<Venture> potentialAttractionStack, int days, List<String> terms, int sort, String tag, Set<String> filterCategories) {
        this.service = new ServiceBuilder().provider(TwoStepOAuth.class).apiKey(CONSUMER_KEY).apiSecret(CONSUMER_SECRET).build();
        this.accessToken = new Token(TOKEN, TOKEN_SECRET);
        this.potentialAttractionStack = potentialAttractionStack;
        this.days = days;
        this.terms = terms;
        this.sort = sort;
        this.TAG = tag;
        this.uniqueCheckHashSet = new HashSet<>();
        this.filterCategories = filterCategories;
    }



    /**
     * Creates and sends a request to the Search API by venture and location.
     * <p>
     * See <a href="http://www.yelp.com/developers/documentation/v2/search_api">Yelp Search API V2</a>
     * for more info.
     *
     * @param venture <tt>String</tt> of the search venture to be queried
     * @return <tt>String</tt> JSON Response
     */
    public String searchForBusinessesByLocation(Venture venture, String cll) {
        OAuthRequest request = createOAuthRequest(SEARCH_PATH);
        //TODO: PUT INTO OBJECTS
        if (venture.getTerm() != "") {
            request.addQuerystringParameter(REQUEST_TERM, venture.getTerm());
        }
        request.addQuerystringParameter(REQUEST_LOCATION, DEFAULT_LOCATION);
        request.addQuerystringParameter(REQUEST_CLL, cll);
        request.addQuerystringParameter(REQUEST_LIMIT, String.valueOf(SEARCH_LIMIT));
        request.addQuerystringParameter(REQUEST_RADIUS_FILTER, String.valueOf(radius));
        request.addQuerystringParameter(REQUEST_SORT, String.valueOf(sort));
        if (!venture.getCategories().isEmpty()) {
            request.addQuerystringParameter(REQUEST_CATEGORY_FILTER, venture.getCategories());
        }
        return sendRequestAndGetResponse(request);
    }


    /**
     * Creates and returns an {@link OAuthRequest} based on the API endpoint specified.
     *
     * @param path API endpoint to be queried
     * @return <tt>OAuthRequest</tt>
     */
    private OAuthRequest createOAuthRequest(String path) {
        OAuthRequest request = new OAuthRequest(Verb.GET, "https://" + API_HOST + path);
        return request;
    }

    /**
     * Sends an {@link OAuthRequest} and returns the {@link Response} body.
     *
     * @param request {@link OAuthRequest} corresponding to the API request
     * @return <tt>String</tt> body of API response
     */
    private String sendRequestAndGetResponse(OAuthRequest request) {
        System.out.println(TAG + ":Querying " + request.getCompleteUrl() + " ...");
        this.service.signRequest(this.accessToken, request);
        Response response = request.send();
        return response.getBody();
    }

    /**
     * Queries the Search API based on the command line arguments and takes the first result to query
     * the Business API.
     *
     * @param venture venture object to define correct attraction
     * @param cll     location of current search place
     * @return an Attraction object
     */
    private List<Attraction> parseYelpResponse(Venture venture, String cll) {
        String searchResponseJSON = searchForBusinessesByLocation(venture, cll);

        JSONArray businesses = parseYelpToJSON(searchResponseJSON);
        List<Attraction> attractions = new ArrayList<>();
        if (businesses == null) {
            return attractions;
        }
        for (Object object : businesses) {

            JSONObject businessObject = (JSONObject) object;
            String name = businessObject.getOrDefault(JSON_NAME, DEFAULT_NA).toString();

            if (uniqueCheckHashSet.contains(name)) {
                continue;
            }
            uniqueCheckHashSet.add(name);

            List<String> categories = new ArrayList<>();
            if (businessObject.containsKey(JSON_CATEGORIES)) {
                JSONArray jsonArray = (JSONArray) businessObject.get(JSON_CATEGORIES);
                categories = convertJSONStringArray(jsonArray);
            }
            if (!Collections.disjoint(filterCategories, categories)) {
                continue;
            }

            JSONObject locationObject = (JSONObject) businessObject.get(JSON_LOCATION);
            Location location = retreiveLocation(locationObject);

            int price = 0;

            String photoUri = businessObject.getOrDefault(JSON_IMAGE_URL, DEFAULT_NA).toString().replace("ms.jpg", "o.jpg");
            String blurb = businessObject.getOrDefault(JSON_SNIPPET_TEXT, DEFAULT_NA).toString();
            double rating = 0;
            if (businessObject.containsKey(JSON_RATING)) {
                rating = Double.parseDouble(businessObject.get(JSON_RATING).toString());
            }

            String phoneNumber = "";
            if (businessObject.containsKey(JSON_PHONE_NUMBER)) {
                phoneNumber = businessObject.get(JSON_PHONE_NUMBER).toString();
            }
            Attraction attraction = new Attraction.Builder()
                    .name(name)
                    .photoUri(photoUri)
                    .blurbUri(blurb)
                    .location(location)
                    .duration(3)
                    .price(price)
                    .rating(rating)
                    .description(categories.stream().collect(Collectors.joining(", ")))
                    .phoneNumber(phoneNumber)
                    .build();
            attractions.add(attraction);
        }
        return attractions;
    }

    private JSONArray parseYelpToJSON(String searchResponseJSON) {
        JSONParser parser = new JSONParser();
        JSONObject response;
        try {
            response = (JSONObject) parser.parse(searchResponseJSON);
        } catch (ParseException pe) {
            System.out.println("Error: could not parse JSON response:");
            System.out.println(searchResponseJSON);
            return null;
        } catch (Exception e) {
            System.out.println("Error: could not parse JSON response:");
            System.out.println(e.getMessage());
            return null;
        }
        return (JSONArray) response.get(JSON_BUSINESSES);

    }

    private List<String> convertJSONStringArray(JSONArray jsonArray) {
        List<String> categories = new ArrayList<>();
        for (Object object : jsonArray.toArray()) {
            JSONArray array = (JSONArray) object;
            categories.add(array.get(1).toString());
        }
        return categories;
    }

    private Location retreiveLocation(JSONObject locationObject) {
        double latitude = 43.644176, longitude = -79.387375;
        if (locationObject.containsKey(JSON_COORDINATES)) {
            JSONObject coordinate = (JSONObject) locationObject.get(JSON_COORDINATES);
            latitude = (double) coordinate.get(JSON_LATITUDE);
            longitude = (double) coordinate.get(JSON_LONGITUDE);
        }

        JSONArray addressArray = (JSONArray) locationObject.getOrDefault("display_address", "No Location Found");
        List<String> categories = new ArrayList<>();
        for (Object object : addressArray.toArray()) {
            String string = (String) object;
            categories.add(string);
        }
        String address = categories.stream().collect(Collectors.joining(", "));

        Location location = new Location.Builder()
                .countryCode(locationObject.getOrDefault("country_code", DEFAULT_NA).toString())
                .address(address)
                .latitude(latitude)
                .longitude(longitude)
                .city("Toronto")
                .neighborhoods("")
                .build();

        return location;
    }

    public Itinerary buildItinerary() {
        Venture restaurantVenture = potentialAttractionStack.get(0);
        Venture sightVenture = potentialAttractionStack.get(1);

        List<Attraction> restaurants = new ArrayList<>();
        while (restaurants.size() < (days * 3)) {
            radius += 1000;
            restaurants.addAll(fetchAttractions(restaurantVenture));
        }
        radius = 3000;

        List<Attraction> sights = new ArrayList<>();
        while (sights.size() < (days * 3)) {
            for (String term : terms) {
                sightVenture.setTerm(term);
                sights.addAll(fetchAttractions(sightVenture));
            }
            //if caught in loop
            if (radius >= 7000) {
                sightVenture.setTerm("Attraction");
                sights.addAll(fetchAttractions(sightVenture));
            }
            radius += 1000;
        }
        List<PackageScheduleListItem> packageScheduleListItem = new ArrayList<>();
        for (int day = 0; day < days; day++) {
            int currentDay = day+1;
            packageScheduleListItem.add(
                    new PackageScheduleListItem.Builder()
                    .itemType(PackageScheduleListItem.TYPE_DAY_HEADER)
                    .name("Day "+currentDay).build());
            for (int i = 0; i < 3; i++) {
                int header = i+1;
                packageScheduleListItem.add(new PackageScheduleListItem.Builder()
                        .itemType(header)
                        .build());
                Attraction restaurant;
                Attraction sight;

                if (sort == 1) {
                    restaurant = restaurants.remove(0);
                    sight = sights.remove(0);
                } else {
                    restaurant = restaurants.remove(new Random().nextInt(restaurants.size()));
                    sight = sights.remove(new Random().nextInt(sights.size()));
                }

                if (i < 2) {
                    packageScheduleListItem.add(new PackageScheduleListItem.Builder()
                            .itemType(PackageScheduleListItem.TYPE_ITEM)
                            .attraction(restaurant)
                            .build());
                    packageScheduleListItem.add(new PackageScheduleListItem.Builder()
                            .itemType(PackageScheduleListItem.TYPE_ITEM)
                            .attraction(sight)
                            .build());
                } else {
                    packageScheduleListItem.add(new PackageScheduleListItem.Builder()
                            .itemType(PackageScheduleListItem.TYPE_ITEM)
                            .attraction(sight)
                            .build());
                    packageScheduleListItem.add(new PackageScheduleListItem.Builder()
                            .itemType(PackageScheduleListItem.TYPE_ITEM)
                            .attraction(restaurant)
                            .build());
                }
            }
        }
        Itinerary itinerary = new Itinerary.Builder()
                .packageScheduleListItems(packageScheduleListItem)
                .authorId("Yelp")
                .description(nameMap.get(sort))
                .build();
        attractionsPrefetch = sights;
        restaurantPrefetch = restaurants;

        return itinerary;
    }

    private List<Attraction> fetchAttractions(Venture venture) {
        double latitude = 43.644176, longitude = -79.387375;
        String cll = latitude + "," + longitude;
        return parseYelpResponse(venture, cll);
    }

    @Override
    public void run() {
        setItinerary(buildItinerary());
    }

    private void setItinerary(Itinerary itinerary) {
        this.itinerary = itinerary;
    }

    public Itinerary getItinerary() {
        return itinerary;
    }

    public List<Attraction> getAttractionsPrefetch() {
        return attractionsPrefetch;
    }

    public List<Attraction> getRestaurantPrefetch() {
        return restaurantPrefetch;
    }

    public Set<String> getUniqueCheckHashSet() {
        return uniqueCheckHashSet;
    }
}