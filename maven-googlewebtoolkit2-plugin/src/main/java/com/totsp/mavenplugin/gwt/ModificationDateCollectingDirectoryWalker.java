package com.totsp.mavenplugin.gwt;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.io.DirectoryWalker;



/**
 * @author Marek Romanowski
 * @since 2009-01-02
 */
public class ModificationDateCollectingDirectoryWalker extends DirectoryWalker {
  
  
  
  public long walk(File file) {
    SortedSet<Long> modificationDates = new TreeSet<Long>();
    try {
      walk(file, modificationDates);
    } catch (IOException e) {
      // something is wrong - return biggest possible date
      return Long.MAX_VALUE;
    }
    return modificationDates.last();
  }
  
  

  @SuppressWarnings("unchecked")
  @Override
  protected void handleFile(File file, int depth, Collection results)
      throws IOException {
    results.add(Long.valueOf(file.lastModified()));
  }
}


