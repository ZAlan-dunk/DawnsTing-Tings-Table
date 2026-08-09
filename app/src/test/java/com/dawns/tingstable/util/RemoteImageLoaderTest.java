package com.dawns.tingstable.util;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RemoteImageLoaderTest {
    @Test
    public void acceptsOnlyHttpsChuimgCoverSources() {
        assertTrue(RemoteImageLoader.isAllowedSource(
                "https://i2.chuimg.com/cover.jpg?imageView2/2/w/620"));
        assertFalse(RemoteImageLoader.isAllowedSource(
                "http://i2.chuimg.com/cover.jpg"));
        assertFalse(RemoteImageLoader.isAllowedSource(
                "https://i2.chuimg.com.evil.example/cover.jpg"));
        assertFalse(RemoteImageLoader.isAllowedSource(
                "https://user@i2.chuimg.com/cover.jpg"));
        assertFalse(RemoteImageLoader.isAllowedSource("not-a-url"));
    }
}
