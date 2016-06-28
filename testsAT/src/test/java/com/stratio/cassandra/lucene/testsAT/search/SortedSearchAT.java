/*
 * Copyright (C) 2014 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stratio.cassandra.lucene.testsAT.search;

import com.stratio.cassandra.lucene.testsAT.util.CassandraUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static com.stratio.cassandra.lucene.builder.Builder.*;

@RunWith(JUnit4.class)
public class SortedSearchAT extends AbstractSearchAT {

    @Test
    public void testSortIntegerAsc() {
        sort(field("integer_1").reverse(false)).checkOrderedIntColumns("integer_1", -5, -4, -3, -2, -1);
    }

    @Test
    public void testSortIntegerDesc() {
        sort(field("integer_1").reverse(true)).checkOrderedIntColumns("integer_1", -1, -2, -3, -4, -5);
    }

    @Test
    public void testSortIntegerDefault() {
        sort(field("integer_1")).checkOrderedIntColumns("integer_1", -5, -4, -3, -2, -1);
    }

    @Test
    public void testSortDoubleAsc() {
        sort(field("double_1").reverse(false)).checkOrderedDoubleColumns("double_1", 1D, 2D, 3D, 3D, 3D);
    }

    @Test
    public void testSortDoubleDesc() {
        sort(field("double_1").reverse(true)).checkOrderedDoubleColumns("double_1", 3D, 3D, 3D, 2D, 1D);
    }

    @Test
    public void testSortDoubleDefault() {
        sort(field("double_1")).checkOrderedDoubleColumns("double_1", 1D, 2D, 3D, 3D, 3D);
    }

    @Test
    public void testSortCombined() {
        sort(field("double_1"), field("integer_1")).checkOrderedDoubleColumns("double_1", 1D, 2D, 3D, 3D, 3D);
        sort(field("double_1"), field("integer_1")).checkOrderedIntColumns("integer_1", -1, -2, -5, -4, -3);
    }

    @Test
    public void testSortWithFilter() {
        filter(all()).sort(field("integer_1").reverse(false)).checkOrderedIntColumns("integer_1", -5, -4, -3, -2, -1);
    }

    @Test
    public void testSortWithQuery() {
        query(all()).sort(field("integer_1").reverse(false)).checkOrderedIntColumns("integer_1", -5, -4, -3, -2, -1);
    }

    @Test
    public void testSortWithFilterMustShouldAndNot() {
        search().filter(all())
                .query(all())
                .sort(field("integer_1").reverse(false))
                .checkOrderedIntColumns("integer_1", -5, -4, -3, -2, -1);
    }

    @Test
    public void testSortWithGeoDistanceFilterNotReversed() {
        search().filter(geoDistance("geo_point", -3.784519, 40.442163, "10000km"))
                .sort(geoDistanceField("geo_point", 40.442163, -3.784519).reverse(false))
                .checkOrderedIntColumns("integer_1", -1, -2, -3, -4, -5);
    }

    @Test
    public void testSortWithGeoDistanceQueryNotReversed() {
        search().query(geoDistance("geo_point", -3.784519, 40.442163, "10000km"))
                .sort(geoDistanceField("geo_point", 40.442163, -3.784519).reverse(false))
                .checkOrderedIntColumns("integer_1", -1, -2, -3, -4, -5);
    }

    @Test
    public void testSortWithGeoDistanceFilterReversed() {
        search().filter(geoDistance("geo_point", -3.784519, 40.442163, "10000km"))
                .sort(geoDistanceField("geo_point", 40.442163, -3.784519).reverse(true))
                .checkOrderedIntColumns("integer_1", -5, -4, -3, -2, -1);
    }

    @Test
    public void testSortWithGeoDistanceQueryReversed() {
        search().query(geoDistance("geo_point", -3.784519, 40.442163, "10000km"))
                .sort(geoDistanceField("geo_point", 40.442163, -3.784519).reverse(true))
                .checkOrderedIntColumns("integer_1", -5, -4, -3, -2, -1);
    }

    @Test
    public void testSortByRelevance() {
        CassandraUtils.builder("issue_123")
                      .withPartitionKey("id")
                      .withColumn("id", "int")
                      .withColumn("field", "text")
                      .build()
                      .createKeyspace()
                      .createTable()
                      .createIndex()
                      .insert(new String[]{"id", "field"}, new Object[]{1, "word"})
                      .insert(new String[]{"id", "field"}, new Object[]{2, "word word"})
                      .insert(new String[]{"id", "field"}, new Object[]{3, "word cat"})
                      .insert(new String[]{"id", "field"}, new Object[]{4, "word cat dog"})
                      .insert(new String[]{"id", "field"}, new Object[]{5, "dog"})
                      .refresh()
                      .query(match("field", "word"))
                      .checkOrderedIntColumns("id", 1, 2, 3, 4)
                      .filter(match("field", "word"))
                      .checkOrderedIntColumns("id", 1, 2, 4, 3)
                      .filter(match("field", "word"))
                      .sort(field("id").reverse(true))
                      .checkOrderedIntColumns("id", 4, 3, 2, 1);
    }
}
