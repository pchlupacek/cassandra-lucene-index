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

package com.stratio.cassandra.lucene.search.condition.builder;

import com.stratio.cassandra.lucene.search.condition.GeoDistance;
import com.stratio.cassandra.lucene.search.condition.GeoTransformation;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.annotate.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Andres de la Pena {@literal <adelapena@stratio.com>}
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({@JsonSubTypes.Type(value = GeoTransformationBuilder.Buffer.class, name = "buffer"),
               @JsonSubTypes.Type(value = GeoTransformationBuilder.Copy.class, name = "copy")})
public interface GeoTransformationBuilder<T extends GeoTransformation> {

    T build();

    @JsonTypeName("identity")
    final class Copy implements GeoTransformationBuilder<GeoTransformation.Copy> {

        @JsonCreator
        public Copy() {
        }

        @Override
        public GeoTransformation.Copy build() {
            return new GeoTransformation.Copy();
        }
    }

    @JsonTypeName("clipper")
    final class Buffer implements GeoTransformationBuilder<GeoTransformation.Buffer> {

        protected static final Logger logger = LoggerFactory.getLogger(GeoTransformationBuilder.class);

        /** The max allowed distance. */
        @JsonProperty("max_distance")
        private final String maxDistance;

        /** The min allowed distance. */
        @JsonProperty("min_distance")
        private String minDistance;

        @JsonCreator
        public Buffer(@JsonProperty("max_distance") String maxDistance) {
            this.maxDistance = maxDistance;
        }

        /**
         * Sets the min allowed distance.
         *
         * @param minDistance the min distance
         * @return this with the specified min distance
         */
        public Buffer setMinDistance(String minDistance) {
            this.minDistance = minDistance;
            return this;
        }

        /** {@inheritDoc}*/
        @Override
        public GeoTransformation.Buffer build() {
            GeoDistance min = StringUtils.isBlank(minDistance) ? null : GeoDistance.parse(minDistance);
            GeoDistance max = StringUtils.isBlank(maxDistance) ? null : GeoDistance.parse(maxDistance);
            return new GeoTransformation.Buffer(max, min);
        }

    }
}