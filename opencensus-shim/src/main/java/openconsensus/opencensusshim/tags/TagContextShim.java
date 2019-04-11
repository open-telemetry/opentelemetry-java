package openconsensus.opencensusshim.tags;

import io.opencensus.tags.Tag;
import io.opencensus.tags.TagContext;
import java.util.Iterator;

public final class TagContextShim extends TagContext {

  @Override
  protected Iterator<Tag> getIterator() {
    throw new UnsupportedOperationException();
  }
}
