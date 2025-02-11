package com.prose.service.dto;

import java.util.Map;

public record PagedMap<T,U>(int pageNumber,
                       int pageSize,
                       int totalPages,
                       Map<T,U> value) {
}
