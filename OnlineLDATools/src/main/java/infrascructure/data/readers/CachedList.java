/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package infrascructure.data.readers;

import infrascructure.data.Data;
import infrascructure.data.list.BigList;
import infrascructure.data.serialize.ResourceSerializer;
import infrascructure.data.util.IOHelper;
import infrascructure.data.util.Trace;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author shredinger
 * This implementation is too difficult and therefore is deprecated
 */
@Deprecated
public class CachedList<T extends Data> implements BigList<T> {

    private final String DATA_FILE = "cached_list_data.txt";

    private String sourceDir;
    private int cacheSize;
    private Map<Integer, T> cacheForWrite;
    private Map<Integer, T> cacheForRead;
    private volatile Integer index;

    private final ReentrantLock lock;
    private final Condition availableCond;

    protected ResourceSerializer<T> serializer;

    /**
     *
     */
    public CachedList(String dataDir, int cacheSize,
                      ResourceSerializer<T> serializer) {
        this.cacheSize = cacheSize;
        this.sourceDir = dataDir;
        cacheForWrite = new ConcurrentHashMap<Integer, T>(cacheSize);
        cacheForRead = new ConcurrentHashMap<Integer, T>(cacheSize);
        index = -1;
        this.lock = new ReentrantLock();
        availableCond = lock.newCondition();
        restoreObjectData();
        this.serializer = serializer;
    }

    /*
     * (non-Javadoc)
     * 
     * @see infrascructure.data.list.BigListResource#get(int)
     */
    @Override
    public T get(int i) {
        if (!contains(i)) {
            Trace.trace("Waiting for new elements ...");
            while (!contains(i)) {
                try {
                    availableCond.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        final ReentrantLock lock = this.lock;
        lock.lock();
        try {

            if (cacheForWrite.containsKey(i)) {
                return cacheForWrite.get(i);
            }
            if (cacheForRead.containsKey(i)) {
                return cacheForRead.get(i);
            }
            try {
                fillCacheRead(i);
                // TO DO: change - too difficult!!!
                flushCacheForWrite();
                return cacheForRead.get(i);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        } finally {
            lock.unlock();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see infrascructure.data.list.BigListResource#add(infrascructure.data.Data)
     */
    @Override
    public void add(T data) throws IOException {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            if (index + 1 > cacheSize) {
                flushCacheForWrite();
            }
            cacheForWrite.put(++index, data);
            availableCond.signal();
        } finally {
            lock.unlock();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see infrascructure.data.list.BigListResource#contains(int)
     */
    @Override
    public boolean contains(int i) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return i <= index;
        } finally {
            lock.unlock();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see infrascructure.data.list.BigListResource#size()
     */
    @Override
    public int size() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return index + 1;
        } finally {
            lock.unlock();
        }
    }

    protected void flushCacheForWrite() throws IOException {
        for (Integer i : cacheForWrite.keySet()) {
            T data = cacheForWrite.get(i);
            serializer.write(data, i);
        }
        cacheForWrite.clear();
        flushObjectData();
    }

    protected void fillCacheRead(int initialIndex) throws IOException {
        final ReentrantLock lock = this.lock;
        lock.lock();
        int upperBound = Math.min(initialIndex + cacheSize - 1, index);
        this.cacheForRead.clear();
        for (int i = initialIndex; i <= upperBound; i++) {
            T resource = serializer.read(i);
            cacheForRead.put(i, resource);
        }

    }

    protected void flushObjectData() {
        try {
            String path = sourceDir + IOHelper.FILE_SEPARATOR + DATA_FILE;
            IOHelper.saveToFile(path, index);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void restoreObjectData() {
        try {
            String path = sourceDir + IOHelper.FILE_SEPARATOR + DATA_FILE;
            File f = new File(path);
            if (f.exists()) {
                String data = IOHelper.readFromFile(path)
                        .replaceAll("\\n", "");
                index = Integer.parseInt(data);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected String getPath(int index) {
        return sourceDir + IOHelper.FILE_SEPARATOR + index + ".txt";
    }

}
