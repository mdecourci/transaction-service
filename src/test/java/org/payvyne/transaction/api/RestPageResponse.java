package org.payvyne.transaction.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

public class RestPageResponse<T> extends PageImpl<T> {

    public RestPageResponse() {
        super(new ArrayList<>());
    }

    public RestPageResponse(List<T> content, Pageable pageable, long total) {
        super(content, pageable, total);
    }

    public RestPageResponse(List<T> content) {
        super(content);
    }

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public RestPageResponse(//
                            @JsonProperty("content") List<T> content, // PageImpl
                            @JsonProperty("number") int number, // PageImpl
                            @JsonProperty("size") int size, // PageImpl
                            @JsonProperty("totalElements") long totalElements, // PageImpl
                            @JsonProperty("pageable") JsonNode pageable, //
                            @JsonProperty("sort") JsonNode sort, //
                            @JsonProperty("totalPages") int totalPages, // computed
                            @JsonProperty("first") boolean first, // computed
                            @JsonProperty("last") boolean last, // computed
                            @JsonProperty("empty") boolean empty, // computed
                            @JsonProperty("numberOfElements") int numberOfElements // computed
    ) {
        super(content, PageRequest.of(number, size), totalElements);
    }
}
