/*
 * Licensed to STRATIO (C) under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.  The STRATIO (C) licenses this file
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
 */

package com.stratio.cassandra.lucene;

import com.stratio.cassandra.lucene.index.FSIndex;
import org.apache.cassandra.config.CFMetaData;
import org.apache.cassandra.db.ColumnFamilyStore;
import org.apache.cassandra.db.ReadCommand;
import org.apache.cassandra.db.partitions.UnfilteredPartitionIterator;
import org.apache.cassandra.db.rows.UnfilteredRowIterator;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;

import java.util.Iterator;
import java.util.Set;

/**
 * @author Andres de la Pena {@literal <adelapena@stratio.com>}
 */
public abstract class IndexPartitionIterator implements UnfilteredPartitionIterator {

    private final ReadCommand command;
    private final ColumnFamilyStore table;
    private final FSIndex lucene;
    private final IndexSearcher searcher;
    protected final Iterator<Document> documents;
    protected UnfilteredRowIterator next;

    public IndexPartitionIterator(ReadCommand command,
                                  ColumnFamilyStore table,
                                  FSIndex lucene,
                                  Query query,
                                  Sort sort,
                                  ScoreDoc after,
                                  Set<String> fieldsToLoad) {
        this.command = command;
        this.table = table;
        this.lucene = lucene;

        int limit = command.limits().count();
        searcher = lucene.acquireSearcher();
        documents = lucene.search(searcher, query, sort, after, limit, fieldsToLoad);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isForThrift() {
        return command.isForThrift();
    }

    /** {@inheritDoc} */
    @Override
    public CFMetaData metadata() {
        return table.metadata;
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasNext() {
        return prepareNext();
    }

    /** {@inheritDoc} */
    @Override
    public UnfilteredRowIterator next() {
        if (next == null) {
            prepareNext();
        }
        UnfilteredRowIterator result = next;
        next = null;
        return result;
    }

    protected abstract boolean prepareNext();

    /** {@inheritDoc} */
    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public void close() {
        lucene.releaseSearcher(searcher); // TODO: Ensure always closed
        if (next != null) {
            next.close();
        }
    }
}
