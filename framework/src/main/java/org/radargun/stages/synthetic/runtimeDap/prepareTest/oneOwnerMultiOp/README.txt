Set replication degree D = 1
Then do R distinct put operations.
At prepare phase you will contact exactly R remote nodes.
1) You select the R nodes (at random, without the local one) that you want to contact
2) You will draw a key that is replicated by that node
3) You access the key
You need a specific hash function to extract a key belonging to a given node
I am assuming  TestPrepareSingleOwnerContendedStringHash
(you can also do it without it,
iterating over keys and looking at the primary owner until you get the one you want)
NB: I am assuming that you will contact also the nodes you read from. This is true for GMU but not for repeatable read!
You should add a flag or, just for simplicity, do only blind writes