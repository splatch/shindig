package org.apache.shindig.api.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * Resource loader interface.
 */
public interface ResourceLoader {

    /**
     * Opens a given path as either a resource or a file, depending on the path
     * name.
     *
     * @param path
     * @return The opened input stream
     */
    InputStream open(String path) throws IOException;

    /**
     * Opens a resource
     * @param resource
     * @return An input stream for the given named resource
     * @throws IOException
     */
    InputStream openResource(String resource) throws IOException;

    /**
     * Reads the contents of a resource as a string.
     *
     * @param resource
     * @return Contents of the resource.
     * @throws IOException
     */
    String getContent(String resource) throws IOException;

}
