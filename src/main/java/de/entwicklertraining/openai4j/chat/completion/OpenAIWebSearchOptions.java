package de.entwicklertraining.openai4j.chat.completion;

import org.json.JSONObject;
import org.json.JSONException;

import java.util.Optional;

/**
 * Represents the web_search_options parameter for the OpenAI Chat Completions API.
 * <p>
 * Allows configuration of web search context size and user location for more relevant search results.
 * <p>
 * See OpenAI documentation for details:
 * https://platform.openai.com/docs/guides/web-search
 */
public class OpenAIWebSearchOptions {

    /**
     * Approximate user location for refining search results.
     */
    private final UserLocation userLocation;

    /**
     * Controls how much context is retrieved from the web.
     * Default is MEDIUM.
     */
    private final SearchContextSize searchContextSize;

    private OpenAIWebSearchOptions(Builder builder) {
        this.userLocation = builder.userLocation;
        this.searchContextSize = builder.searchContextSize;
    }

    /**
     * Creates a new OpenAIWebSearchOptions builder.
     *
     * @return Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * @return Optional user location.
     */
    public Optional<UserLocation> getUserLocation() {
        return Optional.ofNullable(userLocation);
    }

    /**
     * @return Optional search context size.
     */
    public Optional<SearchContextSize> getSearchContextSize() {
        return Optional.ofNullable(searchContextSize);
    }

    /**
     * Serializes this object to a JSONObject matching the OpenAI API structure.
     * If no options are set, returns an empty JSON object.
     *
     * @return JSONObject representing web_search_options.
     */
    public JSONObject toJson() {
        JSONObject json = new JSONObject();

        if (userLocation != null) {
            json.put("user_location", userLocation.toJson());
        }
        if (searchContextSize != null) {
            // Only include if not default (medium) or if explicitly set
            json.put("search_context_size", searchContextSize.getValue());
        }

        return json;
    }

    /**
     * Builder for OpenAIWebSearchOptions.
     */
    public static class Builder {
        private UserLocation userLocation;
        private SearchContextSize searchContextSize;

        /**
         * Sets the user location.
         *
         * @param userLocation UserLocation object.
         * @return Builder
         */
        public Builder userLocation(UserLocation userLocation) {
            this.userLocation = userLocation;
            return this;
        }

        /**
         * Sets the search context size.
         *
         * @param searchContextSize SearchContextSize enum.
         * @return Builder
         */
        public Builder searchContextSize(SearchContextSize searchContextSize) {
            this.searchContextSize = searchContextSize;
            return this;
        }

        /**
         * Builds the OpenAIWebSearchOptions object.
         *
         * @return OpenAIWebSearchOptions
         */
        public OpenAIWebSearchOptions build() {
            return new OpenAIWebSearchOptions(this);
        }
    }

    /**
     * Represents the user_location object for web search.
     */
    public static class UserLocation {
        /**
         * Type of location, always "approximate".
         */
        private final String type;

        /**
         * Approximate location details.
         */
        private final ApproximateLocation approximate;

        /**
         * Constructs a UserLocation with type "approximate".
         *
         * @param approximate ApproximateLocation details.
         */
        private UserLocation(ApproximateLocation approximate) {
            this.type = "approximate";
            this.approximate = approximate;
        }

        /**
         * Creates a new UserLocation builder.
         *
         * @return UserLocation.Builder instance
         */
        public static Builder builder() {
            return new Builder();
        }

        /**
         * @return Type string, always "approximate".
         */
        public String getType() {
            return type;
        }

        /**
         * @return Approximate location details.
         */
        public ApproximateLocation getApproximate() {
            return approximate;
        }

        /**
         * Serializes this object to a JSONObject.
         *
         * @return JSONObject
         */
        public JSONObject toJson() {
            JSONObject json = new JSONObject();
            json.put("type", type);
            if (approximate != null) {
                JSONObject approxJson = approximate.toJson();
                if (approxJson.length() > 0) {
                    json.put("approximate", approxJson);
                }
            }
            return json;
        }

        /**
         * Builder for UserLocation.
         */
        public static class Builder {
            private ApproximateLocation approximate;

            public Builder approximate(ApproximateLocation approximate) {
                this.approximate = approximate;
                return this;
            }

            public UserLocation build() {
                return new UserLocation(approximate);
            }
        }
    }

    /**
     * Represents the approximate location details.
     */
    public static class ApproximateLocation {
        private final String countryCode; // Two letter ISO country code
        private final String city;
        private final String region;
        private final String timezone;

        private ApproximateLocation(Builder builder) {
            this.countryCode = builder.countryCode;
            this.city = builder.city;
            this.region = builder.region;
            this.timezone = builder.timezone;
        }

        public static Builder builder() {
            return new Builder();
        }

        /**
         * @return Optional country code.
         */
        public Optional<String> getCountryCode() {
            return Optional.ofNullable(countryCode);
        }

        /**
         * @return Optional city.
         */
        public Optional<String> getCity() {
            return Optional.ofNullable(city);
        }

        /**
         * @return Optional region.
         */
        public Optional<String> getRegion() {
            return Optional.ofNullable(region);
        }

        /**
         * @return Optional timezone.
         */
        public Optional<String> getTimezone() {
            return Optional.ofNullable(timezone);
        }

        /**
         * Serializes this object to a JSONObject.
         * Only includes non-null fields.
         *
         * @return JSONObject
         */
        public JSONObject toJson() {
            JSONObject json = new JSONObject();
            if (countryCode != null) json.put("country", countryCode);
            if (city != null) json.put("city", city);
            if (region != null) json.put("region", region);
            if (timezone != null) json.put("timezone", timezone);
            return json;
        }

        public static class Builder {
            private String countryCode;
            private String city;
            private String region;
            private String timezone;

            public Builder countryCode(String countryCode) {
                this.countryCode = countryCode;
                return this;
            }

            public Builder city(String city) {
                this.city = city;
                return this;
            }

            public Builder region(String region) {
                this.region = region;
                return this;
            }

            public Builder timezone(String timezone) {
                this.timezone = timezone;
                return this;
            }

            public ApproximateLocation build() {
                return new ApproximateLocation(this);
            }
        }
    }

    /**
     * Enum for search_context_size parameter.
     */
    public enum SearchContextSize {
        HIGH("high"),
        MEDIUM("medium"),
        LOW("low");

        private final String value;

        SearchContextSize(String value) {
            this.value = value;
        }

        /**
         * @return String value for API ("high", "medium", "low").
         */
        public String getValue() {
            return value;
        }

        /**
         * Parses a string value to the corresponding enum.
         *
         * @param value String value.
         * @return SearchContextSize enum.
         * @throws IllegalArgumentException if value is invalid.
         */
        public static SearchContextSize fromValue(String value) {
            for (SearchContextSize size : values()) {
                if (size.value.equalsIgnoreCase(value)) {
                    return size;
                }
            }
            throw new IllegalArgumentException("Unknown search_context_size: " + value);
        }
    }
}