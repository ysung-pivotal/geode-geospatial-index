/*
 * Copyright [2016] Charlie Black
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.geode.geospatial.grid;

import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.partition.PartitionListenerAdapter;
import com.gemstone.gemfire.cache.partition.PartitionRegionHelper;
import com.gemstone.gemfire.internal.cache.BucketRegion;
import com.gemstone.gemfire.internal.cache.PartitionedRegion;
import com.gemstone.gemfire.internal.cache.partitioned.Bucket;
import org.apache.geode.geospatial.index.GeospaitalIndex;
import org.springframework.beans.factory.annotation.Required;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * When / IF there is a failure in the grid this code will be called to index the new primary items.
 *
 * This code will also be called when there is a re-balance in the cluster.
 *
 * The invariant that we are maintaining is the node which is holding the primary copy of the data is the one responsible
 * for maintaining that index.
 *
 * Created by Charlie Black on 6/23/16.
 */
public class IndexHAMaintenance extends PartitionListenerAdapter {
    private GeospaitalIndex geospaitalIndex;
    private PartitionedRegion region;

    @Required
    public void setGeospaitalIndex(GeospaitalIndex geospaitalIndex) {
        this.geospaitalIndex = geospaitalIndex;
    }


    @Override
    public void afterBucketRemoved(int bucketId, Iterable<?> keys) {
        Iterator<?> it = keys.iterator();
        it.forEachRemaining(key -> geospaitalIndex.remove(key));
    }

    @Override
    public void afterPrimary(int bucketId) {
        if (region != null) {
            Bucket b = region.getRegionAdvisor().getBucket(bucketId);
            BucketRegion bucketRegion = b.getBucketAdvisor().getProxyBucketRegion().getHostedBucketRegion();
            Set<Map.Entry> entries = bucketRegion.entrySet();
            entries.forEach(curr -> geospaitalIndex.upsert(curr.getKey(), curr.getValue()));
        }
    }

    @Override
    public void afterRegionCreate(Region<?, ?> reg) {
        region = (PartitionedRegion) reg;

        // We are going to re-index all of the primary data so clear out anything we might have and re-index.
        geospaitalIndex.clear();
        Region localPrimaryData = PartitionRegionHelper.getLocalPrimaryData(region);
        Set<Map.Entry> entries = localPrimaryData.entrySet();
        entries.forEach(curr -> geospaitalIndex.upsert(curr.getKey(), curr.getValue()));
    }

}

