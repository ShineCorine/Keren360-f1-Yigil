package kr.co.yigil.global;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SortBy {
    CREATED_AT("createdAt"),
    RATE("rate"),
    ID("id");

    private String value;
    SortBy(String value) {
        this.value = value;
    }

}
