package org.radargun.btt.colocated;

import org.radargun.CallableWrapper;
import org.radargun.LocatedKey;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class IteratorRemoteTask implements CallableWrapper<List<Long>>, Serializable {

    private LocatedKey treeKey;
    
    public IteratorRemoteTask(LocatedKey treeKey) {
	this.treeKey = treeKey;
    }
    
    @Override
    public List<Long> doTask() throws Exception {
	List<Long> result = new ArrayList<Long>();
	
	BPlusTree<Long> tree = null; //(BPlusTree<Long>) BPlusTree.CACHE.get(treeKey);
	for (Long v : tree) {
	    result.add(v);
	}
	
	return result;
    }

}
