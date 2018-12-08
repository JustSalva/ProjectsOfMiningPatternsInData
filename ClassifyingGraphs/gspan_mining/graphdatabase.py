"""Implementation of a gSpan graph database with two classes."""
from __future__ import absolute_import
from __future__ import division
from __future__ import print_function

import codecs

from .graph import AUTO_EDGE_ID
from .graph import Graph


class GraphDatabase(object):

    def __init__(self):
        self._graph_cnt = 0
        self._graphs = dict()

    def read_graphs(self, filename):
        indexes = []
        with codecs.open(filename, 'r', 'utf-8') as f:
            lines = [line.strip() for line in f.readlines()]
            tgraph = None
            for i, line in enumerate(lines):
                cols = line.split(' ')
                if cols[0] == 't':
                    if tgraph is not None:
                        self._graphs[self._graph_cnt] = tgraph
                        indexes.append(self._graph_cnt)
                        self._graph_cnt += 1
                        tgraph = None
                    if cols[-1] == '-1':
                        break
                    tgraph = Graph(self._graph_cnt,
                                   is_undirected=True,
                                   eid_auto_increment=True)
                elif cols[0] == 'v':
                    tgraph.add_vertex(cols[1], cols[2])
                elif cols[0] == 'e':
                    tgraph.add_edge(AUTO_EDGE_ID, cols[1], cols[2], cols[3])
            # adapt to input files that do not end with 't # -1'
            if tgraph is not None:
                self._graphs[self._graph_cnt] = tgraph
                indexes.append(self._graph_cnt)
        return indexes
