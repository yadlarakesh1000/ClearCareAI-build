package com.clearcareai.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PagedResponse<T> {

    private List<T> content;      // the rows on this page
    private int page;             // which page this is (0-based)
    private int size;             // how many rows per page
    private long totalElements;   // total rows across all pages
    private int totalPages;       // total number of pages
    private boolean last;         // is this the final page?
}
