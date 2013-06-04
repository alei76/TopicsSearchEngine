package vagueobjects.ir.lda.tokens;
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


import java.text.BreakIterator;
import java.util.*;

/**
 * Parses a document into a list of token ids and a list of counts,
 * and builds  document representation as  2D arrays of token ids and counts.
 */
public class Documents {
    private final Vocabulary vocabulary;
    /**
     * wordIds[i][j] gives the jth unique token present in document i
     */
    private int[][] wordIds;
    /**
     * tokenCts[i][j] is the number of times that the token given
     * by wordIds[i][j] appears in document i.
     */
    private int[][] tokenCts;

    public Documents(List<String> docs, Vocabulary vocab) {
        this.vocabulary = vocab;
        build(docs, vocab);
    }

    public Documents(String  doc, Vocabulary vocab) {
        this.vocabulary = vocab;
        List<String> docs = new ArrayList<String> ();
        docs.add(doc);
        build(docs, vocab);
    }

    public List<String> toString(List<Tuple> tuples){
        List<String> list = new ArrayList<String>();
        for (Tuple tuple: tuples){
            list.add(this.vocabulary.getToken(tuple.position));
        }
        return list;
    }

    private void build(List<String> docs, Vocabulary vocab){

        int numDocs = docs.size();
        this.wordIds = new int[numDocs][];
        this.tokenCts = new int[numDocs][];

        for(int docId=0; docId<docs.size();++docId){
            String doc = docs.get(docId);
            doc = doc.toLowerCase()
                    .replaceAll("-", " ")
                    .replaceAll("[^a-z ]", "")
                    .replaceAll(" +", " ");
            Map<Integer,Integer> counts = new LinkedHashMap<Integer, Integer>();
            List<String> tokens = extractTokens(doc);
            for(String token: tokens){
                if(vocab.contains(token)){
                    int tokenId = vocab.getId(token);
                    if(!counts.containsKey(tokenId)){
                        counts.put(tokenId, 1);
                    } else {
                        int c = counts.get(tokenId);
                        counts.put(tokenId, c+1);
                    }
                }
            }
            int tokenCount = counts.size();
            wordIds[docId] = new int[tokenCount];
            tokenCts[docId] = new int[tokenCount];
            int i=0 ;
            for(Map.Entry<Integer,Integer> e: counts.entrySet()){
                wordIds[docId][i] = e.getKey();
                tokenCts[docId][i] = e.getValue();
                ++i;
            }
        }
    }


    public String getToken(int i){
        return vocabulary.getToken(i);
    }


    public int[][] getTokenIds() {
        return wordIds;
    }

    /**
     * document Id x  token Id
     * @return
     */
    public int[][] getTokenCts() {
        return tokenCts;
    }

    public int size() {
        return tokenCts.length;
    }

    public int getTokenCount() {
        int total = 0;
        for(int [] d: tokenCts){
            for(int c: d){
                total+=c;
            }
        }
        return total;
    }
    private List<String> extractTokens(String doc ) {
        List<String> result = new ArrayList<String>();
        BreakIterator boundary = BreakIterator.getWordInstance( );
        boundary.setText(doc);
        int start = boundary.first();
        for (int end = boundary.next();
             end != BreakIterator.DONE;
             start = end, end = boundary.next()) {
            String s = doc.substring(start, end);
            if (s.trim().length() > 0) {
                result.add(s.toLowerCase( ));
            }
        }
        return result;
    }
}
