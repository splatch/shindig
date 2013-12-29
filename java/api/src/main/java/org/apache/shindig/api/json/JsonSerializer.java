package org.apache.shindig.api.json;

import java.io.IOException;

public interface JsonSerializer {

  String serialize(Object object);

  void append(Appendable appendable, Object object) throws IOException;

}
