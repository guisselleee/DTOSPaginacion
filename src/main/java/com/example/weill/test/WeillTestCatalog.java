package com.example.weill.test;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Service
public class WeillTestCatalog {

    public static final int PAGE_COUNT = 5;
    public static final int ROWS_PER_PAGE = 4;
    public static final int ITEMS_PER_ROW = 3;
    public static final int OPTIONS_PER_ITEM = 8;
    public static final int MAX_SCORE = PAGE_COUNT * ROWS_PER_PAGE * ITEMS_PER_ROW;

    private static final int[][] ANSWER_KEY = {
        {2, 8, 7},
        {4, 5, 6},
        {8, 4, 4},
        {7, 3, 2}
    };

    public List<TestPage> pages() {
        List<TestPage> pages = new ArrayList<>();
        for (int page = 1; page <= PAGE_COUNT; page++) {
            pages.add(new TestPage(page, itemsForPage(page)));
        }
        return pages;
    }

    public List<TestItem> itemsForPage(int pageNumber) {
        validatePage(pageNumber);
        List<TestItem> items = new ArrayList<>();
        for (int row = 1; row <= ROWS_PER_PAGE; row++) {
            for (int item = 1; item <= ITEMS_PER_ROW; item++) {
                items.add(item(pageNumber, row, item));
            }
        }
        return items;
    }

    public TestItem item(int pageNumber, int rowNumber, int itemNumber) {
        validateCoordinates(pageNumber, rowNumber, itemNumber);
        return new TestItem(
            pageNumber,
            rowNumber,
            itemNumber,
            correctOption(pageNumber, rowNumber, itemNumber),
            "/images/weill/placeholders/target.svg",
            optionImages(pageNumber, rowNumber, itemNumber)
        );
    }

    public int correctOption(int pageNumber, int rowNumber, int itemNumber) {
        validateCoordinates(pageNumber, rowNumber, itemNumber);
        return ANSWER_KEY[rowNumber - 1][itemNumber - 1];
    }

    public boolean isCorrect(int pageNumber, int rowNumber, int itemNumber, int selectedOption) {
        validateOption(selectedOption);
        return selectedOption == correctOption(pageNumber, rowNumber, itemNumber);
    }

    public int totalItems() {
        return MAX_SCORE;
    }

    private List<OptionImage> optionImages(int pageNumber, int rowNumber, int itemNumber) {
        return IntStream.rangeClosed(1, OPTIONS_PER_ITEM)
            .mapToObj(option -> new OptionImage(
                option,
                "/images/weill/placeholders/option-" + option + ".svg"
            ))
            .toList();
    }

    private void validateCoordinates(int pageNumber, int rowNumber, int itemNumber) {
        validatePage(pageNumber);
        if (rowNumber < 1 || rowNumber > ROWS_PER_PAGE) {
            throw new IllegalArgumentException("Row number must be between 1 and " + ROWS_PER_PAGE);
        }
        if (itemNumber < 1 || itemNumber > ITEMS_PER_ROW) {
            throw new IllegalArgumentException("Item number must be between 1 and " + ITEMS_PER_ROW);
        }
    }

    private void validatePage(int pageNumber) {
        if (pageNumber < 1 || pageNumber > PAGE_COUNT) {
            throw new IllegalArgumentException("Page number must be between 1 and " + PAGE_COUNT);
        }
    }

    private void validateOption(int selectedOption) {
        if (selectedOption < 1 || selectedOption > OPTIONS_PER_ITEM) {
            throw new IllegalArgumentException("Selected option must be between 1 and " + OPTIONS_PER_ITEM);
        }
    }
}
