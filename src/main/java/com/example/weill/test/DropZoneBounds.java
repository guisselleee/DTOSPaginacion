package com.example.weill.test;

public record DropZoneBounds(double left, double top, double width, double height) {

    public static DropZoneBounds centeredFallback() {
        return new DropZoneBounds(36, 34, 28, 32);
    }
}
