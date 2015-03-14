# neoecode

NeoeCode is a binary encoder / decoder for document. (Not actually used yet)

just like things as json, bencode, bson or google buffer protocols.

It support list, map(dictionary), int, float, string(or binary).

integers could be set to its least byte length form(1,2,4,8 byte) for smaller data size.

And like other Neoe products, it is simple and smart :)

It mostly like bencode, but has a predefined length for string, list, map instead of use "e" at the end.

And compared to bson, neoecode output size should be smaller because the integer in it can auto-fit to smallest size. and also much simpler than other protocols.

Its structure is easy for human to understand but it is not human-readable.

A java implementation is in the source section.

well, compared to another similar project, http://code.google.com/p/rencode/

>>> new NeoeCode().encode(PyData.parseAll("{'a':0, 'b':[1,2], 'c':99}"),out)
21

>>> len(rencode.dumps({'a':0, 'b':[1,2], 'c':99}))
13

>>> len(bencode.bencode({'a':0, 'b':[1,2], 'c':99}))
26

so why rencode’s size is smaller, because rencode optimized for special range int, size of list, map, merge 2 bytes into 1 bytes. That is the magic played in rencode, not in neoecode. 
