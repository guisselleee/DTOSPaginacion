package com.example.weill.test;

import java.util.List;

public record TestItem(
    int pageNumber,
    int rowNumber,
    int itemNumber,
    int correctOption,
    String targetImagePath,
    DropZoneBounds dropZone,
    List<OptionImage> options
) {
    public String key() {
        return pageNumber + "-" + rowNumber + "-" + itemNumber;
    }
}
