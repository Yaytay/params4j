/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uk.co.spudsoft.params4j.impl;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

/**
 * A convenience class for performing file watches on multiple dirs with a delay after each change to allow batches of changes to complete before being processed.
 * 
 * Changes to files often come in multiple operations and a quick file watcher can end up either performing unnecessary processing or processing inconsistent files.
 * To avoid this FileWatcher introduces a small delay (2s) after any file notifications before processing the change - if another change comes in during the delay the timer
 * restarts - files have to be stable for 2s.
 * 
 * @author jtalbut
 */
public class FileWatcher {
  
  private static final Logger logger = LoggerFactory.getLogger(FileWatcher.class);

  private static final int NOTIFICATION_DELAY_S = 2;
  private static final int DEFAULT_DELAY_S = 3600;
  
  private final AtomicReference<Thread> threadRef = new AtomicReference<>();
  private final WatchService watcher;
  private final Runnable callback;
  private Map<Path, WatchKey> watchKeys = new HashMap<>();

  /**
   * Constructor.
   * @param callback The Runnable that will be called when any of the watched paths have changed and the changes have stabilized.
   */
  public FileWatcher(Runnable callback) {
    WatchService tempWatchService = null;
    try {
      tempWatchService = FileSystems.getDefault().newWatchService();
    } catch(IOException ex) {
      logger.error("Failed to create watch service, configuration changes will not be noticed: ", ex);              
    }
    this.watcher = tempWatchService;
    this.callback = callback;
  }

  /**
   * Add a path to the list of paths being watched.
   * @param path The path to add to the list of watched paths.
   * @throws IOException if something goes wrong.
   */
  public void watch(Path path) throws IOException {
    if (watcher != null && !watchKeys.containsKey(path)) {
      WatchKey key = path.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
      watchKeys.put(path, key);
    }
  }

  /**
   * Start monitoring files.
   * 
   * Do not call this method more than once - it will create multiple threads that do the same thing.
   * 
   * Monitoring will only begin if at least one watch path has been set to watch.
   * 
   * @return true if monitoring started.
   */
  public boolean start() {
    if (!watchKeys.isEmpty()) {
      if (threadRef.get() == null) {
        Thread thread = new Thread(this::eventLoop, "Params4JFileWatcher");
        Thread oldThread = threadRef.compareAndExchange(null, thread);
        if (oldThread == null) {
          thread.start();
        }
      }
      return true;
    } else {
      return false;
    }
  }

  private void eventLoop() {

    int pollDelay = DEFAULT_DELAY_S;
    
    /**
     * This loop has two non-standard features:
     * 1. It doesn't care what has changed, the caller will run a full gather afterwards so it doesn't matter.
     * 2. It waits as bit after a filesystem notification before notifying the client to allow batches of notifications to complete.
     */
    while (true) {

      // Wait for key to be signaled
      WatchKey key;
      try {
        key = watcher.poll(pollDelay, TimeUnit.SECONDS);
      } catch (InterruptedException x) {
        return;
      }
      
      // There are now three possibilities:
      // 1. A new notification has come in.
      //    key != null -> set pollDelay to NOTIFICATION_DELAY_S.
      // 2. The timeout has been hit for no useful reason.
      //    key == null && pollDelay != NOTIFICATION_DELAY_S -> spin round
      // 3. The timeout has been hit after the post-notification delay.
      //    key == null && pollDelay == NOTIFICATION_DELAY_S -> notify and set pollDelay to default
      logger.trace("Poll completed, key: {}, pollDelay: {}", key, pollDelay);

      if (key == null) {
        if (NOTIFICATION_DELAY_S == pollDelay) {
          callback.run();
          pollDelay = DEFAULT_DELAY_S;
        }
      } else {
        // Drain the events, but don't care what they are (they can't all be OVERFLOW)
        key.pollEvents();
        pollDelay = NOTIFICATION_DELAY_S;
        key.reset();
      }
    }

  }

}
